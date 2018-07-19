/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.rutgers.winlab.tosimulator.tracerunner;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jiachen
 */
public class DataHelperTest {

    public DataHelperTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

//    @Test
    public void test1() throws IOException {
        double xMin = 0, xMax = 100, yMin = 0, yMax = 100;
        int transmitterCount = 100, receiverCount = 109;
        long transmissionTime = 50 * Timeline.MS;
        long startDiff = 50 * Timeline.MS, repeatDuration = startDiff * transmitterCount;
        int repeatTimes = 20000;
        long driftStandardDeviation = 1500 * Timeline.MS;
        Random rand = new Random(0);
        String tsFile = "ts_rand_0.txt", rsFile = "rs_rand_0.txt", traceFile = "trace_0.txt";

        DataHelper.writeTransmitters(
                DataHelper.generateRandomTransmitters(rand, transmitterCount, xMin, xMax, yMin, yMax),
                tsFile);
        List<Transmitter> transmitters = DataHelper.readTransmitters(tsFile);
        transmitters.forEach(t -> System.out.printf("%s\t%f\t%f%n", t.getName(), t.getLocation().getX(), t.getLocation().getY()));

        System.out.println("=======================");

        DataHelper.writeReceivers(
                DataHelper.generateRandomReceivers(rand, receiverCount, xMin, xMax, yMin, yMax),
                rsFile);
        List<Receiver> receivers = DataHelper.readReceivers(rsFile);
        receivers.forEach(t -> System.out.printf("%s\t%f\t%f%n", t.getName(), t.getLocation().getX(), t.getLocation().getY()));

        System.out.println("=======================");

        AtomicLong al = new AtomicLong(0);
        Map<Transmitter, Long> startTimes
                = transmitters.stream().collect(Collectors.toMap(t -> t, t -> al.getAndAdd(startDiff)));
        DataHelper.generateTrace(
                rand,
                startTimes, transmissionTime,
                repeatDuration, repeatTimes, driftStandardDeviation, traceFile);

    }

//    @Test
    public void test4() throws IOException {
        String rsFile = "rs_ridiculous.txt";

        DataHelper.writeReceivers(Arrays.asList(
                new Receiver("R1", new Point2D.Double(50, 5000))
        ), rsFile);
    }

    @Test
    public void test2() throws IOException {
        String tsFile = "ts_rand_0.txt", rsFile = "rs_tmp_2.txt", traceFile = "trace_0.txt";
//        String tsFile = "ts_rand_0.txt", rsFile = "rs_rand_0.txt", traceFile = "trace_0.txt";
//        String tsFile = "ts_rand_0.txt", rsFile = "rs_ridiculous.txt", traceFile = "trace_0.txt";
//        String resultFile = "result.txt";
        int receiverCountStart = 1, receiverCountEnd = 109;
//        int receiverCount = 3;
        double beta = 0.5;

        List<Transmitter> transmitters = DataHelper.readTransmitters(tsFile);
//        transmitters.forEach(t -> System.out.printf("%s\t%f\t%f%n", t.getName(), t.getLocation().getX(), t.getLocation().getY()));
        List<Receiver> receivers = DataHelper.readReceivers(rsFile);

        HashMap<Integer, HashMap<Integer, Integer>> receiverCountContinuousMisses = new HashMap<>();
        int maxContinuousMisses = 0;

        System.out.println("TO=======================");
        System.out.println("R\tC\tT (ns)");
        for (int receiverCount = receiverCountStart; receiverCount <= receiverCountEnd; receiverCount++) {
            long t1 = System.nanoTime();
            List<Receiver> currReceivers = new ArrayList<>(receivers);
            while (currReceivers.size() > receiverCount) {
                currReceivers.remove(receiverCount);
            }
            DataHelper.TraceRunner tr = new DataHelper.TraceRunner(traceFile, null, transmitters, currReceivers, beta);
            maxContinuousMisses = Integer.max(maxContinuousMisses, tr.getMaxContinuousCount());
            receiverCountContinuousMisses.put(receiverCount, tr.getCountinuousMissCounts());
            long t2 = System.nanoTime();
            System.out.printf("%d\t%,d\t%,d%n", receiverCount, tr.getCount(), t2 - t1);

//            System.out.println("=======================");
//            System.out.printf("Receivers: %d%n", receivers.size());
//            receivers.forEach(t -> System.out.printf("%s\t%f\t%f%n", t.getName(), t.getLocation().getX(), t.getLocation().getY()));
//            System.out.println("=======================");
//            ReportObject ro = new ReportObject();
//            ro.setKey("Time", () -> String.format("%,d", Timeline.nowInUs()));
//            ro.beginReport();
//            DataHelper.runTrace(transmitters, receivers, beta, traceFile, resultFile);
//            DataHelper.runTrace2(transmitters, receivers, beta, traceFile, resultFile);
//            ro.endReport();
        }
        System.out.println("=======================");
        System.out.print("R");
        for (int missCount = 0; missCount <= maxContinuousMisses; missCount++) {
            System.out.printf("\t%d", missCount);
        }
        System.out.println();
        for (int receiverCount = receiverCountStart; receiverCount <= receiverCountEnd; receiverCount++) {
            HashMap<Integer, Integer> continuousMisses = receiverCountContinuousMisses.get(receiverCount);
            System.out.printf("%d", receiverCount);
            for (int missCount = 0; missCount <= maxContinuousMisses; missCount++) {
                Integer i = continuousMisses.get(missCount);
                System.out.printf("\t%d", i == null ? 0 : i);
            }
            System.out.println();
        }

    }

