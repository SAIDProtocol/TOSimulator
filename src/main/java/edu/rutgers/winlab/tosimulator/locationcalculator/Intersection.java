package edu.rutgers.winlab.tosimulator.locationcalculator;

import java.awt.geom.Point2D;
import java.util.LinkedList;

/**
 *
 * @author jiachen
 */
public class Intersection {

    private final Point2D.Double location;
    private final LinkedList<CaptureDisc> captureDiscs = new LinkedList<>();

    public Intersection(Point2D.Double location) {
        this.location = location;
    }

    public Point2D.Double getLocation() {
        return location;
    }

    public LinkedList<CaptureDisc> getCaptureDiscs() {
        return captureDiscs;
    }

    public boolean verifyCaptureDisc(CaptureDisc disc) {
        if (Transmitter.getDistance(location, disc.getCenter()) < disc.getRadius() + Field.EPSILON) {
            captureDiscs.add(disc);
            return true;
        }
        return false;
    }

}
