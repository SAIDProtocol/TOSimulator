/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.rutgers.winlab.tosimulator.tracerunner;

import edu.rutgers.winlab.tosimulator.Tuple1;
import java.io.IOException;
import java.util.HashMap;
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
        int transmitterCount = 50, receiverCount = 1;
        long transmissionTime = 50 * Timeline.MS, repeatDuration = 55 * 50 * Timeline.MS;
        int repeatTimes = 200;
        long driftStandardDeviation = 50 * Timeline.MS;
        Random rand = new Random(0);
        long startDiff = 55 * Timeline.MS;
        String tsFile = "ts_rand_0.txt", rsFile = "rs_rand_0.txt", traceFile = "trace_0.txt";

        DataHelper.writeTransmitters(
                DataHelper.generateRandomTransmitters(rand, transmitterCount, xMin, xMax, yMin, yMax),
                tsFile);
        Set<Transmitter> transmitters = DataHelper.readTransmitters(tsFile);
        transmitters.forEach(t -> System.out.printf("%s\t%f\t%f%n", t.getName(), t.getLocation().getX(), t.getLocation().getY()));

        System.out.println("=======================");

        DataHelper.writeReceivers(
                DataHelper.generateRandomReceivers(rand, receiverCount, xMin, xMax, yMin, yMax),
                rsFile);
        Set<Receiver> receivers = DataHelper.readReceivers(rsFile);
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

    @Test
    public void test2() throws IOException {
        String tsFile = "ts_rand_0.txt", rsFile = "rs_rand_0.txt", traceFile = "trace_0.txt";
        String resultFile = "result.txt";

        Set<Transmitter> transmitters = DataHelper.readTransmitters(tsFile);
        Set<Receiver> receivers = DataHelper.readReceivers(rsFile);
        transmitters.forEach(t -> System.out.printf("%s\t%f\t%f%n", t.getName(), t.getLocation().getX(), t.getLocation().getY()));
        System.out.println("=======================");
        receivers.forEach(t -> System.out.printf("%s\t%f\t%f%n", t.getName(), t.getLocation().getX(), t.getLocation().getY()));
        System.out.println("=======================");
        ReportObject ro = new ReportObject();
        ro.setKey("Time", () -> String.format("%,d", Timeline.nowInUs()));
        ro.beginReport();
        DataHelper.runTrace(transmitters, receivers, 0.5, traceFile, resultFile);
        ro.endReport();

    }

}
