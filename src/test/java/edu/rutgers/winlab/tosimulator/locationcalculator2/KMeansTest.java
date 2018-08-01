/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.rutgers.winlab.tosimulator.locationcalculator2;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.Random;
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
public class KMeansTest {

    public KMeansTest() {
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
    public void originalTestMethod() {
        // the test data is four 750-point Gaussian clusters (3000 points in all)
        // created around the vertices of the unit square
        String data = "TestData.csv";
        int numPoints = 3000;
        int dimensions = 2;
        int k = 4;
        double[][] points = new double[numPoints][dimensions];
        Random rand = new Random(0);
        for (int i = 0; i < numPoints; i++) {
            for (int j = 0; j < dimensions; j++) {
                points[i][j] = rand.nextDouble() * 100;
            }
        }
//CSVreader.read(data, numPoints, dimensions);

        // run K-means
        final long startTime = System.currentTimeMillis();
        KMeans clustering = new KMeans.Builder(k, points)
                .iterations(50)
                .pp(true)
                .epsilon(.001)
                .useEpsilon(true)
                .build();
        final long endTime = System.currentTimeMillis();

        // print timing information
        final long elapsed = endTime - startTime;
        System.out.println("Clustering took " + (double) elapsed / 1000 + " seconds");
        System.out.println();

        // get output
        double[][] centroids = clustering.getCentroids();
        double WCSS = clustering.getWCSS();
        // int[] assignment  = kmean.getAssignment();

        // print output
        for (int i = 0; i < k; i++) {
            System.out.println("(" + centroids[i][0] + ", " + centroids[i][1] + ")");
        }
        System.out.println();

        System.out.println("The within-cluster sum-of-squares (WCSS) = " + WCSS);
        System.out.println();

        // write output to CSV
        // CSVwriter.write("filePath", centroids);
    }

    @Test
    public void test1() throws IOException {
        String transmitterFile = "ts_rand_0.txt", receiverFolder = "rs_kmeans/";
        File f = new File(receiverFolder);
        if (!f.exists()) {
            f.mkdirs();
        }
        Calculator.Point[] transmitters = Calculator.readPoints(transmitterFile);
        double[][] points = Stream.of(transmitters).map(t -> new double[]{t.getX(), t.getY()}).toArray(double[][]::new);
        int numPoints = points.length;
        for (int k = 1; k < numPoints; k++) {
            final long startTime = System.currentTimeMillis();
            KMeans clustering = new KMeans.Builder(k, points)
                    .iterations(50)
                    .pp(true)
                    .epsilon(.001)
                    .useEpsilon(true)
                    .build();
            final long endTime = System.currentTimeMillis();
            double[][] centroids = clustering.getCentroids();
            double WCSS = clustering.getWCSS();
            System.out.printf("K=%d\tT=%d\tWCSS=%f%n", k, endTime - startTime, WCSS);

            try (PrintStream ps = new PrintStream(String.format("%s/rs_%d.txt", receiverFolder, k))) {
                int i = 1;
                for (double[] centroid : centroids) {
                    ps.printf("R%d\t%d\t%d%n", i++,
                            Double.doubleToLongBits(centroid[0]),
                            Double.doubleToLongBits(centroid[1]));
                }
                ps.flush();
            }

        }

    }
}
