package experimentalcode.franz;

import de.lmu.ifi.dbs.paros.graph.Node;
import experimentalcode.franz.osm.OSMNode;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OSMSubSkyline<N extends OSMNode<?>>    {

    private final N endNode;
    private final float mintodest;
    private final OSMApproximation minDist;
    private final LinkedList<OSMComplexPath> skyline = new LinkedList<OSMComplexPath>();
    private float preference;

    public OSMSubSkyline(OSMComplexPath<N, ?> first, float[] pref, OSMApproximation mD, N dest, Map<N, NodeWrapper<N>> embedding) {
        minDist = mD;
        if (embedding.containsKey(first.getLast()) && embedding.containsKey(dest)) {
            mintodest = minDist.estimate(first.getLast(), dest);
        } else {
            mintodest = 0;
        }
        preference = first.prefVal(pref) + mintodest;
        skyline.add(first);
        endNode = (N) first.getLast();
    }

    public float getPreference() {
        return preference;
    }

    public N getEnd() {
        return endNode;
    }

    public List<OSMComplexPath> getSkyline() {
        return skyline;
    }

    private boolean samePath(OSMComplexPath a, OSMComplexPath b) {
        if (a.getLength() != b.getLength() || a.getLast() != b.getLast() || a.getFirst() != b.getFirst()) {
            return false;
        }
        for (int x = 0; x < a.getCost().length; x++) {
            if (a.getCost()[x] != b.getCost()[x]) {
                return false;
            }
        }
        return true;
    }

    public void update(OSMComplexPath p, float[] weights) {
        // TODO don't use a delList but iterate with a for-loop by deleting imideately
        // -> don't use a LinkedList but an ArrayList
        List<OSMComplexPath> delList = new LinkedList<OSMComplexPath>();
        for (OSMComplexPath skyPath : skyline) {
            if (samePath(p, skyPath)) {
                return;
            }
            int result = skyPath.dominates(p, endNode, minDist, weights);
            if (result == 1) {
                return;
            } else if (result == -1) {
                delList.add(skyPath);
            }
        }
        Node start = skyline.get(0).getFirst();
        Node end = skyline.get(0).getLast();
        for (OSMComplexPath delPath : delList) {
            skyline.remove(delPath);
        }
        assert p.getFirst().equals(start) : "invalid start node: " + p.getFirst() + " <> " + start;
        assert p.getLast().equals(end) : "invalid end node: " + p.getLast() + " <> " + end;
        skyline.add(p);
        preference = Math.min(preference, p.prefVal(weights) + mintodest);
    }

    @Override
    public String toString() {
        return endNode + "|" + preference + "| skyline size: " + skyline.size();
    }
}