package de.lmu.ifi.dbs.paros.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;

public class Graph<N extends Node, L extends Link> {

    private HashMap<Integer, N> nodes = new HashMap<Integer, N>();

    public Graph() {
    }

    public void addNode(N node) {
        nodes.put(node.getName(), node);
    }

    public void removeNode(N node) {
        nodes.remove(node.getName());
    }

    public void removeNode(int id) {
        nodes.remove(id);
    }

    public N getNode(int name) {
        return nodes.get(name);
    }

    public Collection<N> getNodes() {
        return nodes.values();
    }

    public int nodeCount() {
        return nodes.size();
    }
}
