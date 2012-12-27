package de.lmu.ifi.dbs.paros;

import de.lmu.ifi.dbs.paros.graph.WeightedNode2D;

public interface Approximation {

    public float estimate(WeightedNode2D node1, WeightedNode2D node2);

    public float estimateX(WeightedNode2D node1, WeightedNode2D node2, int x);
}
