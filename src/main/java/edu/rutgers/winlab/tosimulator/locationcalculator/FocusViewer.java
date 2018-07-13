/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.rutgers.winlab.tosimulator.locationcalculator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTextArea;

/**
 *
 * @author jiachen
 */
public class FocusViewer extends JDialog implements Consumer<List<Object>> {

    private final JTextArea jta = new JTextArea();

    public FocusViewer(JFrame owner) {
        super(owner, "Focus viewer", false);
        init(owner);
    }

    private void init(JFrame owner) {
        add(jta);
        jta.setEnabled(false);
        jta.setBackground(Color.white);
        jta.setDisabledTextColor(Color.black);
        innerAccept(new ArrayList<>());
        setLocationRelativeTo(owner);
        this.setResizable(false);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    @Override
    public void accept(List<Object> l) {
        innerAccept(l);
    }

    private void innerAccept(List<Object> l) {
        StringBuilder interSections = new StringBuilder(),
                transmitters = new StringBuilder(),
                captureDiscs = new StringBuilder();

        l.forEach((object) -> {
            if (object instanceof Transmitter) {
//                System.out.printf("T: %s|", ((Transmitter) object).getName());
                transmitters.append(((Transmitter) object).getName());
                transmitters.append('\n');
            } else if (object instanceof Intersection) {
                Intersection is = (Intersection) object;
//                System.out.print("I: ");
                interSections.append("I:\n");
                is.getCaptureDiscs().forEach(cd -> {
//                    System.out.printf("(%s,%s)", cd.getCapture().getName(), cd.getIgnore().getName());
                    interSections.append("   ");
                    interSections.append('(');
                    interSections.append(cd.getCapture().getName());
                    interSections.append(',');
                    interSections.append(cd.getIgnore().getName());
                    interSections.append(')');
                    interSections.append('\n');
                });
//                System.out.print("|");

            } else if (object instanceof CaptureDisc) {
                CaptureDisc cd = (CaptureDisc) object;
//                System.out.printf("CD: (%s,%s)|", cd.getCapture().getName(), cd.getIgnore().getName());
                captureDiscs.append('(');
                captureDiscs.append(cd.getCapture().getName());
                captureDiscs.append(',');
                captureDiscs.append(cd.getIgnore().getName());
                captureDiscs.append(')');
                captureDiscs.append('\n');
            }
        });
//        System.out.println();
//        if (interSections.length() == 0) {
//            interSections.append('\n');
//        }
//        if (transmitters.length() == 0) {
//            transmitters.append('\n');
//        }
//        if (captureDiscs.length() == 0) {
//            captureDiscs.append('\n');
//        }
        jta.setText(String.format("====Transmitter====%n%s====Intersection====%n%s====Capture Disc====%n%s",
                transmitters.toString(), interSections.toString(), captureDiscs.toString()));
        this.pack();

    }

}
