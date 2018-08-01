package edu.rutgers.winlab.tosimulator.locationcalculator2;

import edu.rutgers.winlab.tosimulator.Tuple2;
import edu.rutgers.winlab.tosimulator.tracerunner.ReportObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author jiachen
 */
public class Calculator {

    public static Point[] readPoints(String fileName) throws IOException {
        return Files.lines(Paths.get(fileName))
                .map(s -> {
                    String[] parts = s.split("\t");
                    assert parts.length == 3;
                    long l1 = Long.parseLong(parts[1]);
                    long l2 = Long.parseLong(parts[2]);
                    double d1 = Double.longBitsToDouble(l1);
                    double d2 = Double.longBitsToDouble(l2);
                    return new Point(d1, d2);
                }).toArray(Point[]::new);
    }

    static {
        setBeta(0.5);
    }

    private static double beta = 0.5, betaSquare = 0.25, oneMinusBetaSquare = 1 - 0.25;

    public static double getBeta() {
        return beta;
    }

    public static void setBeta(double beta) {
        assert beta > 0 && beta < 1;
        Calculator.beta = beta;
        Calculator.betaSquare = beta * beta;
        Calculator.oneMinusBetaSquare = 1 - Calculator.betaSquare;
    }

    public static class Point {

        private final double x, y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getDistanceSquare(Point another) {
            double xDiff = x - another.x;
            double yDiff = y - another.y;
            return xDiff * xDiff + yDiff * yDiff;
        }
    }

    static class CaptureDisc extends Point implements Comparable<CaptureDisc> {

        private final double r, rSquare;

        public CaptureDisc(Point capture, Point ignore) {
            super((capture.x - betaSquare * ignore.x) / oneMinusBetaSquare,
                    (capture.y - betaSquare * ignore.y) / oneMinusBetaSquare);
            assert capture != ignore;
            rSquare = capture.getDistanceSquare(ignore) * betaSquare / oneMinusBetaSquare / oneMinusBetaSquare;
            r = StrictMath.sqrt(rSquare);
        }

        public double getR() {
            return r;
        }

        public double getrSquare() {
            return rSquare;
        }

        public boolean withIn(Point p) {
            return getDistanceSquare(p) <= rSquare;
        }

        @Override
        public int compareTo(CaptureDisc o) {
            int val = Double.compare(getX(), o.getX());
            if (val != 0) {
                return val;
            }
            val = Double.compare(getY(), o.getY());
            if (val != 0) {
                return val;
            }
            return Double.compare(getrSquare(), o.getrSquare());
        }
    }

    public static class ArrayCollector<T> implements Collector<T[], HashSet<T>, HashSet<T>> {

        @Override
        public Supplier<HashSet<T>> supplier() {
            return HashSet<T>::new;
        }

        @Override
        public BiConsumer<HashSet<T>, T[]> accumulator() {
            return (s, a) -> {
//                System.out.printf("ACC: %d, %d%n", s.size(), a.length);
                List<T> tmp = Arrays.asList(a);
                s.addAll(tmp);
            };
        }

        @Override
        public BinaryOperator<HashSet<T>> combiner() {
            return (l, r) -> {
//                System.out.printf("COMB: %d, %d%n", l.size(), r.size());
                l.addAll(r);
                return l;
            };
        }

