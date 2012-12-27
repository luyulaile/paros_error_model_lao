package de.lmu.ifi.dbs.paros;

import de.lmu.ifi.dbs.paros.graph.WeightedNode2D;

public class EuclidianApproximation implements Approximation {

    @Override
    public float estimate(WeightedNode2D node1, WeightedNode2D node2) {
        float xComp = (node1.getX() - node2.getX());
        xComp *= xComp;
        float yComp = (node1.getY() - node2.getY());
        yComp *= yComp;
        return (float) Math.sqrt(xComp + yComp);
    }

    @Override
    public float estimateX(WeightedNode2D node1, WeightedNode2D node2, int x) {
        if (x == 0) {
            return estimate(node1, node2);
        } else {
            return 0;
        }
    }
}
