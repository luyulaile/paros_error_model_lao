package de.lmu.ifi.dbs.paros.graph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class WeightedAdjacentListGraph<N extends WeightedNode2D<L>, L extends WeightedLink>
        extends Graph<N, L> {

    private Logger log = Logger.getLogger(WeightedAdjacentListGraph.class.getName());
    private int numAttributes;

    public WeightedAdjacentListGraph() {
    }

    public int getNumAttributes() {
        return numAttributes;
    }

    public void setNumAttributes(int numAttributes) {
        this.numAttributes = numAttributes;
    }

    protected N addNode(int name, float x, float y) {    	
        N node = (N) new WeightedNode2D<WeightedLink>(name, x, y, null);
        super.addNode(node);
        return node;
    }

//    protected void linkNodes(WeightedNode2D node1, WeightedNode2D node2, float[] weights) {
//        WeightedLink l1 =new WeightedLink(node1, node2, weights);
//        WeightedLink l2 =new WeightedLink(node2, node1, weights);    
//    	node1.addLink(l1);
//        node2.addLink(l2);
//        node1.addLink(l1);
//        node2.addLink(l2);
//    }

    public void writeGraphToFile(String nodeF, String edgeF) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(nodeF));

        for (WeightedNode2D aktNode : getNodes()) {
            String line = "";
            line += aktNode.getName() + " ";
            line += aktNode.getX() + " ";
            line += aktNode.getY() + " ";
            for (int p = 0; p < aktNode.getRefDist().length; p++) {
                for (int r = 0; r < aktNode.getRefDist()[p].length; r++) {
                    line += aktNode.getRefDist()[p][r] + " ";
                }
            }
            out.write(line + "\r\n");
        }
        out.close();

        out = new BufferedWriter(new FileWriter(edgeF));
        int counter = 0;

        for (N aktNode : getNodes()) {
            for (WeightedLink aktL : aktNode.getLinks()) {
                if (aktL.getTarget().getName() < aktNode.getName()) {
                    continue;
                }
                String line = "";
                line += counter + " ";
                counter++;
                line += aktNode.getName() + " ";
                line += aktL.getTarget().getName() + " ";
                for (int w = 0; w < aktL.getWeights().length; w++) {
                    line += aktL.getWeights()[w] + " ";
                }
                out.write(line + "\r\n");
            }
        }
        out.close();
    }

    public void loadGraph(String nodeFile, String edgeFile, int attributes)
            throws IOException {
        numAttributes = attributes;
        BufferedReader in = new BufferedReader(new FileReader(nodeFile));
        String inputline;
        while ((inputline = in.readLine()) != null) {
            StringTokenizer tokenizer = new StringTokenizer(inputline, " ");
            int name = Integer.parseInt(tokenizer.nextToken());
            float x = Float.parseFloat(tokenizer.nextToken());
            float y = Float.parseFloat(tokenizer.nextToken());
            addNode(name, x, y);
        }
        in.close();

        in = new BufferedReader(new FileReader(edgeFile));
        inputline = null;
        while ((inputline = in.readLine()) != null) {
            float[] weights = new float[attributes];
            StringTokenizer tokenizer = new StringTokenizer(inputline, " ");
            String name = tokenizer.nextToken();
            int name1 = Integer.parseInt(tokenizer.nextToken());
            int name2 = Integer.parseInt(tokenizer.nextToken());
            weights[0] = Float.parseFloat(tokenizer.nextToken());
            new WeightedLink(getNode(name1), getNode(name2), weights,false);
        }
        in.close();
    }
}
