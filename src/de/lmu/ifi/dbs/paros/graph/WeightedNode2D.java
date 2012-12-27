package de.lmu.ifi.dbs.paros.graph;

public class WeightedNode2D<L extends WeightedLink> extends Node<L> {

    private float x;
    private float y;
    private float[][] refDist;

    public WeightedNode2D(int n, float xVal, float yVal, float[][] refs) {
        super(n);
        x = xVal;
        y = yVal;
        refDist = refs;
    }

    public float[][] getRefDist() {
        return refDist;
    }

    public void setRefDist(float[][] refDist) {
        this.refDist = refDist;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

//    public void addLink(WeightedNode2D target, float[] weights) {
//        addLink((L) new WeightedLink(target, weights));
//    }
}
