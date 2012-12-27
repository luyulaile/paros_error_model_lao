package experimentalcode.franz;

import de.lmu.ifi.dbs.paros.graph.Path;
import de.lmu.ifi.dbs.utilities.Arrays2;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public abstract class AbstractResult implements Result {

    private final Logger log = Logger.getLogger(AbstractResult.class.getName());
    private final Hashtable<Path, double[]> results = new Hashtable<Path, double[]>();
    protected final List<String> units = new ArrayList<String>(3);

    @Override
    public Map<Path, double[]> getResults() {
        return results;
    }

    /**
     * @param path
     * @param weight Weight might be cost according to time or length
     */
    public void addResult(Path path, double[] weight) {
        if (path != null) {
            getResults().put(path, weight);
        } else {
            log.warning("tried to set null-path");
        }
    }

    /**
     * @param path
     * @param weight Weight might be cost according to time or length
     */
    public void addResult(Path path, float[] weight) {
        addResult(path, Arrays2.convertToDouble(weight));
    }

    public Collection<Path> getPaths() {
        return results.keySet();
    }

    public double[] getWeight(Path key) {
        return results.get(key);
    }

    @Override
    public List<String> getUnits() {
        return Collections.unmodifiableList(units);
    }
}
