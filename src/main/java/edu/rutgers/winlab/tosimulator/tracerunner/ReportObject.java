package edu.rutgers.winlab.tosimulator.tracerunner;

import edu.rutgers.winlab.tosimulator.Tuple1;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author jiachen
 */
public class ReportObject {

    private PrintStream ps;

    private final HashMap<String, Integer> keys = new HashMap<>();
    private final HashMap<Integer, Integer> counts = new HashMap<>();
    private final HashMap<String, Supplier<String>> funcKeys = new HashMap<>();
    private Timer timer = null;

    public PrintStream getWriter() {
        return ps;
    }

    public void setWriter(PrintStream Writer) {
        this.ps = Writer;
    }

    public void setKey(String name, int key) {
        keys.put(name, key);
        counts.put(key, 0);
    }

    public void setKey(String name, Supplier<String> func) {
        funcKeys.put(name, func);
    }

    public int getValue(int key) {
        return counts.get(key);
    }

    public void setValue(int key, int value) {
        counts.put(key, value);
    }

    public int increment(int key, int value) {
        return counts.merge(key, value, Integer::sum);
    }

    public ReportObject() {
        ps = System.out;
    }

    public synchronized void beginReport() {
        assert timer == null;
        timer = new Timer("ReportObject");
        timer.scheduleAtFixedRate(task, 0, 1000);
    }

    public synchronized void endReport() {
        assert timer != null;
        timer.cancel();
        writeContent(ps);
        ps.println();
        timer = null;
    }

    private final TimerTask task = new TimerTask() {
        @Override
        public void run() {
            writeContent(ps);
        }
    };

    public void writeContent(PrintStream ps) {
        ps.print("\r");
        keys.forEach((k, v) -> {
            ps.printf("%s=%d, ", k, counts.get(v));
        });
        funcKeys.forEach((k, v) -> {
            ps.printf("%s=%s, ", k, v.get());
        });
        ps.print("                 \r");
    }
}