        @Override
        public Function<HashSet<T>, HashSet<T>> finisher() {
            return a -> a;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.UNORDERED,
                    Collector.Characteristics.IDENTITY_FINISH));
        }
    }

    public static void calculateCoverages(
            Point[] transmitters,
            Point[] receivers) {
        long t1 = System.currentTimeMillis();
        HashSet<CaptureDisc> discs = Stream.of(transmitters)
                .parallel()
                .map(t -> {
                    // t is capture, i is ignore
                    CaptureDisc[] tmp = new CaptureDisc[transmitters.length];
                    for (int i = 0; i < transmitters.length; i++) {
                        if (transmitters[i] == t) {
                            continue;
                        }
                        tmp[i] = new CaptureDisc(t, transmitters[i]);
                    }
                    return tmp;
                })
                .collect(new ArrayCollector<>());
        discs.remove(null);
        long t2 = System.currentTimeMillis();
        System.out.printf("Calculate discs, used: %,dms, size:%,d%n", t2 - t1, discs.size());

        for (int i = 1; i <= receivers.length; i++) {
            Point[] subsetReceivers = new Point[i];
            System.arraycopy(receivers, 0, subsetReceivers, 0, i);
            long count = discs.parallelStream().filter(disc -> {
                boolean ret = false;
                for (Point receiver : subsetReceivers) {
                    if (disc.withIn(receiver)) {
                        ret = true;
                        break;
                    }
                }
                return ret;
            }).count();
            System.out.printf("%d\t%d%n", i, count);
        }
    }

    public static long calculateCoveragesSingle(
            Point[] transmitters,
            Point[] receivers) {
        long t1 = System.currentTimeMillis();
        HashSet<CaptureDisc> discs = Stream.of(transmitters)
                .parallel()
                .map(t -> {
                    // t is capture, i is ignore
                    CaptureDisc[] tmp = new CaptureDisc[transmitters.length];
                    for (int i = 0; i < transmitters.length; i++) {
                        if (transmitters[i] == t) {
                            continue;
                        }
                        tmp[i] = new CaptureDisc(t, transmitters[i]);
                    }
                    return tmp;
                })
                .collect(new ArrayCollector<>());
        discs.remove(null);
        long t2 = System.currentTimeMillis();
//        System.out.printf("Calculate discs, used: %,dms, size:%,d%n", t2 - t1, discs.size());

        long count = discs.parallelStream().filter(disc -> {
            boolean ret = false;
            for (Point receiver : receivers) {
                if (disc.withIn(receiver)) {
                    ret = true;
                    break;
                }
            }
            return ret;
        }).count();
        return count;
    }

    public static Point[] calculateReceiverLocations(
            Point[] transmitters,
            int receiverCount) {
        Point[] ret = new Point[receiverCount];

        // calculate capture disks
        long t1 = System.currentTimeMillis();
        HashSet<CaptureDisc> discs = Stream.of(transmitters)
                .parallel()
                .map(t -> {
                    // t is capture, i is ignore
                    CaptureDisc[] tmp = new CaptureDisc[transmitters.length];
                    for (int i = 0; i < transmitters.length; i++) {
                        if (transmitters[i] == t) {
                            continue;
                        }
                        tmp[i] = new CaptureDisc(t, transmitters[i]);
                    }
                    return tmp;
                })
                .collect(new ArrayCollector<>());
        discs.remove(null);
        long t2 = System.currentTimeMillis();

        System.out.printf("Calculate discs, used: %,dms, size:%,d%n", t2 - t1, discs.size());

        ReportObject ro = new ReportObject();
        AtomicInteger ai = new AtomicInteger(0);
        ro.setKey("Count", () -> String.format("%,d", ai.get()));
        ro.setKey("Discs", () -> String.format("%,d", discs.size()));
        ro.beginReport();
        for (int i = 0; i < receiverCount && !discs.isEmpty(); i++) {
            ai.set(0);
            long t3 = System.currentTimeMillis();
            // calculate intersections
            // calculate solution point capture counts
            // pick maximum
            Tuple2<Point, Set<CaptureDisc>> max = discs
                    .parallelStream()
                    .map(current -> {
                        // center point of the disc
                        Point maxPoint = new Point(current.getX(), current.getY());
                        Point tmpPoint1 = maxPoint;
                        // calculate the capture count
                        long maxDiscCount = discs.stream()
                                .filter(dx -> dx.withIn(tmpPoint1))
                                .count();
                        for (CaptureDisc disc : discs) {
                            // just compare onece, avoid comparing with itself
                            if (disc.compareTo(current) <= 0) {
                                continue;
                            }
                            // calculate intersections
                            double dSquare = current.getDistanceSquare(disc);
                            double d = StrictMath.sqrt(dSquare);
                            // If d > r0 + r1 then there are no solutions, the circles are separate
                            double tmp = current.r + disc.r;
                            if (dSquare > tmp * tmp) {
                                continue;
                            }
                            // If d < |r0 - r1| then there are no solutions because one circle is contained within the other.
                            tmp = current.r - disc.r;
                            if (dSquare < tmp * tmp) {
                                continue;
                            }
                            // If d = 0 and r0 = r1 then the circles are coincident and there are an infinite number of solutions.
                            // skip, captures should not collide in a field
                            double a = (current.rSquare - disc.rSquare + dSquare) / 2 / d;
                            double h = StrictMath.sqrt(current.rSquare - a * a);
                            double p2x = current.getX() + a * (disc.getX() - current.getX()) / d;
                            double p2y = current.getY() + a * (disc.getY() - current.getY()) / d;
                            // intersection 1
                            Point intersection1 = new Point(
                                    p2x + h * (disc.getY() - current.getY()) / d,
                                    p2y - h * (disc.getX() - current.getX()) / d);
                            long intersection1DiscCount = discs.stream()
                                    .filter(dx -> dx.withIn(intersection1))
                                    .count();
                            if (intersection1DiscCount > maxDiscCount) {
                                maxPoint = intersection1;
                                maxDiscCount = intersection1DiscCount;
                            }
                            // intersection 2
                            Point intersection2 = new Point(
                                    p2x - h * (disc.getY() - current.getY()) / d,
                                    p2y - h * (disc.getX() - current.getX()) / d);
                            long intersection2DiscCount = discs.stream()
                                    .filter(dx -> dx.withIn(intersection2))
                                    .count();
                            if (intersection2DiscCount > maxDiscCount) {
                                maxPoint = intersection2;
                                maxDiscCount = intersection2DiscCount;
                            }
                        }
                        ai.incrementAndGet();
                        Point tmpPoint2 = maxPoint;
                        Set<CaptureDisc> maxDiscs = discs.stream()
                                .filter(dx -> dx.withIn(tmpPoint2))
                                .collect(Collectors.toSet());
                        return new Tuple2<>(maxPoint, maxDiscs);
                    })
                    .reduce((s1, s2) -> s1.getV2().size() >= s2.getV2().size() ? s1 : s2)
                    .get();
            // remove capture disks and centers
            ret[i] = max.getV1();
            discs.removeAll(max.getV2());
            long t4 = System.currentTimeMillis();
            System.out.printf("receiver %d, used: %,dms, discs:%,d-%,d=%,d%n",
                    i, t4 - t3, discs.size() + max.getV2().size(), max.getV2().size(), discs.size());
        }
        ro.endReport();
        return ret;
    }
}
