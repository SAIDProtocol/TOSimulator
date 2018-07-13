package edu.rutgers.winlab.tosimulator.tracerunner;

import java.awt.geom.Point2D;

/**
 *
 * @author jiachen
 */
public class Receiver {

    private final Point2D.Double location;
    private final String name;

    public Receiver(String name, Point2D.Double location) {
        this.location = location;
        this.name = name;
    }

    public Point2D.Double getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

}
