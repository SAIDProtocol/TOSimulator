package edu.rutgers.winlab.tosimulator;

import static edu.rutgers.winlab.tosimulator.Transmitter.getDistance;
import static edu.rutgers.winlab.tosimulator.Transmitter.getDistanceSquare;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JComponent;

/**
 *
 * @author jiachen
 */
public class Viewer extends JComponent {

    public static final double POINT_RADIUS = 5;
    public static final double POINT_DIAMETER = POINT_RADIUS * 2;
    public static final double POINT_RADIUS_SQUARE = POINT_RADIUS * POINT_RADIUS;
    public static final Color TRANSMITTER_NORMAL_COLOR = new Color(0, 128, 0);
    public static final Color TRANSMITTER_HIGHLIGHT_COLOR = new Color(255, 255, 0);
    public static final Color INTERSECTION_NORMAL_COLOR = new Color(0, 0, 255);
    public static final Color INTERSECTION_HIGHLIGHT_COLOR = new Color(255, 255, 255);
    public static final Color CAPTURE_DISC_NORMAL_COLOR = new Color(0, 255, 255, 32);
    public static final Color CAPTURE_DISC_HIGHLIGHT_COLOR = new Color(255, 0, 255, 32);

    private final LinkedList<Tuple<Transmitter, Boolean>> transmitters = new LinkedList<>();
    private final LinkedList<Tuple<CaptureDisc, Boolean>> captureDiscs = new LinkedList<>();
    private final LinkedList<Tuple<Intersection, Boolean>> intersections = new LinkedList<>();

    private final LinkedList<Consumer<List<Object>>> focusChangedHandlers = new LinkedList<>();

