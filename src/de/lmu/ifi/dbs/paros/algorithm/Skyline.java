package de.lmu.ifi.dbs.paros.algorithm;

import de.lmu.ifi.dbs.paros.Approximation;
import de.lmu.ifi.dbs.paros.ComplexPath;
import de.lmu.ifi.dbs.paros.PrioritySkyline;
import de.lmu.ifi.dbs.paros.RefPointMinApproximation;
import de.lmu.ifi.dbs.paros.SubSkyline;
import de.lmu.ifi.dbs.paros.graph.WeightedLink;
import de.lmu.ifi.dbs.paros.graph.WeightedNode2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import de.lmu.ifi.dbs.utilities.UpdatablePriorityQueue;

public class Skyline {

    public Skyline() {
    }

    public List<ComplexPath> skyLine(WeightedNode2D node1, WeightedNode2D dest, float[] weights) {
        Approximation minPref = new RefPointMinApproximation(weights);
        HashMap<WeightedNode2D, SubSkyline> nodeTab = new HashMap<WeightedNode2D, SubSkyline>();
        if (node1 == dest) {
            return new LinkedList<ComplexPath>();
        }
        UpdatablePriorityQueue<PrioritySkyline> q = new UpdatablePriorityQueue<PrioritySkyline>(true);

        Iterator<WeightedLink> lIter = node1.getLinks().iterator();
        while (lIter.hasNext()) {
            WeightedLink aktLink = lIter.next();
            ComplexPath aktPath = new ComplexPath(node1, (WeightedNode2D) aktLink.getTarget(), aktLink.getWeights());
            SubSkyline sub = nodeTab.get(aktPath.getLast());
            if (sub == null) {
                sub = new SubSkyline(aktPath, weights, minPref, dest);
                nodeTab.put(aktPath.getLast(), sub);
            } else {
                sub.update(aktPath, weights);
            }
            PrioritySkyline pS = new PrioritySkyline(sub.getPreference(), sub);
            q.insertIfBetter(pS);
        }

        int counter = 0;
        SubSkyline result = null;
        while (!q.isEmpty()) {
            counter++;
            //System.out.println(nodeTab.size() + " nodes referenced"+ q.size());
            SubSkyline aktSL = q.removeFirst().getSkyline();
            // Test ob man schon am Ziel ist
            if (aktSL.getEnd() == dest) {
                continue;
            }
            
            if (result == null) {
                result = nodeTab.get(dest);
            }
            if (result != null) {
                for (ComplexPath lokal : aktSL.getSkyline()) {
                    if (lokal.isProcessed()) {
                        continue;
                    }
                    Iterator<ComplexPath> skyIter = result.getSkyline().iterator();
                    while (skyIter.hasNext()) {
                        ComplexPath skyPath = skyIter.next();
                        Float d = skyPath.dominates(lokal, dest, minPref);
                        if (d == 1) {
                            lokal.setProcessed();
                        }
                    }
                }
            }
            // Erweitern aller Skyline-Pfade auf diesem Pfad
            // Erweiterung aller lokalen Skyline-Pfade
            List<ComplexPath> cand = aktSL.extend(aktSL.getEnd().getLinks());
            // Einf�gen der Kandidatenpfade in lokale Skylines
            // falls sie nicht dominiert werden
            for (ComplexPath aktPath : cand) {
                SubSkyline sl = nodeTab.get(aktPath.getLast());
                if (sl == null) {
                    sl = new SubSkyline(aktPath, weights, minPref, dest);
                    nodeTab.put(aktPath.getLast(), sl);
                } else {
                    sl.update(aktPath, weights);
                }
                PrioritySkyline pS = new PrioritySkyline(sl.getPreference(), sl);
                q.insertIfBetter(pS);
            }
        }

        if (result == null) {
            result = nodeTab.get(dest);
        }
        return result.getSkyline();
    }
}
