package de.lmu.ifi.dbs.paros.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Path<P extends Path, N extends Node> implements Iterable<N> {

    private final N end;
    private final N start;
    private final int hops;
    private final P prev;
    private boolean processed = false;

    /**
     * Consructs a new Path from start to end.
     * If prev is null, you might want to use Path(N start, N end, int hops) instead.
     *
     * If you extend an existing path, use Path(P p, N n) instead.
     *
     * @param start 
     * @param end
     * @param hops
     * @param prev
     */
    public Path(N start, N end, int hops, P prev) {
        this.start = start;
        this.end = end;
        this.hops = hops;
        this.prev = prev;
    }

    public Path(N start, N end) {
        this.start = start;
        this.end = end;
        if (start.equals(end)) {
            this.hops = 0;
        } else {
            this.hops = 1;
        }
        this.prev = null;
    }

    public Path(P p, N n) {
        this.start = (N) p.getFirst();
        this.end = n;
        this.hops = p.getLength() + 1;
        this.prev = p;
    }

    public Path(P p, Link l) {
        this(p, (N) l.getTarget(p.getLast()));
//        this.start = (N) p.getFirst();
//        this.end = (N) l.getTarget(p.getLast());
//        this.hops = p.getLength() + 1;
//        this.prev = p;
    }

    public Path(N n, Link l) {
        this.start = n;
        this.end = (N) l.getTarget(start);
        this.hops = 1;
        this.prev = null;
    }

    public int getLength() {
        return hops;
    }

    public N getFirst() {
        return start;
    }

    public N getLast() {
        return end;
    }

    /**
     * @return
     * @deprecated since May 19th 2010. If you need to mark a path processd, use a subclass.
     */
    @Deprecated
    public boolean isProcessed() {
        return processed;
    }

    /**
     * @deprecated since May 19th 2010. If you need to mark a path processd, use a subclass.
     */
    @Deprecated
    public void setProcessed() {
        processed = true;
    }

    public P getParent() {
        return prev;
    }

    /**
     * @return List of nodes from begin to end
     */
    public List<N> getNodes() {
        List<N> nodes = new ArrayList<N>(hops);
        Path<P, N> p = this;
        while (p != null) {
            nodes.add(p.getLast());
            p = p.getParent();
        }
        nodes.add(start);
        Collections.reverse(nodes);
        return nodes;
    }

    /**
     * Reverses the path
     * @param n
     * @return
     */
    public Path<P, N> reverse() {
        List<Path<P, N>> subPaths = new ArrayList<Path<P, N>>(hops);
        Path<P, N> p = this.getParent();
        while (p != null) {
            subPaths.add(p);
            p = p.getParent();
        }
        Path<P, N> result = null;
        for (Path<P, N> aktPath : subPaths) {
            if (result == null) {
                result = new Path(this.getLast(), aktPath.getLast());
            } else {
                result = new Path(result, aktPath.getLast());
            }
        }
        if (result != null) {
            result = new Path(result, this.getFirst());
        } else {
            result = new Path(this.getLast(), this.getFirst());
        }
        return result;
    }

    /**
     * Appends one path to another
     * @param n
     * @return
     */
    public Path<P, N> append(Path<P, N> end) {
        if (this.getLast().equals(end.getFirst())) {
            end = end.reverse();
        }
        if (this.getLast().equals(end.getFirst())) {
            throw new IllegalArgumentException("Paths not connected !");
        }
        P temp = (P) this;
        while (end != null) {
            temp = (P) new Path<P, N>(temp, end.getLast());
            end = end.getParent();
        }
        return (Path<P, N>) temp;
    }

    /**
     * @param link
     * @return true if start AND end node of a link are contained in the path
     */
    public boolean contains(Link<? extends Node> link) {
        return contains(link.getSource()) && contains(link.getTarget());
    }

    public boolean contains(Node n) {
        if (getFirst().equals(n)) {
            return true;
        }

        Path p = this;
        while (p.getLength() > 1) {
            if (p.getLast().equals(n)) {
                return true;
            }
            p = p.getParent();
        }
        assert p.getLength() == 1 : "p.length not 1 but " + p.getLength();
        return p.getLast().equals(n);
    }

    @Override
    public String toString() {
        List<N> nodes = getNodes();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nodes.size(); i++) {
            sb.append(nodes.get(i).toString());
            if (i < nodes.size() - 1) {
                sb.append("-");
            }
        }
        return sb.toString();
    }

    @Override
    public Iterator<N> iterator() {
        return getNodes().iterator();
    }
}
