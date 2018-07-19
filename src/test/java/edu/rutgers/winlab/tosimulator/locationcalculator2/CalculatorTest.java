/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.rutgers.winlab.tosimulator.locationcalculator2;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jiachen
 */
public class CalculatorTest {

    public CalculatorTest() {
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
        String transmitterFile = "ts_rand_0.txt", receiverFile = "rs_tmp_2.txt";
        int rCount = 9900;

        Calculator.Point[] transmitters = Calculator.readPoints(transmitterFile);

        for (int i = 0; i < transmitters.length; i++) {
            System.out.printf("T%d\t%f\t%f%n", i + 1, transmitters[i].getX(), transmitters[i].getY());
        }
        System.out.println("========================================");
        Calculator.Point[] receivers = Calculator.calculateReceiverLocations(transmitters, rCount);
        System.out.println("========================================");

        AtomicInteger ai = new AtomicInteger();

        try (PrintStream ps = new PrintStream(receiverFile)) {
            for (int i = 0; i < receivers.length; i++) {
                if (receivers[i] != null) {
                    ps.printf("R%d\t%d\t%d%n", i + 1,
                            Double.doubleToLongBits(receivers[i].getX()),
                            Double.doubleToLongBits(receivers[i].getY()));
                }
            }
            ps.flush();
        }
//        Files.write(Paths.get(receiverFile),
//                (Iterable<String>) Stream.of(receivers)
//                        .map(r -> String.format("R%d\t%f\t%f", ai.incrementAndGet(), r.getX(), r.getY()))::iterator);
    }

//    @Test
    public void test2() throws IOException {
//        String transmitterFile = "ts_rand_0.txt", receiverFile = "rs_tmp_2.txt";
        String transmitterFile = "ts_rand_0.txt", receiverFile = "rs_rand_0.txt";
        Calculator.Point[] transmitters = Calculator.readPoints(transmitterFile);
        Calculator.Point[] receivers = Calculator.readPoints(receiverFile);
        Calculator.calculateCoverages(transmitters, receivers);
//        System.out.printf("Coverage: %d%n", count);
    }

}