    public Viewer() {
        this.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int newMouseX = e.getX(), newMouseY = e.getY();

                posX += newMouseX - mouseX;
                posY += newMouseY - mouseY;
                mouseX = newMouseX;
                mouseY = newMouseY;
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                updateFocus();
            }
        });
        this.addMouseWheelListener((MouseWheelEvent e) -> {
            double mx = (mouseX - posX) / scale, my = (mouseY - posY) / scale;
            scale *= e.getWheelRotation() > 0 ? 0.9 : 1.1;
            posX = mouseX - mx * scale;
            posY = mouseY - my * scale;
            repaint();
        });
        this.setDoubleBuffered(true);
    }

    double posX = 0, posY = 0;
    double scale = 1;
    int mouseX = 0, mouseY = 0;

    public void addFocusChangedHandler(Consumer<List<Object>> handler) {
        focusChangedHandlers.add(handler);
    }

    public void removeFocusChangeHandler(Consumer<List<Object>> handler) {
        focusChangedHandlers.remove(handler);
    }

    private void updateFocus() {
        final boolean[] modified = new boolean[]{false};
        Point2D.Double mouse = new Point2D.Double((mouseX - posX) / scale, (mouseY - posY) / scale);
        double sizeSquare = POINT_RADIUS_SQUARE / scale / scale;
        transmitters.forEach(transmitter -> {
            boolean newIsHighlight = getDistanceSquare(transmitter.getV1().getLocation(), mouse) < sizeSquare;
            if (newIsHighlight != transmitter.getV2()) {
                modified[0] = true;
            }
            transmitter.setV2(newIsHighlight);
        });
        captureDiscs.forEach(captureDisc -> {
            boolean newIsHighlight = getDistance(captureDisc.getV1().getCenter(), mouse) < captureDisc.getV1().getRadius();
            if (newIsHighlight != captureDisc.getV2()) {
                modified[0] = true;
            }
            captureDisc.setV2(newIsHighlight);
        });
        intersections.forEach(intersection -> {
            boolean newIsHighlight = getDistanceSquare(intersection.getV1().getLocation(), mouse) < sizeSquare;
            if (newIsHighlight != intersection.getV2()) {
                modified[0] = true;
            }
            intersection.setV2(newIsHighlight);
        });
        if (modified[0]) {
            LinkedList ret = new LinkedList();
            transmitters.stream().filter(v -> v.getV2()).map(v -> v.getV1()).forEach(t -> ret.add(t));
            intersections.stream().filter(v -> v.getV2()).map(v -> v.getV1()).forEach(is -> ret.add(is));
            captureDiscs.stream().filter(v -> v.getV2()).map(v -> v.getV1()).forEach(cd -> ret.add(cd));
            focusChangedHandlers.forEach((focusChangedHandler) -> {
                focusChangedHandler.accept(ret);
            });
            repaint();
        }
    }

    public void setField(Field field) {
        transmitters.clear();
        captureDiscs.clear();
        intersections.clear();
        field.getTms().forEach(t -> transmitters.add(new Tuple<>(t, false)));
        field.getCds().forEach(cd -> captureDiscs.add(new Tuple<>(cd, false)));
        field.getIntersections().forEach(is -> intersections.add(new Tuple<>(is, false)));
    }

    private void drawTransmitter(Graphics2D g, Tuple<Transmitter, Boolean> entry) {
        AffineTransform t = g.getTransform();
        Transmitter tm = entry.getV1();
        Boolean highlight = entry.getV2();
        g.translate(tm.getLocation().getX(), tm.getLocation().getY());
        g.scale(POINT_RADIUS / scale, POINT_RADIUS / scale);
        if (highlight) {
            g.setColor(TRANSMITTER_HIGHLIGHT_COLOR);
            g.fillOval(-1, -1, 2, 2);
            g.setColor(Color.black);
            g.drawOval(-1, -1, 2, 2);
        } else {
            g.setColor(TRANSMITTER_NORMAL_COLOR);
            g.fillOval(-1, -1, 2, 2);
        }
        g.setTransform(t);
    }

    private void drawIntersection(Graphics2D g, Tuple<Intersection, Boolean> entry) {
        AffineTransform t = g.getTransform();
        Intersection is = entry.getV1();
        Boolean highlight = entry.getV2();
        g.translate(is.getLocation().getX(), is.getLocation().getY());
        g.scale(POINT_RADIUS / scale, POINT_RADIUS / scale);
        if (highlight) {
            g.setColor(INTERSECTION_HIGHLIGHT_COLOR);
            g.fillOval(-1, -1, 2, 2);
            g.setColor(Color.black);
            g.drawOval(-1, -1, 2, 2);
        } else {
            g.setColor(INTERSECTION_NORMAL_COLOR);
            g.fillOval(-1, -1, 2, 2);
        }
        g.setTransform(t);
    }

    private void drawCaptureDisc(Graphics2D g, Tuple<CaptureDisc, Boolean> entry) {
        AffineTransform t = g.getTransform();
        CaptureDisc cd = entry.getV1();
        Boolean highlight = entry.getV2();
        Point2D center = cd.getCenter();
        g.translate(center.getX(), center.getY());
        double radius = cd.getRadius();
//        System.out.printf("C: %s, r: %f%n", center, radius);
        g.scale(radius, radius);
        g.setStroke(new BasicStroke((float) (1 / radius / scale)));
        if (highlight) {
            g.setColor(CAPTURE_DISC_HIGHLIGHT_COLOR);
            g.fillOval(-1, -1, 2, 2);
            g.setColor(Color.black);
            g.drawOval(-1, -1, 2, 2);
        } else {
            g.setColor(CAPTURE_DISC_NORMAL_COLOR);
            g.fillOval(-1, -1, 2, 2);
            g.setColor(Color.black);
            g.drawOval(-1, -1, 2, 2);
        }
        g.setTransform(t);
    }

    private static final HashMap<RenderingHints.Key, Object> RENDERING_HINTS = new HashMap<>();

    static {
        RENDERING_HINTS.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        RENDERING_HINTS.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RENDERING_HINTS.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        RENDERING_HINTS.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        RENDERING_HINTS.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        RENDERING_HINTS.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        RENDERING_HINTS.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        RENDERING_HINTS.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        RENDERING_HINTS.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHints(RENDERING_HINTS);
        g2d.translate(posX, posY);
        g2d.scale(scale, scale);

        captureDiscs.forEach(entry -> drawCaptureDisc(g2d, entry));

        g2d.setStroke(new BasicStroke((float) (1 / POINT_RADIUS / scale)));
        transmitters.forEach(entry -> drawTransmitter(g2d, entry));
        intersections.forEach(entry -> drawIntersection(g2d, entry));
    }
}
