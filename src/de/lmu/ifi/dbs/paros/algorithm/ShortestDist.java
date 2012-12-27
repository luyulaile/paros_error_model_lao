package de.lmu.ifi.dbs.paros.algorithm;

import de.lmu.ifi.dbs.paros.graph.WeightedAdjacentListGraph;
import de.lmu.ifi.dbs.paros.Approximation;
import de.lmu.ifi.dbs.paros.ComplexPath;
import de.lmu.ifi.dbs.paros.PriorityPath;
import de.lmu.ifi.dbs.paros.graph.WeightedLink;
import de.lmu.ifi.dbs.paros.graph.WeightedNode2D;
import java.util.Iterator;
import de.lmu.ifi.dbs.utilities.UpdatablePriorityQueue;

public class ShortestDist<N extends WeightedNode2D<L>, L extends WeightedLink> {

    private int globalCount = 0;
    private final WeightedAdjacentListGraph graph;

    public ShortestDist(WeightedAdjacentListGraph graph) {
        this.graph = graph;
    }

    public float shortestDist(WeightedNode2D node1, WeightedNode2D dest, float[] weights,
            Approximation minDist) {
        if (node1 == dest) {
            return 0;
        }
        float best = Float.MAX_VALUE;
        ComplexPath bestPath = null;
        UpdatablePriorityQueue<PriorityPath> q = new UpdatablePriorityQueue<PriorityPath>(
                true);
        Iterator<WeightedLink> lIter = node1.getLinks().iterator();
        while (lIter.hasNext()) {
            WeightedLink aktLink = lIter.next();
            ComplexPath aktPath = new ComplexPath(node1, (WeightedNode2D) aktLink.getTarget(), aktLink.getWeights());
            Float ctt = aktPath.prefVal(weights);
            if(aktLink.getTarget().getRefDist()!=null)
            	ctt+= minDist.estimate((WeightedNode2D) aktLink.getTarget(), dest);
            PriorityPath p = new PriorityPath(ctt, aktPath);
            q.insertIfBetter(p);
        }

        while (!q.isEmpty() && best > q.firstValue()) {
        	Float oldPrio = (float) q.firstValue();
            PriorityPath<ComplexPath> aktPath = q.removeFirst();
            globalCount++;
            // System.out.println("Extending path to " + aktPath.path.end.getName() +
            // " with cost " + aktPath.path.cost[0] + " " +
            // aktPath.path.history);
            if (aktPath.getPath().getLast() == dest) {
                if (aktPath.getPriority() < best) {
                    best = (float) aktPath.getPriority();
                    bestPath = aktPath.getPath();
                    // TODO hier break?
                }
            }
            lIter = aktPath.getPath().getLast().getLinks().iterator();
            while (lIter.hasNext()) {
                WeightedLink aktLink = lIter.next();
                if(aktPath.getPath().getFirst().equals(aktLink.getTarget()))
                	continue;
                ComplexPath nPath = new ComplexPath(aktPath.getPath(),aktLink);
                Float nPathCost = nPath.prefVal(weights);
                if(aktLink.getTarget().getRefDist()!=null)
                	nPathCost += minDist.estimate(nPath.getLast(), dest);
                nPathCost = Math.max(nPathCost,oldPrio);
                
                if ((aktPath.getPath()).prefVal(weights) >= nPathCost) {
                    System.out.println((aktPath.getPath()).prefVal(weights) + " <> "
                            + nPathCost);
                    System.out.println(nPath);
                }
                PriorityPath pPath = new PriorityPath(nPathCost, nPath);
                q.insertIfBetter(pPath);
            }
        }
        return best;
    }
    
    
}
