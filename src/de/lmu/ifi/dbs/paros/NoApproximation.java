package de.lmu.ifi.dbs.paros;

import de.lmu.ifi.dbs.paros.graph.WeightedNode2D;

public class NoApproximation implements Approximation {

    @Override
    public float estimate(WeightedNode2D node1, WeightedNode2D node2) {
        return 0;
    }

    @Override
    public float estimateX(WeightedNode2D node1, WeightedNode2D node2, int x) {
        return 0;
    }
}
