/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.rutgers.winlab.tosimulator.locationcalculator;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 *
 * @author jiachen
 */
public class FieldFrame {

    private static final Logger LOG = Logger.getLogger(FieldFrame.class.getName());

    public static Field getCircularField(int count, double centerX, double centerY, double radius) {
        Field f = new Field();
        double angle = Math.PI * 2 / count;
        for (int i = 0; i < count; i++) {
            f.addTransmitter(new Transmitter("T" + (i + 1), new Point2D.Double(centerX + Math.sin(angle * i + angle / 2) * radius, centerY + Math.cos(angle * i + angle / 2) * radius)));
        }
        f.recalculateIntersections();
        return f;
    }

    public static Field getFieldFromFile(String fileName) {
        LOG.log(Level.INFO, "Reading transmitters from file...");
        LinkedList<Transmitter> ts = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            int lineId = 0;
            while ((line = br.readLine()) != null) {
                lineId++;
                String[] parts = line.split("\t");
                if (parts.length < 3) {
                    LOG.log(Level.SEVERE, "Line {0} does not have enough parts: {1}", new Object[]{lineId, line});
                    continue;
                }
                double x, y;
                String name;
                try {
                    name = parts[0];
                    x = Double.parseDouble(parts[1]);
                    y = Double.parseDouble(parts[2]);
                    ts.add(new Transmitter(name, new Point2D.Double(x, y)));
                } catch (NumberFormatException e) {
                    LOG.log(Level.SEVERE, "Cannot parse line {0}: {1}", new Object[]{lineId, line});
                }

            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed in reading file {0}: {1}", new Object[]{fileName, e});
            return null;
        }
        LOG.log(Level.INFO, "Calculating field...");
        Field f = new Field();
        ts.forEach(t -> f.addTransmitter(t));
        f.recalculateIntersections();
        LOG.log(Level.INFO, "Done!");
        return f;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length == 0) {
            System.out.println("usage: java XXX %traceFileName% [v]");
            return;
        }
        final Field f = getFieldFromFile(args[0]);
        Thread t = new Thread(() -> {
            Intersection[] is = new Intersection[f.getIntersections().size()];
            LOG.log(Level.INFO, "Writing intersections, count: {0}", is.length);
            f.getIntersections().toArray(is);
            Arrays.sort(is, (i1, i2) -> Integer.compare(i2.getCaptureDiscs().size(), i1.getCaptureDiscs().size()));
            try (PrintStream ps = new PrintStream("TOSimulator_out.txt")) {
                for (Intersection i : is) {
                    ps.printf("(%f,%f)\t%d\t", i.getLocation().getX(), i.getLocation().getY(), i.getCaptureDiscs().size());
                    i.getCaptureDiscs().forEach(cd -> ps.printf("(%s,%s)", cd.getCapture().getName(), cd.getIgnore().getName()));
                    ps.println();
                }
                LOG.log(Level.INFO, "Writing intersections finished");
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "Cannot write to file TOSimulator_out.txt", ex);
            }
        });
        t.start();

        if (args.length > 1 && args[1].equals("v")) {
            JFrame frame = new JFrame("Transmit-Only Representer");
            FocusViewer fv = new FocusViewer(frame);
            Viewer v = new Viewer();
            v.addFocusChangedHandler(fv);

            v.setField(f);
            frame.add(v);
            frame.setSize(500, 500);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            fv.setVisible(true);

        }
        t.join();

//        Field f = getCircularField(3, 250, 250, 50);
    }
}
