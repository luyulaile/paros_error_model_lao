package de.lmu.ifi.dbs.paros.graph;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author graf
 */
public class LinkTest {

    public LinkTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testIsOneway() {
        System.out.println("isOneway");
        Node a = new Node(1);
        Node b = new Node(2);
        assertTrue(new Link(a, b, true).isOneway());
        assertFalse(new Link(a, b, false).isOneway());
    }

    @Test
    public void testGetSource() {
        System.out.println("getSource");
        Node a = new Node(1);
        Node b = new Node(2);
        Link instance = new Link(a, b);
        Node expResult = a;
        Node result = instance.getSource();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetTarget_0args() {
        System.out.println("getTarget");
        Node a = new Node(1);
        Node b = new Node(2);
        Link instance = new Link(a, b);
        Node expResult = b;
        Node result = instance.getTarget();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetTarget_GenericType() {
        System.out.println("getTarget");
        Node a = new Node(1);
        Node b = new Node(2);
        Link instance = new Link(a, b);
        assertEquals(b, instance.getTarget(a));
        assertEquals(a, instance.getTarget(b));
    }
}
