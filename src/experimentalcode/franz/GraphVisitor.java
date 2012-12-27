package experimentalcode.franz;

import de.lmu.ifi.dbs.paros.graph.Graph;
import de.lmu.ifi.dbs.paros.graph.Link;
import de.lmu.ifi.dbs.paros.graph.Node;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import org.jdesktop.beans.AbstractBean;

public abstract class GraphVisitor<N extends Node<L>, L extends Link> extends AbstractBean {

    public final String EVT_PROGRESS = GraphVisitor.class.getName() + "PROGRESS";
    private final Logger log = Logger.getLogger(GraphVisitor.class.getName());
    private final HashSet<L> processedLinks = new HashSet<L>();

    public void walk(Graph<N, L> graph) {
        Collection<N> nodes = graph.getNodes();
        int total = nodes.size();
        double now = 0;
        for (Node<L> node : nodes) {
            if (Thread.interrupted()) {
                log.info("GraphVisitor interrupted externally.");
                return;
            }
            processNode(node);
            processLinks(node.getLinks());

            firePropertyChange(EVT_PROGRESS, now / total, ++now / total);
        }
    }

    protected void processLinks(List<L> links) {
        for (L link : links) {
            if (!processedLinks.contains(link)) {
                processedLinks.add(link);
                processLink(link);
            }
        }
    }

    protected abstract void processLink(L link);

    protected abstract void processNode(Node<L> node);
}
