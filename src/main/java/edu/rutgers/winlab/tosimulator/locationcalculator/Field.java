package edu.rutgers.winlab.tosimulator.locationcalculator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jiachen
 */
public class Field {
    
    private static final Logger LOG = Logger.getLogger(Field.class.getName());
    
    public static final double EPSILON = 0.0000001;
    public static final double EPSILON_SQUARE = EPSILON * EPSILON;
    
    private final LinkedList<Transmitter> tms = new LinkedList<>();
    private final ArrayList<CaptureDisc> cds = new ArrayList<>();
    private final LinkedList<Intersection> intersections = new LinkedList<>();
    
    public void addTransmitter(Transmitter t) {
        LinkedList<CaptureDisc> tmp = new LinkedList<>();
        tms.forEach(tm -> {
            if (isTransmitterCollide(t, tm)) {
                LOG.log(Level.SEVERE, "Transmitters {0} and {1} collide!", new Object[]{t.getName(), tm.getName()});
                throw new IllegalArgumentException("Two transmitters collide!");
            }
            final CaptureDisc cd1 = new CaptureDisc(t, tm);
            final CaptureDisc cd2 = new CaptureDisc(tm, t);
            calculateCD(cds, cd1, cd2);
            calculateCD(tmp, cd1, cd2);
            tmp.add(cd1);
            tmp.add(cd2);
        });
        tms.add(t);
        cds.addAll(tmp);
    }
    
    private void calculateCD(List<CaptureDisc> list, CaptureDisc cd1, CaptureDisc cd2) {
        list.forEach(cd -> {
            if ((isCaptureDiskCollide(cd, cd1) || isCaptureDiskCollide(cd, cd2))) {
                LOG.log(Level.SEVERE, "Capture disc ({0},{1}) and ({2},{3}) collide!", new Object[]{cd.getCapture().getName(), cd.getIgnore().getName(), cd2.getCapture().getName(), cd2.getIgnore().getName()});
                throw new IllegalArgumentException("Two capture discs collide!");
            }
        });
    }
    
    public void recalculateIntersections() {
        LOG.log(Level.INFO, "# of cds: {0}", cds.size());
        intersections.clear();
        for (int i = 0; i < cds.size(); i++) {
            CaptureDisc cd1 = cds.get(i);
            for (int j = 0; j < i; j++) {
                CaptureDisc cd2 = cds.get(j);
                Intersection[] iss = cd1.getInterSections(cd2);
                if (iss == null) {
                    continue;
                }
                addIntersection(iss[0]);
                if (Transmitter.getDistanceSquare(iss[0].getLocation(), iss[1].getLocation()) < EPSILON_SQUARE) {
                    continue;
                }
                addIntersection(iss[1]);
            }
        }
        cds.forEach(cd -> addIntersection(new Intersection(cd.getCenter())));
    }
    
    private void addIntersection(Intersection i) {
//        if (intersections.stream().anyMatch(is -> (Transmitter.getDistanceSquare(is.getLocation(), i.getLocation())) < EPSILON_SQUARE)) {
//            return;
//        }
        intersections.add(i);
        cds.forEach((cd) -> {
            i.verifyCaptureDisc(cd);
        });
    }
    
    public LinkedList<Transmitter> getTms() {
        return tms;
    }
    
    public ArrayList<CaptureDisc> getCds() {
        return cds;
    }
    
    public LinkedList<Intersection> getIntersections() {
        return intersections;
    }
    
    public static boolean isTransmitterCollide(Transmitter t1, Transmitter t2) {
        return Transmitter.getDistanceSquare(t1.getLocation(), t2.getLocation()) < EPSILON_SQUARE;
    }
    
    public static boolean isCaptureDiskCollide(CaptureDisc c1, CaptureDisc c2) {
        if (Math.abs(c1.getRadius() - c2.getRadius()) > EPSILON) {
            return false;
        }
        return Transmitter.getDistanceSquare(c1.getCenter(), c2.getCenter()) < EPSILON_SQUARE;
    }
}
