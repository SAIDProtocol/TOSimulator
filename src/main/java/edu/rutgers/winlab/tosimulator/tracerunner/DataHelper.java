package edu.rutgers.winlab.tosimulator.tracerunner;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author jiachen
 */
public class DataHelper {

    private static final Logger LOG = Logger.getLogger(DataHelper.class.getName());

    public static List<Transmitter> generateRandomTransmitters(
            Random random, int count,
            double xMin, double xMax, double yMin, double yMax) {
        List<Transmitter> ret = new ArrayList<>();
        double xDiff = xMax - xMin, yDiff = yMax - yMin;
        for (int i = 1; i <= count; i++) {
            double x = random.nextDouble() * xDiff + xMin;
            double y = random.nextDouble() * yDiff + yMin;
            ret.add(new Transmitter("T" + i, new Point2D.Double(x, y)));
        }
        return ret;
    }

    public static void writeTransmitters(Collection<Transmitter> transmitters, String fileName) throws IOException {
        try (PrintStream ps = new PrintStream(fileName)) {
            transmitters.forEach(t -> {
                ps.printf("%s\t%d\t%d%n", t.getName(),
                        Double.doubleToLongBits(t.getLocation().getX()),
                        Double.doubleToLongBits(t.getLocation().getY()));
            });
        }
    }

