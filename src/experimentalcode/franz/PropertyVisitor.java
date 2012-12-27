package experimentalcode.franz;

import de.lmu.ifi.dbs.paros.graph.Link;
import de.lmu.ifi.dbs.paros.graph.Node;
import java.util.Collection;
import java.util.HashSet;

public class PropertyVisitor<N extends Node<L>, L extends Link> extends GraphVisitor<N, L> {

    private HashSet<String> propertyKeys = new HashSet<String>();

    public Collection<String> getPropertyKeys() {
        return propertyKeys;
    }

    @Override
    protected void processLink(L link) {
    }

    @Override
    protected void processNode(Node<L> node) {
        // extract properties
    }
}
