package experimentalcode.franz;

import de.lmu.ifi.dbs.paros.graph.Path;

public class Simplex1Result extends AbstractResult {

    /**
     * @param path
     * @param weight Weight might be cost according to time or length
     */
    public void addResult(Path path, double weight) {
        addResult(path, new double[]{weight});
    }

    @Override
    public void setUnits(String... unitStrings) {
        if (unitStrings.length != 1) {
            throw new IllegalArgumentException("only 1 unit allowed");
        }
        units.clear();
        for (String s : unitStrings) {
            units.add(s);
        }
    }
}