    public static List<Transmitter> readTransmitters(String fileName) throws IOException {
        List<Transmitter> ret = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                assert parts.length == 3;
                double x = Double.longBitsToDouble(Long.parseLong(parts[1]));
                double y = Double.longBitsToDouble(Long.parseLong(parts[2]));
                ret.add(new Transmitter(parts[0], new Point2D.Double(x, y)));
            }
        }
        return ret;
    }

    public static List<Receiver> generateRandomReceivers(
            Random random, int count,
            double xMin, double xMax, double yMin, double yMax) {
        List<Receiver> ret = new ArrayList<>();
        double xDiff = xMax - xMin, yDiff = yMax - yMin;
        for (int i = 1; i <= count; i++) {
            double x = random.nextDouble() * xDiff + xMin;
            double y = random.nextDouble() * yDiff + yMin;
            ret.add(new Receiver("R" + i, new Point2D.Double(x, y)));
        }
        return ret;
    }

    public static void writeReceivers(Collection<Receiver> receivers, String fileName) throws IOException {
        try (PrintStream ps = new PrintStream(fileName)) {
            receivers.forEach(r -> {
                ps.printf("%s\t%d\t%d%n", r.getName(),
                        Double.doubleToLongBits(r.getLocation().getX()),
                        Double.doubleToLongBits(r.getLocation().getY()));
            });
        }
    }

    public static List<Receiver> readReceivers(String fileName) throws IOException {
        List<Receiver> ret = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                assert parts.length == 3;
                double x = Double.longBitsToDouble(Long.parseLong(parts[1]));
                double y = Double.longBitsToDouble(Long.parseLong(parts[2]));
                ret.add(new Receiver(parts[0], new Point2D.Double(x, y)));
            }
        }
        return ret;
    }

    private static void addNextTransmit(Object... params) {
        Transmitter t = (Transmitter) params[0];
        long transmitDuration = (long) params[1];
        long driftStandardDeviation = (long) params[2];
        long repeatDuration = (long) params[3];
        int remaining = (int) params[4];
        Random random = (Random) params[5];
        PrintStream ps = (PrintStream) params[6];

        long sendStart = Timeline.nowInUs();
        ps.printf("%s\t%d\t%d%n", t.getName(), sendStart, transmitDuration);

        //add event start, duration
        remaining--;
        if (remaining > 0) {
            long sendEnd = sendStart + transmitDuration;
            //calculate drift
            double d = Double.MAX_VALUE;
            while (d < -driftStandardDeviation || d > driftStandardDeviation) {
                d = random.nextGaussian() * driftStandardDeviation;
            }
            //add event add next
            long nextStart = sendStart + repeatDuration + (long) Math.round(d);
//            System.out.println(sendStart);
//            System.out.println(nextStart);
            nextStart = Math.max(sendEnd, nextStart);
            Timeline.addEvent(nextStart, DataHelper::addNextTransmit,
                    t, transmitDuration, driftStandardDeviation, repeatDuration, remaining, random, ps);
        }
    }

    public static void generateTrace(
            Random random,
            Map<Transmitter, Long> startTimes,
            long transmitDuration, long repeatDuration, int repeatTimes,
            long driftStandardDeviation,
            String fileName) throws FileNotFoundException {
        ReportObject ro = new ReportObject();
        ro.setKey("Time", () -> String.format("%,d", Timeline.nowInUs()));
        try (PrintStream ps = new PrintStream(fileName)) {
            ro.beginReport();
            startTimes.forEach((t, s) -> {
                Timeline.addEvent(s, DataHelper::addNextTransmit,
                        t, transmitDuration, driftStandardDeviation, repeatDuration, repeatTimes, random, ps);
            });
            long end = Timeline.run();
            System.out.printf("%,d%n", end);
            ro.endReport();
            ps.flush();
        }

    }

    public static class TraceRunner {

        private final Iterator<String> input;
//        private final PrintStream output;
        private final Map<String, Transmitter> ts;
        private final Field f;
        private int lineId = 0, count = 0, maxContinuousCount = 0;
        private final Map<Transmitter, Integer> transmitterContinuousMisses;
        private final HashMap<Integer, Integer> countinuousMissCounts = new HashMap<>();

        public TraceRunner(String inputFile, String outputFile,
                List<Transmitter> ts, List<Receiver> rs, double beta) throws IOException {
            transmitterContinuousMisses
                    = ts.stream().collect(Collectors.toMap(t -> t, t -> 0));
            this.input = Files.lines(Paths.get(inputFile)).iterator();
            this.ts = ts.stream().collect(Collectors.toMap(t -> t.getName(), t -> t));
            this.f = new Field(ts, rs, beta);
//            this.output = new PrintStream(outputFile);

            parseLine();
            Timeline.run();
//            output.flush();
//            output.close();
        }

        public int getLineId() {
            return lineId;
        }

        public int getCount() {
            return count;
        }

        public int getMaxContinuousCount() {
            return maxContinuousCount;
        }

        public HashMap<Integer, Integer> getCountinuousMissCounts() {
            return countinuousMissCounts;
        }

        private void runEvent(Object... params) {
            Transmitter t = (Transmitter) params[0];
            long duration = (long) params[1];
            int lid = (int) params[2];

            f.handleTransmitStart(t);
            Timeline.addEvent(Timeline.nowInUs() + duration, p -> {
                int capture = f.handleTransmitEnd(t).size();
                if (capture > 0) {
                    count++;
                    transmitterContinuousMisses.compute(t, (tx, i) -> {
                        countinuousMissCounts.merge(i, 1, Integer::sum);
                        maxContinuousCount = Math.max(maxContinuousCount, i);
                        return 0;
                    });
                } else {
                    transmitterContinuousMisses.merge(t, 1, Integer::sum);
                }
//                output.printf("%d\t%d\t%d%n", Timeline.nowInUs(), lid, capture);
            });
            parseLine();
        }

        private void parseLine() {
            if (input.hasNext()) {
                String line = input.next();
                String[] parts = line.split("\t");
                assert parts.length == 3;
                Transmitter t = ts.get(parts[0]);
                assert t != null;
                long time = Long.parseLong(parts[1]);
                long duration = Long.parseLong(parts[2]);
                Timeline.addEvent(time, this::runEvent, t, duration, ++lineId);
            }
        }
    }

    public static synchronized void runTrace2(
            List<Transmitter> transmitters, List<Receiver> receivers, double beta,
            String traceFile, String resultFile) throws IOException {
        TraceRunner tr = new TraceRunner(traceFile, resultFile, transmitters, receivers, beta);
        System.out.printf("Lines: %,d, Captures: %,d%n", tr.getLineId(), tr.getCount());
    }
}
