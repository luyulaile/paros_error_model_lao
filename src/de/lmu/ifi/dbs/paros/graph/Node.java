package de.lmu.ifi.dbs.paros.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Node<L extends Link> implements Serializable{

    transient private Logger log = Logger.getLogger(Node.class.getName());
    private static final long serialVersionUID = 1L;
    public final int name;
    transient private List<L> links;

    public Node(int newName) {
        this.name = newName;
        this.links = new ArrayList<L>(1);
    }

    public List<L> getOutLinks() {
        List<L> out = new ArrayList<L>(links.size());
        for (L link : links) {
            if (!link.isOneway() || link.getSource().equals(this)) {
                out.add(link);
            }
        }
        return out;
    }

    public List<L> getInLinks() {
        List<L> out = new ArrayList<L>(links.size());
        for (L link : links) {
            if (!link.isOneway() || link.getTarget().equals(this)) {
                out.add(link);
            }
        }
        return out;
    }

    public List<L> getLinks() {
        return links;
    }

    public void setLinks(List<L> links) {
        this.links = links;
    }

    public void addLink(L link) {
        if (!links.contains(link)) {
            links.add(link);
        }
    }

    public void removeLink(L link) {
        links.remove(link);
    }

    public int getName() {
        return name;
    }

    public int getDegree() {
        return links.size();
    }

    @Override
    public String toString() {
        return "N" + name;
    }
}
