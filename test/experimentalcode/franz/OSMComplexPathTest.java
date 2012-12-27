package experimentalcode.franz;

import de.lmu.ifi.dbs.utilities.Arrays2;
import experimentalcode.franz.osm.OSMNode;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class OSMComplexPathTest {

    public OSMComplexPathTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

//    @Test
//    public void testPrefVal() {
//        System.out.println("prefVal");
//        float[] w = null;
//        OSMComplexPath instance = null;
//        float expResult = 0.0F;
//        float result = instance.prefVal(w);
//        assertEquals(expResult, result, 0.0);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    @Test
    public void testReverse() {
        OSMNode a = new OSMNode(1);
        float[] ab = {1f, 2f};
        OSMNode b = new OSMNode(2);
        float[] bc = {2f, 3f};
        OSMNode c = new OSMNode(3);
        float[] cd = {3f, 4f};
        OSMNode d = new OSMNode(4);

        OSMComplexPath p = new OSMComplexPath(a, b, ab);
        p = new OSMComplexPath(p, c, bc);
        p = new OSMComplexPath(p, d, cd);

        OSMComplexPath exp = new OSMComplexPath(d, c, cd);
        exp = new OSMComplexPath(exp, b, bc);
        exp = new OSMComplexPath(exp, a, ab);

        OSMComplexPath result = p.reverse();
        assertEquals(exp.getLength(), result.getLength());
        assertArrayEquals(exp.getCost(), result.getCost());

        List expNodes = exp.getNodes();
        List revNodes = result.getNodes();
        for (int i = 0; i < expNodes.size(); i++) {
            assertEquals(expNodes.get(i), revNodes.get(i));
        }
    }
//    @Test
//    public void testAppend() {
//        System.out.println("append");
//        OSMComplexPath<N, L> other = null;
//        OSMComplexPath instance = null;
//        OSMComplexPath expResult = null;
//        OSMComplexPath result = instance.append(other);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//    @Test
//    public void testDominates() {
//        System.out.println("dominates");
//        OSMComplexPath p2 = null;
//        OSMNode<L> dest = null;
//        OSMApproximation minDist = null;
//        OSMComplexPath instance = null;
//        int expResult = 0;
//        int result = instance.dominates(p2, dest, minDist);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    private static void assertArrayEquals(float[] a, float[] b) {
        for (int i = 0; i < b.length; i++) {
            assertEquals(Arrays2.join(a, "|", 2) + " <> " + Arrays2.join(b, "|", 2),
                    a[i], b[i], Math.ulp(a[i]));
        }
    }
}
