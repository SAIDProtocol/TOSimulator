package edu.rutgers.winlab.tosimulator.tracerunner;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author jiachen
 */
public class DataHelper {

    private static final Logger LOG = Logger.getLogger(DataHelper.class.getName());

    public static Set<Transmitter> generateRandomTransmitters(
            Random random, int count,
            double xMin, double xMax, double yMin, double yMax) {
        Set<Transmitter> ret = new HashSet<>();
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
                ps.printf("%s\t%f\t%f%n", t.getName(), t.getLocation().getX(), t.getLocation().getY());
            });
        }
    }

    public static Set<Transmitter> readTransmitters(String fileName) throws IOException {
        Set<Transmitter> ret = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                assert parts.length == 3;
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                ret.add(new Transmitter(parts[0], new Point2D.Double(x, y)));
            }
        }
        return ret;
    }

    public static Set<Receiver> generateRandomReceivers(
            Random random, int count,
            double xMin, double xMax, double yMin, double yMax) {
        Set<Receiver> ret = new HashSet<>();
        double xDiff = xMax - xMin, yDiff = yMax - yMin;
        for (int i = 1; i <= count; i++) {
            double x = random.nextDouble() * xDiff + xMin;
            double y = random.nextDouble() * yDiff + yMin;
            ret.add(new Receiver("R" + i, new Point2D.Double(x, y)));
        }
        return ret;
    }

    public static void writeReceivers(Collection<Receiver> transmitters, String fileName) throws IOException {
        try (PrintStream ps = new PrintStream(fileName)) {
            transmitters.forEach(r -> {
                ps.printf("%s\t%f\t%f%n", r.getName(), r.getLocation().getX(), r.getLocation().getY());
            });
        }
    }

    public static Set<Receiver> readReceivers(String fileName) throws IOException {
        Set<Receiver> ret = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                assert parts.length == 3;
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
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
            System.out.println(sendStart);
            System.out.println(nextStart);
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

    private static void runEvent(Object... params) {
        BufferedReader reader = (BufferedReader) params[0];
        PrintStream output = (PrintStream) params[1];
        Map<String, Transmitter> ts = (Map<String, Transmitter>) params[2];
        Field f = (Field) params[3];
        Transmitter t = (Transmitter) params[4];
        long duration = (long) params[5];
        int lineId = (int) params[6];

        f.handleTransmitStart(t);
        Timeline.addEvent(Timeline.nowInUs() + duration, p -> {
            ((PrintStream) p[0]).printf("%d\t%d\t%d%n",
                    Timeline.nowInUs(),
                    (int) p[3],
                    ((Field) p[1]).handleTransmitEnd((Transmitter) p[2]).size());
        }, output, f, t, lineId);
        parseLine(reader, output, ts, f, lineId + 1);
    }

    private static void parseLine(BufferedReader reader, PrintStream output,
            Map<String, Transmitter> ts, Field f, int lineId) {
        try {
            String line = reader.readLine();
            if (line != null) {
                String[] parts = line.split("\t");
                assert parts.length == 3;
                Transmitter t = ts.get(parts[0]);
                assert t != null;
                long time = Long.parseLong(parts[1]);
                long duration = Long.parseLong(parts[2]);
                Timeline.addEvent(time, DataHelper::runEvent,
                        reader, output, ts, f, t, duration, lineId);
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public static void runTrace(
            Set<Transmitter> transmitters, Set<Receiver> receivers, double beta,
            String traceFile, String resultFile) throws IOException {
        Map<String, Transmitter> ts = transmitters.stream().collect(
                Collectors.toMap(t -> t.getName(), t -> t));
        Field f = new Field(transmitters, receivers, beta);
        try (PrintStream ps = new PrintStream(resultFile)) {
            try (BufferedReader reader = new BufferedReader(new FileReader(traceFile))) {
                parseLine(reader, ps, ts, f, 1);
                Timeline.run();
                ps.flush();
            }
        }
    }
}