    @Test
    public void test3() throws IOException {
//   String tsFile = "ts_rand_0.txt", rsFile = "rs_tmp_2.txt", traceFile = "trace_0.txt";
        String tsFile = "ts_rand_0.txt", rsFile = "rs_rand_0.txt", traceFile = "trace_0.txt";
//        String tsFile = "ts_rand_0.txt", rsFile = "rs_ridiculous.txt", traceFile = "trace_0.txt";
//        String resultFile = "result.txt";
        int receiverCountStart = 1, receiverCountEnd = 109;
//        int receiverCount = 3;
        double beta = 0.5;

        List<Transmitter> transmitters = DataHelper.readTransmitters(tsFile);
//        transmitters.forEach(t -> System.out.printf("%s\t%f\t%f%n", t.getName(), t.getLocation().getX(), t.getLocation().getY()));
        List<Receiver> receivers = DataHelper.readReceivers(rsFile);

        HashMap<Integer, HashMap<Integer, Integer>> receiverCountContinuousMisses = new HashMap<>();
        int maxContinuousMisses = 0;

        System.out.println("Rand=======================");
        System.out.println("R\tC\tT (ns)");
        for (int receiverCount = receiverCountStart; receiverCount <= receiverCountEnd; receiverCount++) {
            long t1 = System.nanoTime();
            List<Receiver> currReceivers = new ArrayList<>(receivers);
            while (currReceivers.size() > receiverCount) {
                currReceivers.remove(receiverCount);
            }
            DataHelper.TraceRunner tr = new DataHelper.TraceRunner(traceFile, null, transmitters, currReceivers, beta);
            maxContinuousMisses = Integer.max(maxContinuousMisses, tr.getMaxContinuousCount());
            receiverCountContinuousMisses.put(receiverCount, tr.getCountinuousMissCounts());
            long t2 = System.nanoTime();
            System.out.printf("%d\t%,d\t%,d%n", receiverCount, tr.getCount(), t2 - t1);

//            System.out.println("=======================");
//            System.out.printf("Receivers: %d%n", receivers.size());
//            receivers.forEach(t -> System.out.printf("%s\t%f\t%f%n", t.getName(), t.getLocation().getX(), t.getLocation().getY()));
//            System.out.println("=======================");
//            ReportObject ro = new ReportObject();
//            ro.setKey("Time", () -> String.format("%,d", Timeline.nowInUs()));
//            ro.beginReport();
//            DataHelper.runTrace(transmitters, receivers, beta, traceFile, resultFile);
//            DataHelper.runTrace2(transmitters, receivers, beta, traceFile, resultFile);
//            ro.endReport();
        }
        System.out.println("=======================");
        System.out.print("R");
        for (int missCount = 0; missCount <= maxContinuousMisses; missCount++) {
            System.out.printf("\t%d", missCount);
        }
        System.out.println();
        for (int receiverCount = receiverCountStart; receiverCount <= receiverCountEnd; receiverCount++) {
            HashMap<Integer, Integer> continuousMisses = receiverCountContinuousMisses.get(receiverCount);
            System.out.printf("%d", receiverCount);
            for (int missCount = 0; missCount <= maxContinuousMisses; missCount++) {
                Integer i = continuousMisses.get(missCount);
                System.out.printf("\t%d", i == null ? 0 : i);
            }
            System.out.println();
        }

    }
}
