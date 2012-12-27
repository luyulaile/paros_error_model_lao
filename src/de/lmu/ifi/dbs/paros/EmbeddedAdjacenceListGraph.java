package de.lmu.ifi.dbs.paros;

import de.lmu.ifi.dbs.paros.graph.WeightedAdjacentListGraph;
import de.lmu.ifi.dbs.paros.graph.WeightedNode2D;
import de.lmu.ifi.dbs.paros.graph.WeightedLink;
import de.lmu.ifi.dbs.paros.algorithm.ShortestDist;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmbeddedAdjacenceListGraph<N extends WeightedNode2D<L>, L extends WeightedLink>
        extends WeightedAdjacentListGraph<N, L> {

    private Logger log = Logger.getLogger(WeightedAdjacentListGraph.class.getName());

    public EmbeddedAdjacenceListGraph() {
    }

    private void addNode(int name, float x, float y, float[][] refs) {
        N node = addNode(name, x, y);
        node.setRefDist(refs);
    }

    public void loadGraph(String nodeFile, String edgeFile, int refNum, int attributes)
            throws IOException {
        setNumAttributes(attributes);
        // read nodes from File
        BufferedReader in = new BufferedReader(new FileReader(nodeFile));
        String inputline;
        int lineCount = 0;
        while ((inputline = in.readLine()) != null) {
            try {
                StringTokenizer tokenizer = new StringTokenizer(inputline, " ");
                int name = Integer.parseInt(tokenizer.nextToken());
                float x = Float.parseFloat(tokenizer.nextToken());
                float y = Float.parseFloat(tokenizer.nextToken());
                float[][] refs = new float[getNumAttributes()][refNum];
                for (int p = 0; p < getNumAttributes(); p++) {
                    for (int r = 0; r < refNum; r++) {
                        refs[p][r] = Float.parseFloat(tokenizer.nextToken());
                    }
                }
                addNode(name, x, y, refs);
                if (lineCount++ % 100 == 0) {
                    System.out.println("Node " + (lineCount - 1));
                }
            } catch (Exception e) {
                log.log(Level.SEVERE, "parser error in line: " + inputline, e);
            }
        }
        in.close();

        // read edges from File
        in = new BufferedReader(new FileReader(edgeFile));
        while ((inputline = in.readLine()) != null) {
            try {
                StringTokenizer tokenizer = new StringTokenizer(inputline, " ");
                String name = tokenizer.nextToken();
                int name1 = Integer.parseInt(tokenizer.nextToken());
                int name2 = Integer.parseInt(tokenizer.nextToken());
                float[] weights = new float[getNumAttributes()];
                for (int p = 0; p < getNumAttributes(); p++) {
                    weights[p] = Float.parseFloat(tokenizer.nextToken());
                    if (weights[p] == 0) {
                        System.out.println("Warp-Edge found.");
                    }
                }
                new WeightedLink(getNode(name1), getNode(name2), weights,false);
                System.out.println("Link " + lineCount++);
            } catch (Exception e) {
                log.log(Level.SEVERE, "parser error in line: " + inputline, e);
            }
        }

        in.close();

    }

  
//    private void buildAllShortestPaths(WeightedNode2D start, int p, int index) {
//        float[] weights = new float[getNumAttributes()];
//        weights[p] = 1;
//        UpdatablePriorityQueue<PriorityPath> q = new UpdatablePriorityQueue<PriorityPath>(true);
//        Iterator<WeightedLink> lIter = start.getLinks().iterator();
//        while (lIter.hasNext()) {
//            WeightedLink aktLink = lIter.next();
//            ComplexPath aktPath = new ComplexPath(start, (WeightedNode2D) aktLink.getTarget(), aktLink.getWeights());
//            float ctt = aktPath.prefVal(weights);
//            PriorityPath pPath = new PriorityPath(ctt, aktPath);
//            q.insertIfBetter(pPath);
//        }
//
//        while (!q.isEmpty()) {
//            float fp = (float) q.firstValue();
//            PriorityPath aktPath = q.removeFirst();
//            // Knotenbereits gefunden
//            if (aktPath.getPath().getLast().getRefDist()[p][index] != 0) {
//                continue;
//            }
//            if (aktPath.getPath().getLast() == start) {
//                continue;
//            }
//            // System.out.println("Error loop");
//            aktPath.getPath().getLast().getRefDist()[p][index] = fp;
//            lIter = aktPath.getPath().getLast().getLinks().iterator();
//            while (lIter.hasNext()) {
//                WeightedLink aktLink = lIter.next();
//                ComplexPath nPath = new ComplexPath(aktPath.getPath(),aktLink);
//                Float nPathCost = nPath.prefVal(weights);
//                PriorityPath pPath = new PriorityPath(nPathCost, nPath);
//                q.insertIfBetter(pPath);
//            }
//        }
//    }

    public void addRandomWeights() {
        for (int i = 0; i < getNumAttributes(); i++) {
            for (N aktNode : getNodes()) {
                System.out.println(aktNode.getName() + " has " + aktNode.getLinks().size() + " links");
                for (WeightedLink link : aktNode.getLinks()) {
                    if (link.getWeights()[i] == 0) {
                        float weight = (float) Math.random();
                        link.getWeights()[i] = weight;
                        for (WeightedLink revLink : link.getTarget().getLinks()) {
                            if (revLink.getTarget() == aktNode) {
                                revLink.getWeights()[i] = weight;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public void buildEmbedding(List<WeightedNode2D> refPoints) {
        // calculate shortest paths
        int count = 0;
        ShortestDist<WeightedNode2D<WeightedLink>, WeightedLink> sd =
                new ShortestDist<WeightedNode2D<WeightedLink>, WeightedLink>(this);

        for (WeightedNode2D node1 : getNodes()) {
            node1.setRefDist(new float[getNumAttributes()][refPoints.size()]);
            for (int refCount = 0; refCount < refPoints.size(); refCount++) {
                WeightedNode2D dest = refPoints.get(refCount);
                for (int x = 0; x < getNumAttributes(); x++) {
                    // generate MinDistEmbedding
                    float[] weights = new float[getNumAttributes()];
                    weights[x] = 1;
                    Approximation minDist = new NoApproximation();
                    float dist = sd.shortestDist(node1, dest, weights, minDist);
                    node1.getRefDist()[x][refCount] = dist;
                }
//                // generate MaxDistEmbedding
//                Float[] weights = new Float[getNumAttributes()];
//                for (int p = 0; p < weights.length; p++) {
//                    weights[p] = 1 / (Float) (weights.length);
//                }
//                Approximation minDist = new NoApproximation();
//                ComplexPath pa = shortestPath(node1, dest, weights, minDist);
//                for (int p = 0; p < pa.cost.length; p++) {
//                    node1.refPaths[p][refCount] = pa.cost[p];
//                }
//                refCount++;
            }
            System.out.println("Progress " + (count++));
        }
    }

    public void verifyEmbedding(List<WeightedNode2D> starts, List<WeightedNode2D> targets) {
        ShortestDist<WeightedNode2D<WeightedLink>, WeightedLink> sd =
                new ShortestDist<WeightedNode2D<WeightedLink>, WeightedLink>(this);
        Iterator<WeightedNode2D> startIter = starts.iterator();
        while (startIter.hasNext()) {
            WeightedNode2D startNode = startIter.next();
            Iterator<WeightedNode2D> destIter = targets.iterator();
            while (destIter.hasNext()) {
                WeightedNode2D destNode = destIter.next();
                for (int x = 0; x < getNumAttributes(); x++) {
                    float[] weights = new float[getNumAttributes()];
                    weights[x] = 1;
                    float d1 = sd.shortestDist(startNode, destNode, weights,
                            new NoApproximation());
                    float d2 = sd.shortestDist(startNode, destNode, weights,
                            new RefPointMinApproximation(weights));
                    if (d1 != d2) {
                        System.out.print("ERROR: ");
                        System.out.println("Path " + startNode.getName() + " -> "
                                + destNode.getName() + " : Dijkstra " + d1
                                + " Lipschitz : " + d2);
                    }
                }
            }
        }
    }

    public List<WeightedNode2D> selectRefPoints(int refNum) {
        List<WeightedNode2D> refNodes = new ArrayList<WeightedNode2D>(refNum);
        int nodeNum = nodeCount();
        assert nodeCount() > 0 : "div by zero expected";
        int nodeId = (int) (Math.random() * nodeNum);
        for (int i = 0; i < refNum; i++) {
            nodeId = (nodeId + (int) (nodeNum / (float) refNum)) % nodeNum;
            System.out.println("Node " + nodeId + " selected as reference node: # " + i);
            refNodes.add(getNode(nodeId));
        }
        return refNodes;
    }
}
