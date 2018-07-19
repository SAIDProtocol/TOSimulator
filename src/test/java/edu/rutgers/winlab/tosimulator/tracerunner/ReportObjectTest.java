package edu.rutgers.winlab.tosimulator.tracerunner;

import java.sql.Time;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jiachen
 */
public class ReportObjectTest {

    public ReportObjectTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

//    @Test
    public void test1() throws InterruptedException {
        ReportObject ro = new ReportObject();
        ro.setKey("Test1", 1);
        ro.beginReport();
        ro.setKey("Time", () -> new Date().toString());
        int k = 0;
        for (int i = 0; i < 10; i++) {
            assertEquals(k += i, ro.increment(1, i));
            assertEquals(k, ro.getValue(1));
            Thread.sleep(1500);
        }
        ro.endReport();
    }

}
