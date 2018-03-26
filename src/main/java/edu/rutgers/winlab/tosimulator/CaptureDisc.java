package edu.rutgers.winlab.tosimulator;

import static edu.rutgers.winlab.tosimulator.Transmitter.getDistance;
import java.awt.geom.Point2D;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jiachen
 */
public class CaptureDisc {

    public static final String BETA_PROP_FILE_NAME = "beta.properties";
    public static final String BETA_PROP_FIELD_NAME = "beta";
    private static final Logger LOG = Logger.getLogger(CaptureDisc.class.getName());
    public static final double DEFAULT_BETA = 0.5;

    public static final double BETA;
    public static final double BETA_SQUARE;
    public static final double ONE_MINUS_BETA_SQUARE;

    static {
        double beta = DEFAULT_BETA;
        Properties p = new Properties();
        try (FileReader fr = new FileReader(BETA_PROP_FILE_NAME)) {
            p.load(fr);
            String bs = p.getProperty(BETA_PROP_FIELD_NAME);
            try {
                beta = Double.parseDouble(bs);
            } catch (NumberFormatException | NullPointerException e) {
                LOG.log(Level.SEVERE, "Cannot load beta");
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Cannot load file: {0}", BETA_PROP_FILE_NAME);
        }
        BETA = beta;
        LOG.log(Level.INFO, "BETA={0}", BETA);
        BETA_SQUARE = BETA * BETA;
        ONE_MINUS_BETA_SQUARE = 1 - BETA_SQUARE;

    }

    private final Transmitter capture;
    private final Transmitter ignore;
    private final Point2D.Double center;
    private final double radius;

    public CaptureDisc(Transmitter capture, Transmitter ignore) {
        this.capture = capture;
        this.ignore = ignore;
        center = new Point2D.Double(
                (capture.getLocation().getX() - BETA_SQUARE * ignore.getLocation().getX()) / ONE_MINUS_BETA_SQUARE,
                (capture.getLocation().getY() - BETA_SQUARE * ignore.getLocation().getY()) / ONE_MINUS_BETA_SQUARE);
        radius = BETA * getDistance(capture.getLocation(), ignore.getLocation()) / ONE_MINUS_BETA_SQUARE;
    }

    public Transmitter getCapture() {
        return capture;
    }

    public Transmitter getIgnore() {
        return ignore;
    }

    public Point2D.Double getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }

    public Intersection[] getInterSections(CaptureDisc another) {
        double d = getDistance(center, another.center);
//      If d > r0 + r1 then there are no solutions, the circles are separate
        if (d > radius + another.radius) {
            return null;
        }
//      If d < |r0 - r1| then there are no solutions because one circle is contained within the other.
        if (d < Math.abs(radius - another.radius)) {
            return null;
        }
//      If d = 0 and r0 = r1 then the circles are coincident and there are an infinite number of solutions.
//      skip, captures should not collide in a field
        double a = (radius * radius - another.radius * another.radius + d * d) / 2 / d;
        double h = Math.sqrt(radius * radius - a * a);
        double p2x = center.x + a * (another.center.x - center.x) / d;
        double p2y = center.y + a * (another.center.y - center.y) / d;
        double p31x = p2x + h * (another.center.y - center.y) / d;
        double p31y = p2y - h * (another.center.x - center.x) / d;
        double p32x = p2x - h * (another.center.y - center.y) / d;
        double p32y = p2y + h * (another.center.x - center.x) / d;

        return new Intersection[]{
            new Intersection(new Point2D.Double(p31x, p31y)),
            new Intersection(new Point2D.Double(p32x, p32y))
        };
    }
}
