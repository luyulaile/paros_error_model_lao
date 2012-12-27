package de.lmu.ifi.dbs.paros.algorithm;

import de.lmu.ifi.dbs.paros.Approximation;
import de.lmu.ifi.dbs.paros.ComplexPath;
import de.lmu.ifi.dbs.paros.RefPointMinApproximation;
import de.lmu.ifi.dbs.paros.graph.WeightedLink;
import de.lmu.ifi.dbs.paros.graph.WeightedNode2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import de.lmu.ifi.dbs.utilities.PriorityQueue;

public class NaiveSkyline {

    public NaiveSkyline() {
    }

    public List<ComplexPath> naiveSkyline(WeightedNode2D node1, WeightedNode2D dest, float[] weights) {
        Approximation minPref = new RefPointMinApproximation(weights);
        List<ComplexPath> skyline = new ArrayList<ComplexPath>();
        PriorityQueue<ComplexPath> q = new PriorityQueue<ComplexPath>();

        HashMap<WeightedNode2D, String> nodeCounter = new HashMap<WeightedNode2D, String>();
        Iterator<WeightedLink> lIter = node1.getLinks().iterator();
        while (lIter.hasNext()) {
            WeightedLink aktLink = lIter.next();
            ComplexPath aktPath = new ComplexPath(node1, (WeightedNode2D) aktLink.getTarget(), aktLink.getWeights());
            Float ctt = aktPath.prefVal(weights) + minPref.estimate((WeightedNode2D) aktLink.getTarget(), dest);
            q.add(ctt, aktPath);
            nodeCounter.put(aktPath.getLast(), "");
        }
        int counter = 0;
        while (!q.isEmpty()) {
            // if(counter%10000 ==0){
            //System.out.println("referenced nodes " + nodeCounter.size() + " queue " + q.size());
            // skyline.size() +" " + " queuesize " + q.size());
            // listSkylines(skyline);
            // }
            counter++;
            float priority = (float) q.firstPriority();
            ComplexPath path = q.removeFirst();

            // globalCount++;
            // System.out.println(counter + " : skylineEntries: " +
            // skyline.size() + " QueueLength " + q.size() );
            Iterator<ComplexPath> skyIter = skyline.iterator();
            boolean dominated = false;
            List<ComplexPath> delList = new ArrayList<ComplexPath>();
            while (skyIter.hasNext()) {
                ComplexPath skyPath = skyIter.next();
                Float d = skyPath.dominates(path, dest, minPref);
                if (d == 1) {
                    dominated = true;
                    break;
                }
                if (d == -1) {
                    delList.add(skyPath);
                }
            }
            if (dominated) {
                continue;
            }
            for (Iterator<ComplexPath> pIter = delList.iterator(); pIter.hasNext();) {
                ComplexPath p = pIter.next();
                skyline.remove(p);
            }
            if (dest == path.getLast()) {
                skyline.add(path);
                continue;
            }

            lIter = path.getLast().getLinks().iterator();
            while (lIter.hasNext()) {
                WeightedLink aktLink = lIter.next();
                if (path.contains((WeightedNode2D) aktLink.getTarget())) {
                    continue;
                }
                ComplexPath nPath = new ComplexPath(path ,aktLink);
                Float nPathCost = nPath.prefVal(weights)
                        + minPref.estimate(nPath.getLast(), dest);
                q.add(nPathCost, nPath);
                nodeCounter.put(nPath.getLast(), "");
            }
        }

        return skyline;
    }
}
