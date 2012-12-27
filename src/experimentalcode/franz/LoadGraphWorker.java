package experimentalcode.franz;

import experimentalcode.franz.osm.OSMGraph;
import experimentalcode.franz.osm.OSMLink;
import experimentalcode.franz.osm.OSMNode;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 * Class o asynchronously load nodes and ways into a graph representation
 * 
 * @author graf
 */
class LoadGraphWorker extends SwingWorker<OSMGraph, Void> {

    private final Logger log = Logger.getLogger(LoadGraphWorker.class.getName());
    private final File[] files;
    private File nodes = null;
    private File ways = null;

    public LoadGraphWorker(File... file) {
        this.files = file;
    }

    public void init() {
        for (File f : files) {
            if (!f.exists() || !f.canRead()) {
                throw new IllegalArgumentException(f.getName() + " doesn't exist or is not readable!");
            }
            if (f.getName().equalsIgnoreCase("nodes.txt")) {
                nodes = f;
            }
            if (f.getName().equalsIgnoreCase("ways.txt")) {
                ways = f;
            }
        }

        // check if both files were found
        if (nodes == null) {
            throw new NullPointerException("nodes file must not be null");
        }
        if (ways == null) {
            throw new NullPointerException("ways file must not be null");
        }

    }

    @Override
    protected OSMGraph doInBackground() throws Exception {
        init();
        OSMGraph<OSMNode, OSMLink> graph = null;
        try {
            log.fine("reading graph from " + nodes.getName() + " & " + ways.getName());
            long a = System.currentTimeMillis();
            graph = new OSMGraph<OSMNode, OSMLink>(nodes, ways);
            long b = System.currentTimeMillis();
            log.fine("read graph in " + (b - a) + "ms");
        } catch (Exception e) {
            log.log(Level.SEVERE, "couldn't read graph", e);
        }
        return graph;
    }
}
