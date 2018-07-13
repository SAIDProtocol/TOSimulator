package edu.rutgers.winlab.tosimulator.locationcalculator;

import java.awt.geom.Point2D;

/**
 *
 * @author jiachen
 */
public class Transmitter {

    private final Point2D.Double location;
    private final String name;

    public Transmitter(String name, Point2D.Double location) {
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

//    public void setName(String name) {
//        this.name = name;
//    }

    public Point2D.Double getLocation() {
        return location;
    }

//    public void setLocation(Point2D.Double location) {
//        this.location = location;
//    }

    public static double getDistance(Point2D.Double p1, Point2D.Double p2) {
        return Math.sqrt(getDistanceSquare(p1, p2));
    }

    public static double getDistanceSquare(Point2D.Double p1, Point2D.Double p2) {
        double d1 = p1.getX() - p2.getX(), d2 = p1.getY() - p2.getY();
        return d1 * d1 + d2 * d2;
    }

}
