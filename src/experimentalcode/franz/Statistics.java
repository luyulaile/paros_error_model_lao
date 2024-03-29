package experimentalcode.franz;

import de.lmu.ifi.dbs.paros.graph.Path;
import experimentalcode.franz.OSMUtils.PATH_ATTRIBUTES;
import experimentalcode.franz.osm.OSMNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Basic statistics class which can be extended to hold any other information.
 * Each single information should be accessible by a getter in order to be
 * displayed after the execution.
 *
 * @author graf
 */
public class Statistics {

    private final Logger log = Logger.getLogger(Statistics.class.getName());
    /** do not keep more than that many nodes */
    public static final int MAX_VISITED_NODES = 500000;
    /** General statistics about teh algorithm like runtime etc. */
    private Hashtable<String, String> map = new Hashtable<String, String>();
    /** Statistics about a certain path */
    private Hashtable<Path, Map<PATH_ATTRIBUTES, String>> pathMap = new Hashtable<Path, Map<PATH_ATTRIBUTES, String>>();
    /** List of visited nodes */
    private List<OSMNode> visitedNodes = new ArrayList<OSMNode>();

    public List<OSMNode> getVisitedNodes() {
        return visitedNodes;
    }

    /**
     * saves the list of visited nodes. The list is limited to a maximum of {@link #MAX_VISITED_NODES}
     * @param nodes
     * @see #MAX_VISITED_NODES
     */
    public void setVisitedNodes(Collection<? extends OSMNode> nodes) {
        this.visitedNodes = new ArrayList<OSMNode>();
        if (nodes.size() > MAX_VISITED_NODES) {
            log.info(nodes.size() + " visited nodes were requested to be stored. Storing only the first " + MAX_VISITED_NODES);
            for (Iterator<? extends OSMNode> it = nodes.iterator(); this.visitedNodes.size() < MAX_VISITED_NODES;) {
                this.visitedNodes.add(it.next());
            }
        } else {
            this.visitedNodes.addAll(nodes);
        }
    }

    /**
     * Add general statistical information about the search (not per path)
     * @param key
     * @param value
     */
    public void put(String key, String value) {
        map.put(key, value);
    }

    public String get(String key) {
        return map.get(key);
    }

    void putPath(Path key, Map<PATH_ATTRIBUTES, String> pathInfos) {
        pathMap.put(key, pathInfos);
    }

    public Map<PATH_ATTRIBUTES, String> getPath(Path p) {
        if (!pathMap.containsKey(p)) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(pathMap.get(p));
    }

    public Map<String, String> getAll() {
        return Collections.unmodifiableMap(map);
    }

    @Override
    public String toString() {
        List<String> keys = new ArrayList<String>();
        keys.addAll(map.keySet());
        StringBuilder out = new StringBuilder(100);
        for (String key : keys) {
            out.append(key + " : " + get(key) + " | ");
        }
        return out.toString();
    }
}
