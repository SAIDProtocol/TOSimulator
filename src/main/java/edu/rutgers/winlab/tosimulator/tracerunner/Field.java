package edu.rutgers.winlab.tosimulator.tracerunner;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jiachen
 */
public class Field {

    public enum CaptureStatus {
        CaptureT1, CaptureT2, CaptureNeither
    }

    public static final double EPSILON = 0.00001;
    private final Set<Transmitter> transmitters;
    private final Set<Receiver> receivers;
    private final HashMap<Transmitter, Set<Receiver>> transmittingDevices;
    private final double betaSquare;

    public Field(Collection<Transmitter> transmitters, Collection<Receiver> receivers, double beta) {
        this.transmitters = new HashSet<>(transmitters);
        this.receivers = new HashSet<>(receivers);
        transmittingDevices = new HashMap<>();
        this.betaSquare = beta * beta;
    }

    public void handleTransmitStart(Transmitter t) {
        assert transmitters.contains(t) : "Transmitter not in field!";
        assert !transmittingDevices.containsKey(t) : "Transmitter already transmitting!";

        Set<Receiver> transmitterReceivers = new HashSet<>();
        // compete with other transmitters
        for (Receiver receiver : receivers) {
            boolean canCapture = true;
            for (Map.Entry<Transmitter, Set<Receiver>> entry : transmittingDevices.entrySet()) {
                CaptureStatus stat = canCapture(t, entry.getKey(), receiver);
                switch (stat) {
                    case CaptureT1:
                        entry.getValue().remove(receiver);
                        break;
                    case CaptureT2:
                        canCapture = false;
                        break;
                    case CaptureNeither:
                        entry.getValue().remove(receiver);
                        canCapture = false;
                        break;
                }
            }
            if (canCapture) {
                transmitterReceivers.add(receiver);
            }
        }
        transmittingDevices.put(t, transmitterReceivers);
    }

    public CaptureStatus canCapture(Transmitter t1, Transmitter t2, Receiver receiver) {
        double distanceSquare1 = Transmitter.getDistanceSquare(t1.getLocation(), receiver.getLocation());
        double distanceSquare2 = Transmitter.getDistanceSquare(t2.getLocation(), receiver.getLocation());
        if (distanceSquare1 <= distanceSquare2 * betaSquare + EPSILON) {
            return CaptureStatus.CaptureT1;
        } else if (distanceSquare2 <= distanceSquare1 * betaSquare + EPSILON) {
            return CaptureStatus.CaptureT2;
        } else {
            return CaptureStatus.CaptureNeither;
        }
    }

    public Set<Receiver> handleTransmitEnd(Transmitter t) {
        assert transmitters.contains(t) : "Transmitter not in field!";
        assert transmittingDevices.containsKey(t) : "Transmitter not transmitting!";
        return transmittingDevices.remove(t);
    }
}
