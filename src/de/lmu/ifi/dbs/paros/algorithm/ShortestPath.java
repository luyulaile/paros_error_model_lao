package de.lmu.ifi.dbs.paros.algorithm;

import de.lmu.ifi.dbs.paros.Approximation;
import de.lmu.ifi.dbs.paros.ComplexPath;
import de.lmu.ifi.dbs.paros.PriorityPath;
import de.lmu.ifi.dbs.paros.graph.WeightedLink;
import de.lmu.ifi.dbs.paros.graph.WeightedNode2D;
import java.util.HashMap;
import java.util.Iterator;
import de.lmu.ifi.dbs.utilities.UpdatablePriorityQueue;

public class ShortestPath<N extends WeightedNode2D<L>, L extends WeightedLink> {

    public ShortestPath() {
    }

    public ComplexPath shortestPath(WeightedNode2D node1, WeightedNode2D dest, float[] weights,
            Approximation minDist) {

        if (node1 == dest) {
            return null;
        }
        Float best = Float.MAX_VALUE;
        ComplexPath bestPath = null;
        UpdatablePriorityQueue<PriorityPath> q = new UpdatablePriorityQueue<PriorityPath>(
                true);
        HashMap nodeCounter = new HashMap();
        Iterator<WeightedLink> lIter = node1.getLinks().iterator();
        while (lIter.hasNext()) {
            WeightedLink aktLink = lIter.next();
            ComplexPath aktPath = new ComplexPath(node1, (WeightedNode2D) aktLink.getTarget(), aktLink.getWeights());
            float ctt = aktPath.prefVal(weights) + minDist.estimate((WeightedNode2D) aktLink.getTarget(), dest);
            PriorityPath p = new PriorityPath(ctt, aktPath);
            q.insertIfBetter(p);
        }

        while (!q.isEmpty() && best > q.firstValue()) {
            PriorityPath<ComplexPath> p = q.removeFirst();
            nodeCounter.put(p.getPath().getLast().getName(), "");

            if (p.getPath().getLast() == dest) {
                if (p.getPriority() < best) {
                    best = (float) p.getPriority();
                    bestPath = p.getPath();
                    // TODO hier break?
                }
            }
            lIter = p.getPath().getLast().getLinks().iterator();
            while (lIter.hasNext()) {
                WeightedLink aktLink = lIter.next();
                if (aktLink.getTarget().equals(p.getPath().getFirst())) {
                    continue;
                }
                ComplexPath nPath = new ComplexPath(p.getPath(), aktLink);

                Float nPathCost = nPath.prefVal(weights)
                        + minDist.estimate(nPath.getLast(), dest);
                // if(aktPath.path.prefVal(weights)>= nPathCost){
                // System.out.println(aktPath.path.prefVal(weights)+ " <> " +
                // nPathCost);
                // System.out.println(nPath.history);
                // }
                PriorityPath np = new PriorityPath(nPathCost, nPath);
                q.insertIfBetter(np);
            }
        }

        return bestPath;
    }
}
