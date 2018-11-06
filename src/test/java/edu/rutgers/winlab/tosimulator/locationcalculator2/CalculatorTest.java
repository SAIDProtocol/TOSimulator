/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.rutgers.winlab.tosimulator.locationcalculator2;

import edu.rutgers.winlab.tosimulator.tracerunner.DataHelper;
import edu.rutgers.winlab.tosimulator.tracerunner.Transmitter;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
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

    @Test
    public void generateCircularTransmitters() throws IOException {
        int transmitterCount = 100;
        String transmitterFile = "ts_circle.txt";
        double width = 80, height = 80, centerX = 50, centerY = 50;

        ArrayList<Transmitter> transmitters = new ArrayList<>();
        for (int i = 0; i < transmitterCount; i++) {
            double angle = Math.PI * 2 / transmitterCount * i;
            double x = centerX + Math.cos(angle) * width / 2;
            double y = centerY + Math.sin(angle) * height / 2;
            transmitters.add(new Transmitter("R" + (i + 1), new Point2D.Double(x, y)));
        }
        DataHelper.writeTransmitters(transmitters, transmitterFile);

        Calculator.Point[] ts = Calculator.readPoints(transmitterFile);
        for (Calculator.Point t : ts) {
            System.out.printf("%f\t%f%n", t.getX(), t.getY());
        }
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

//        AtomicInteger ai = new AtomicInteger();
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

//    @Test
    public void test3() throws IOException {
        String transmitterFile = "ts_rand_0.txt", receiverFolder = "rs_kmeans";
        int receiverCount = 99;
        Calculator.Point[] transmitters = Calculator.readPoints(transmitterFile);
        for (int i = 1; i <= receiverCount; i++) {
            Calculator.Point[] receivers = Calculator.readPoints(String.format("%s/rs_%d.txt", receiverFolder, i));
            long count = Calculator.calculateCoveragesSingle(transmitters, receivers);
            System.out.printf("%d\t%d%n", i, count);
        }

//        System.out.printf("Coverage: %d%n", count);
    }
}
