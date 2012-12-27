package experimentalcode.franz;

import de.lmu.ifi.dbs.paros.PriorityPath;
import de.lmu.ifi.dbs.paros.graph.Path;
import de.lmu.ifi.dbs.utilities.Arrays2;
import de.lmu.ifi.dbs.utilities.PriorityObjectAdapter;
import de.lmu.ifi.dbs.utilities.UpdatablePriorityQueue;
import experimentalcode.franz.osm.OSMGraph;
import experimentalcode.franz.osm.OSMLink;
import experimentalcode.franz.osm.OSMNode;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO build Embedding class containing the embedding and refpoints
public class OSMSkyline<N extends OSMNode<L>, L extends OSMLink<N>>
             extends Algorithm<N, OSMGraph<N, L>, Path> {

    private static final String STAT_RUNTIME = "Runtime";
    private static final String STAT_NUM_REFPOINTS = "# of reference points";
    private static final String STAT_NUM_VISITED_NODES = "# of visited nodes";
    // -
    // TODO caching issue
    private static SoftReference embCache = new SoftReference(null);
    // -
    private File embeddingFile = new File("embedding.cache.txt");
    private Map<N, NodeWrapper<N>> embedding = new HashMap<N, NodeWrapper<N>>();
    // -
    private final int numberOfRefpoints = 50;
    private final int maxAttributes = 3;
    private List<N> referencePoints = null;
    // ----
    private Logger log = Logger.getLogger(OSMSkyline.class.getName());
    private List<ATTRIBS> selected = new ArrayList<ATTRIBS>();
    private ATTRIBS[] attribRelation = {ATTRIBS.DISTANCE, ATTRIBS.TIME, ATTRIBS.HEIGHT, ATTRIBS.TRAFFIC_LIGHTS};
    private Result result;

    private enum ATTRIBS {

        DISTANCE, TIME, HEIGHT, TRAFFIC_LIGHTS
    }

    public OSMSkyline() {
    }

    @Override
    public int getNumAllowedAttrbutes() {
        return maxAttributes;
    }

    @Override
    public List<String> getAttributes() {
        List<String> attribs = new ArrayList<String>(3);
        for (ATTRIBS att : ATTRIBS.values()) {
            attribs.add(att.name());
        }
        return attribs;
    }

    @Override
    public List<String> getSelectedAttributes() {
        List<String> attribs = new ArrayList<String>(3);
        for (ATTRIBS att : selected) {
            attribs.add(att.name());
        }
        return attribs;
    }

    @Override
    public void setAttributes(List<String> list) {
        selected.clear();
        for (String att : list) {
            selected.add(ATTRIBS.valueOf(att));
        }
    }

    @Override
    public Result getResult() {
        return this.result;
    }

    @Override
    public void run() {
        long a = System.currentTimeMillis();
        this.referencePoints = selectRefPoints(numberOfRefpoints);
        buildEmbedding();
        if (Thread.interrupted()) {
            return;
        }

        final float[] weights = buildWeights();
        List<OSMComplexPath> resultList = skyLine(getStartNode(), getEndNode(), weights);
        if (Thread.interrupted()) {
            return;
        }

        log.fine("found " + resultList.size() + " skylines");
        AbstractResult res = buildResult();
        for (OSMComplexPath aPath : resultList) {
            getStatistics().putPath(aPath, OSMUtils.getPathInfos(aPath.getNodes()));
            res.addResult(aPath, filterCost(aPath.getCost(), weights));
        }
        this.result = res;
        long b = System.currentTimeMillis();

        buildStatistics(resultList);
        getStatistics().put(STAT_RUNTIME, String.format("%,d ms", b - a));
        getStatistics().put(STAT_NUM_REFPOINTS, Integer.toString(numberOfRefpoints));
    }

    /**
     * Reduces full cost array to an array of costs that had a non-zero weight.
     * For example if the cost array was [1,2,3,4] and the weights [0,0,1,1],
     * the output array would be [3,4].
     *
     * @param cost
     * @param weights
     * @return
     */
    private float[] filterCost(float[] cost, float[] weights) {
        float[] out = new float[selected.size()];
        int dst = 0;
        for (int i = 0; i < cost.length; i++) {
            if (weights[i] > 0) {
                out[dst++] = cost[i];
            }
        }
        return out;
    }

    private AbstractResult buildResult() {
        AbstractResult res = null;
        if (selected.size() == 1) {
            res = new Simplex1Result();
        } else if (selected.size() == 2) {
            res = new Simplex2Result();
        } else if (selected.size() == 3) {
            res = new Simplex3Result();
        }
        res.setUnits(buildUnits());
        return res;
    }

    private String[] buildUnits() {
        String[] units = new String[selected.size()];
        int i = 0;
        if (selected.contains(ATTRIBS.DISTANCE)) {
            units[i++] = "km";
        }
        if (selected.contains(ATTRIBS.TIME)) {
            units[i++] = "h";
        }
        if (selected.contains(ATTRIBS.HEIGHT)) {
            units[i++] = "m";
        }
        if (selected.contains(ATTRIBS.TRAFFIC_LIGHTS)) {
            units[i++] = "#";
        }
        return units;
    }

    private float[] buildWeights() {
        float[] weights = new float[attribRelation.length];
        for (int i = 0; i < attribRelation.length; i++) {
            weights[i] = selected.contains(attribRelation[i]) ? 1 : 0;
        }
        return weights;
    }

    public List<OSMComplexPath> skyLine(N start, N destination, float[] weights) {
        if (start == destination) {
            return Collections.EMPTY_LIST;
        }

        OSMApproximation mD = new OSMRefPointMinApproximation(weights, embedding, referencePoints);

        log.fine("get surrounding skyline");
        Map<N, OSMSubSkyline> targets = getSurroundingSkyline(start, destination, weights);
        log.fine("found " + targets.size() + " surrounding skylines");

        // start in targets?
        if (targets.containsKey(start)) {
            OSMSubSkyline<N> sky = targets.get(start);
            List<OSMComplexPath> res = new ArrayList<OSMComplexPath>();
            for (OSMComplexPath path : sky.getSkyline()) {
                res.add(path.reverse());
            }
            return res;
        }

        Map<N, OSMSubSkyline> nodeTab = new HashMap<N, OSMSubSkyline>();
        UpdatablePriorityQueue<PrioritySubSkyline> q = new UpdatablePriorityQueue<PrioritySubSkyline>(true);
        for (L aktLink : start.getOutLinks()) {
            OSMComplexPath<N, ?> aktPath = new OSMComplexPath(start, aktLink, linkToCost(aktLink, weights));
            OSMSubSkyline sub = nodeTab.get(aktPath.getLast());
            if (sub == null) {
                sub = new OSMSubSkyline(aktPath, weights, mD, destination, embedding);
                nodeTab.put((N) aktPath.getLast(), sub);
            } else {
                sub.update(aktPath, weights);
            }
            q.insertIfBetter(new PrioritySubSkyline(sub, sub.getPreference()));
        }

        OSMSubSkyline<N> subSkylineResult = null;
        int count = 1;
        while (!q.isEmpty() && !Thread.interrupted()) {
            OSMSubSkyline<N> aktSL = q.removeFirst().getValue();

            // Test ob man an einem der Ziele ist
            if (targets.containsKey(aktSL.getEnd())) {
                OSMSubSkyline<N> tSL = targets.get(aktSL.getEnd());
                for (OSMComplexPath p1 : aktSL.getSkyline()) {
                    for (OSMComplexPath p2 : tSL.getSkyline()) {
                        OSMComplexPath tmp = p1;
                     
                        if (p2.getLength() > 0 && OSMUtils.isConnectable(p1, p2)) {
                            tmp = p1.append(p2);
                        }
                        if (subSkylineResult == null) {
                            subSkylineResult = new OSMSubSkyline(tmp, weights, mD, destination, embedding);
                        }
                        subSkylineResult.update(tmp, weights);
                    }
                }
                continue;
            }
            if (subSkylineResult != null) {
                for (OSMComplexPath lokal : aktSL.getSkyline()) {
                    if (lokal.isProcessed()) {
                        continue;
                    }
                    boolean allExtDom = true;
                    for (OSMSubSkyline<N> target : targets.values()) {
                        for (OSMComplexPath<N, L> tSLP : target.getSkyline()) {
                            boolean dominated = false;
                            //Compare potential target path to all result paths
                            for (int i = 0; i < subSkylineResult.getSkyline().size() && !dominated; i++) {
                                if (Thread.interrupted()) {
                                    return Collections.EMPTY_LIST;
                                }
                                OSMComplexPath<N, L> resultPath = subSkylineResult.getSkyline().get(i);

                                boolean p1betterp2 = false;
                                boolean p2betterp1 = false;
                                for (int d = 0; d < resultPath.getCost().length && !Thread.interrupted(); d++) {
                                    if (weights[d] == 0) {
                                        continue;
                                    }
                                    float approx = 0;
                                    if (embedding.get(lokal.getLast()) != null) {
                                        approx = lokal.getCost()[d] + mD.estimateX(lokal.getLast(), tSLP.getLast(), d) + tSLP.getCost()[d];
                                    } else {
                                        approx = lokal.getCost()[d] + tSLP.getCost()[d];
                                    }
                                    p2betterp1 |= resultPath.getCost()[d] > approx;
                                    p1betterp2 |= resultPath.getCost()[d] < approx;
                                }
                                if (p1betterp2 && !p2betterp1) {
                                    dominated = true;
                                    break;
                                }
                            }
                            if (!dominated) {
                                allExtDom = false;
                                break;
                            }
                        }
                        if (!allExtDom) {
                            break;
                        }
                    }
                    if (allExtDom) {
                        lokal.setProcessed();
                    }
                }
            }
            List<OSMComplexPath> cand = extend(aktSL, aktSL.getEnd().getOutLinks(), weights);
            for (OSMComplexPath<N, L> aktPath : cand) {
                if (Thread.interrupted()) {
                    return Collections.EMPTY_LIST;
                }

                OSMSubSkyline sl = nodeTab.get(aktPath.getLast());
                if (sl == null) {
                    sl = new OSMSubSkyline(aktPath, weights, mD, destination, embedding);
                    nodeTab.put(aktPath.getLast(), sl);
                } else {
                    sl.update(aktPath, weights);
                }
                q.insertIfBetter(new PrioritySubSkyline(sl, sl.getPreference()));
            }
        }

        if (subSkylineResult == null) {
            subSkylineResult = nodeTab.get(destination);
        }

        // stats
        int visitedNodes = nodeTab.size();
        int allNodes = getGraph().getNodes().size();
        getStatistics().setVisitedNodes(nodeTab.keySet());
        getStatistics().put(STAT_NUM_VISITED_NODES, String.format("%d / %d = %d%%",
                visitedNodes, allNodes, visitedNodes * 100 / allNodes));
        log.fine(nodeTab.size() + " subskylines, iterations: " + (count++) + ", queue size: " + q.size());

        if (subSkylineResult == null) {
            return Collections.EMPTY_LIST;
        }
        return subSkylineResult.getSkyline();
    }

    /**
     * Makes a reverse subskylin-search for the embedded nearest neighbors in each
     * direction and their skylines. If the start is already found search is finisched
     *
     * @param start
     * @param dest
     * @return Map with nearest embedded neighbors
     */
    private Map<N, OSMSubSkyline> getSurroundingSkyline(N start, N dest, float[] weights) {
        OSMApproximation<N> mD = new OSMNoApproximation<N>();
        Map<N, OSMSubSkyline> result = new HashMap<N, OSMSubSkyline>();
        Map<N, OSMSubSkyline> nodeTab = new HashMap<N, OSMSubSkyline>();

        UpdatablePriorityQueue<PrioritySubSkyline> q = new UpdatablePriorityQueue<PrioritySubSkyline>(true);
        UpdatablePriorityQueue<PrioritySubSkyline> q2 = new UpdatablePriorityQueue<PrioritySubSkyline>(true);

        // inits paths with start=end
        if (embedding.get(dest) != null) {
            OSMComplexPath path = new OSMComplexPath(dest, dest, new float[weights.length]);
            OSMSubSkyline subSkyline = new OSMSubSkyline(path, weights, mD, dest, embedding);
            result.put(dest, subSkyline);
            return result;
        }

        for (OSMLink<N> aktLink : dest.getInLinks()) {
            OSMComplexPath aktPath = new OSMComplexPath(dest, aktLink, linkToCost(aktLink, weights));
            OSMSubSkyline sub = nodeTab.get(aktPath.getLast());
            if (sub == null) {
                sub = new OSMSubSkyline(aktPath, weights, mD, dest, embedding);
                nodeTab.put((N) aktPath.getLast(), sub);
            } else {
                sub.update(aktPath, weights);
            }
            q.insertIfBetter(new PrioritySubSkyline(sub, sub.getPreference()));
        }

        while (!q.isEmpty() && !Thread.interrupted()) {
            OSMSubSkyline aktSL = q.removeFirst().getValue();
            if (aktSL.getEnd() == start) {
                result.put(dest, aktSL);
                continue;
            }

            if (embedding.get(aktSL.getEnd()) != null) {
                result.put(dest, aktSL);
                q2.insertIfBetter(new PrioritySubSkyline(aktSL, aktSL.getPreference()));
                continue;
            }

            List<OSMComplexPath> cand = extend(aktSL, aktSL.getEnd().getInLinks(), weights);
            for (OSMComplexPath aktPath : cand) {
                OSMSubSkyline sl = nodeTab.get(aktPath.getLast());
                if (sl == null) {
                    sl = new OSMSubSkyline(aktPath, weights, mD, dest, embedding);
                    nodeTab.put((N) aktPath.getLast(), sl);
                } else {
                    sl.update(aktPath, weights);
                }
                q.insertIfBetter(new PrioritySubSkyline(sl, sl.getPreference()));
            }
        }

        //continue the traversal until all result skylines are complete
        while (!q2.isEmpty()) {
            OSMSubSkyline aktSL = q2.removeFirst().getValue();
            if (aktSL.getEnd() == start) {
                result.put(dest, aktSL);
                continue;
            }

            // Erweitern aller Skyline-Pfade auf diesem Pfad
            // Erweiterung aller lokalen Skyline-Pfade
            List<OSMComplexPath> cand = extend(aktSL, aktSL.getEnd().getInLinks(), weights);
            cand = checkAgainstResult(cand, result, weights);
            // Einf�gen der Kandidatenpfade in lokale Skylines
            // falls sie nicht dominiert werden
            for (OSMComplexPath aktPath : cand) {
                OSMSubSkyline sl = nodeTab.get(aktPath.getLast());
                if (sl == null) {
                    sl = new OSMSubSkyline(aktPath, weights, mD, dest, embedding);
                    nodeTab.put((N) aktPath.getLast(), sl);
                } else {
                    sl.update(aktPath, weights);
                }
                q.insertIfBetter(new PrioritySubSkyline(sl, sl.getPreference()));
            }
        }

        return result;
    }

    /**
     * Checks all candidates whether they are already dominated for result node
     * @param cand candidate paths
     * @param result result subskylines
     * @return reduced candidate set;
     */
    private List<OSMComplexPath> checkAgainstResult(List<OSMComplexPath> cand, Map<N, OSMSubSkyline> result, float[] weights) {
        OSMApproximation minDist = new OSMNoApproximation();
        List<OSMComplexPath> r = new ArrayList<OSMComplexPath>();
        for (OSMComplexPath path : cand) {
            boolean needed = false;
            for (OSMSubSkyline<N> sky : result.values()) {
                boolean dominated = false;
                for (OSMComplexPath<N, L> sPath : sky.getSkyline()) {
                    if (sPath.dominates(path, sPath.getLast(), minDist, weights) == 1) {
                        dominated = true;
                        break;
                    }
                }
                if (!dominated) {
                    needed = true;
                    break;
                }
            }
            if (needed) {
                r.add(path);
            }
        }
        return r;
    }

    public List<OSMComplexPath> extend(OSMSubSkyline<N> skyline, List<L> links, float[] weights) {
        LinkedList<OSMComplexPath> out = new LinkedList();
        for (OSMComplexPath p : skyline.getSkyline()) {
            if (p.isProcessed()) {
                continue;
            }
            p.setProcessed();
            for (OSMLink aLink : links) {
                if (!p.contains(aLink)) {
                    out.add(new OSMComplexPath(p, aLink, linkToCost(aLink, weights)));
                }
            }
        }
        return out;
    }

    private void buildEmbedding() {
        // TODO caching issue
        this.embedding = (Map<N, NodeWrapper<N>>) OSMSkyline.embCache.get();
        if (this.embedding != null) {
            log.fine("using in memory embedding");
            return;
        }

        if (embeddingFile.exists()) { // check for embeddingfile
            log.fine("try to load embedding from file: " + embeddingFile);
            try {
                Embedding emb = new Embedding<N>();
                emb.deserializeFrom(getGraph(), embeddingFile);
                embedding = emb.getEmbedding();
                // TODO check dimensionality of costs
                // TODO check number of reference points

                // TODO caching issue
                OSMSkyline.embCache = new SoftReference(embedding);
                return;
            } catch (IOException ex) {
                log.severe("Loading embedding failed, rebuilding embedding.");
                log.log(Level.SEVERE, null, ex);
            }
        }

        log.fine("start building embedding");
        long a = System.currentTimeMillis();
        this.embedding = new HashMap<N, NodeWrapper<N>>();

        // calculate all reverse shortest paths from all refPoints to all other nodes
        for (int i = 0; i < referencePoints.size(); i++) {
            N refPoint = referencePoints.get(i);
            for (int p = 0; p < attribRelation.length && !Thread.interrupted(); p++) {
                log.fine("calculate embedding " + (i + 1) + "/" + referencePoints.size() + ": " + refPoint + ", weight " + (p + 1) + "/" + attribRelation.length);
                buildAllShortestPaths(refPoint, p);
            }
        }
        long b = System.currentTimeMillis();
        log.fine("embedding built in " + (b - a) + "ms");

        // TODO caching issue
        OSMSkyline.embCache = new SoftReference(embedding);

        try { // save embedding
            log.fine("saving emebdding to file");
            new Embedding(embedding).serializeTo(embeddingFile);
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
    }

    private void buildAllShortestPaths(N startRefNode, int p) {
        float[] weights = new float[attribRelation.length];
        weights[p] = 1;
        UpdatablePriorityQueue<PriorityPath> q = new UpdatablePriorityQueue<PriorityPath>(true);

        for (OSMLink<N> aktLink : startRefNode.getInLinks()) {
            OSMComplexPath aktPath = new OSMComplexPath(startRefNode, aktLink, linkToCost(aktLink, null));
            q.insertIfBetter(new PriorityPath(aktPath.prefVal(weights), aktPath));
        }

        while (!q.isEmpty() && !Thread.interrupted()) {
            final PriorityPath<OSMComplexPath<N, L>> aktPrioPath = q.removeFirst();
            final OSMComplexPath<N, L> path = aktPrioPath.getPath();
            final N lastNode = path.getLast();

            NodeWrapper nodeWrapper = embedding.get(lastNode);
            if (nodeWrapper == null) {
                nodeWrapper = new NodeWrapper(lastNode, attribRelation.length);
                embedding.put(lastNode, nodeWrapper);
            }

            // not yet processed
            if (Float.isNaN(nodeWrapper.getRefDist(p, startRefNode))) {
                // Wert speichern
                nodeWrapper.setRefDist(p, startRefNode, (float) aktPrioPath.getPriority());
                for (OSMLink<N> aktLink : lastNode.getInLinks()) {
                    OSMComplexPath nPath = new OSMComplexPath(path, aktLink, linkToCost(aktLink, null));
                    q.insertIfBetter(new PriorityPath(nPath.prefVal(weights), nPath));
                }
            }
        }
    }

    /**
     * @param l Link
     * @return array of weights
     */
    private float[] linkToCost(OSMLink<N> l, float[] weights) {
        float x = (float) l.getDistance();
        float v = l.getSpeed();
        float t = x / v; // v = x/t
        float z = (float) (l.getAscend() + l.getDescend());
        assert l.getAscend() >= 0 : "ascend < 0: " + l.getAscend();
        assert l.getDescend() >= 0 : "descend < 0: " + l.getDescend();
        float trafficLights = 0;
        {
            String value = l.getSource().getAttr("highway");
            if (value != null && value.equals("traffic_signals")) {
                trafficLights += .5f;
            }
            value = l.getTarget().getAttr("highway");
            if (value != null && value.equals("traffic_signals")) {
                trafficLights += .5f;
            }
        }


        float[] cost = new float[]{x, t, z, trafficLights};
        if (weights != null) {
            Arrays2.mul(cost, weights);
        }
        return cost;
    }

    private List<N> selectRefPoints(int refNum) {
        Collection<N> nodes = getGraph().getNodes();
        List<N> refNodes = new ArrayList<N>(nodes.size());
        refNodes.addAll(nodes);
        Collections.shuffle(refNodes);

        // copy shuffeled list to new array as sublist just link to the original list
        List<N> newRefNodes = new ArrayList<N>();
        for (int i = 0; newRefNodes.size() < refNum && i < nodes.size(); i++) {
            N node = refNodes.get(i); // add only nodes that can be traversed (in AND out)
            if (node.getInLinks().size() > 0 && node.getOutLinks().size() > 0) {
                newRefNodes.add(node);
            }
        }
        return newRefNodes;
    }
}

class PrioritySubSkyline extends PriorityObjectAdapter<OSMSubSkyline> {

    public PrioritySubSkyline(OSMSubSkyline t, double priority) {
        super(t, priority);
    }

    @Override
    public Comparable getKey() {
        return getValue().getEnd().toString();
    }

    @Override
    public String toString() {
        return getKey() + " : " + getPriority();
    }
}
