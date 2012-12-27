package experimentalcode.franz;

import de.lmu.ifi.dbs.paros.graph.Path;
import de.lmu.ifi.dbs.utilities.MutablePriorityObject;
import java.util.HashMap;
import java.util.List;

import de.lmu.ifi.dbs.utilities.UpdatablePriorityQueue;
import experimentalcode.franz.osm.OSMGraph;
import experimentalcode.franz.osm.OSMLink;
import experimentalcode.franz.osm.OSMNode;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Logger;

public class OSMDijkstra<N extends OSMNode<L>, L extends OSMLink<N>>
      extends Algorithm<N, OSMGraph<N, L>, Path> {

    private static final String STAT_RUNTIME = "Runtime";
    private static final String STAT_NUM_VISITED_NODES = "# of visited nodes";
    // -
    private final Logger log = Logger.getLogger(OSMDijkstra.class.getName());
    private final int allowedAttribs = 1;
    private ATTRIBS selected = null;
    //--
    private final float[] shortestWeights = new float[]{1, 0};
    private final float[] fastestWeights = new float[]{0, 1};
    // Results
    private final Simplex1Result s1 = new Simplex1Result();

    private enum ATTRIBS {

        SHORTEST, FASTEST
    }

    private boolean updateVisited(HashMap<N, Float> visited, N last, float prefVal) {
        Float tabVal = visited.get(last);
        if (tabVal == null) {
            visited.put(last, new Float(prefVal));
            return true;
        } else {
            if (tabVal.floatValue() >= prefVal) {
                visited.put(last, new Float(prefVal));
                return true;
            }
        }
        return false;
    }
/**
 * return a path if exists, or else return null.
 * @param node1
 * @param dest
 * @param weights
 * @return 
 */
    private OSMComplexPath simpleShortestPath(N node1, N dest, float[] weights) {
        if (node1 == dest) {
            return null;
        }

        Float best = Float.MAX_VALUE;
        OSMComplexPath<N, L> bestPath = null;
        HashMap<N, Float> visited = new HashMap<N, Float>();
        UpdatablePriorityQueue<PriorityPath> q = new UpdatablePriorityQueue<PriorityPath>(true);

        //start form node1 (source node), to construct shortest path
        for (OSMLink<N> aktLink : node1.getOutLinks()) {
            OSMComplexPath<N, L> aktPath = new OSMComplexPath(node1, aktLink, linkCost(aktLink));

            float ctt = aktPath.prefVal(weights);
            if (updateVisited(visited, aktPath.getLast(), ctt)) {
                q.insertIfBetter(new PriorityPath(ctt, aktPath));
            }//sort according to the ctt.
        }

        while (!q.isEmpty() && best > q.firstValue() && !Thread.interrupted()) {
            PriorityPath<OSMComplexPath<N, L>> p = q.removeFirst();//remove the smallest value from the priority queue
            // reached target with a (cheaper) path
            if (p.getPath().getLast() == dest && p.getPriority() < best) {
                best = (float) p.getPriority();
                bestPath = p.getPath();
            }

            for (OSMLink<N> aktLink : p.getPath().getLast().getOutLinks()) {
                // catch loop, we don't need to add the used aktLink
                if (p.getPath().contains(aktLink)) {
                    continue;
                }
                //expand the current path by adding all the nodes that are reachable
                OSMComplexPath<N, L> newPath = new OSMComplexPath(p.getPath(), aktLink, linkCost(aktLink));
                Float nPathCost = newPath.prefVal(weights);
                if (updateVisited(visited, newPath.getLast(), nPathCost)) {
                    q.insertIfBetter(new PriorityPath(nPathCost, newPath));
                }
            }
        }//end of while, when queue is empty, or best value < the smallest value in the priority queue.

        int visitedSize = visited.size();
        int totalSize = getGraph().getNodes().size();
        getStatistics().put(STAT_NUM_VISITED_NODES, String.format("%d / %d = %d%%",
                visitedSize, totalSize, visitedSize * 100 / totalSize));
        getStatistics().setVisitedNodes(visited.keySet());
        return bestPath;
    }

    private float[] linkCost(OSMLink link) {
        float[] cost = new float[2];
        float x = (float) link.getDistance();
        float v = link.getSpeed();
        float time = x / v;
        if (time <= 0) {
            log.warning("invalid time!: " + x + "km, " + v + "km/h => " + time + "h");
            time = 1;
        }
        cost[1] = time;
        cost[0] = x;
        return cost;
    }

    @Override
    public Result getResult() {
        return s1;
    }

    @Override
    public void run() {
        long a = System.currentTimeMillis();

        // define weights
        float[] weights = null;
        if (selected.equals(ATTRIBS.SHORTEST)) {
            weights = shortestWeights;
        } else {
            weights = fastestWeights;
        }

        // 
        OSMComplexPath path = simpleShortestPath(getStartNode(), getEndNode(), weights);
        if (path == null) {
            throw new IllegalStateException("couldn't find a path :-(");
        }
        getStatistics().putPath(path, OSMUtils.getPathInfos(path.getNodes()));

        // fill result
        if (selected.equals(ATTRIBS.SHORTEST)) {
            s1.setUnits("km");
            s1.addResult(path, path.getCost()[0]);
        } else {
            s1.setUnits("h");
            s1.addResult(path, path.getCost()[1]);
        }

        // fill statistic
//        getStatistics().put("Length (km)", String.format(Locale.US, "%.3f", path.getCost()[0]));
//        int h = (int) path.getCost()[1];
//        int min = (int) ((path.getCost()[1] * 60) % 60);
//        int sec = (int) ((path.getCost()[1] * 3600) % 60);
//        getStatistics().put("Time (h:min)", String.format("%d:%02d:%02d", h, min, sec));

        long b = System.currentTimeMillis();
        getStatistics().put(STAT_RUNTIME, String.format("%,d ms", b - a));
        buildStatistics(path);
    }

    @Override
    public List<String> getAttributes() {
        List<String> l = new ArrayList<String>();
        for (ATTRIBS att : ATTRIBS.values()) {
            l.add(att.name());
        }
        return l;
    }

    @Override
    public void setAttributes(List<String> list) {
        if (list.size() != 1) {
            throw new IllegalArgumentException("You must choose only one attribute");
        }
        selected = ATTRIBS.valueOf(list.get(0));
    }

    @Override
    public List<String> getSelectedAttributes() {
        List<String> l = new ArrayList<String>(1);
        l.add(selected.name());
        return l;
    }

    @Override
    public int getNumAllowedAttrbutes() {
        return allowedAttribs;
    }

    class PriorityPath<P extends Path> implements MutablePriorityObject<P> {

        private float prio;
        private final P path;

        public PriorityPath(float p, P pa) {
            prio = p;
            path = pa;

        }

        @Override
        public Comparable getKey() {
            return path.getLast().getName();
        }

        @Override
        public double getPriority() {
            return prio;
        }

        @Override
        public void setPriority(double newPriority) {
            prio = (float) newPriority;
        }

        public P getPath() {
            return path;
        }

        @Override
        public P getValue() {
            return path;
        }

        @Override
        public String toString() {
            return prio + " / " + path.toString();
        }
    }
}

