package experimentalcode.franz;

import de.lmu.ifi.dbs.paros.graph.Graph;
import de.lmu.ifi.dbs.paros.graph.Path;
import experimentalcode.franz.osm.OSMNode;
import java.util.ArrayList;
import java.util.List;

public class DemoAlgorithm1D extends Algorithm<OSMNode, Graph, Path> {

    private final int allowedAttribs = 1;
    private ATTRIBS selected = null;

    private enum ATTRIBS {

        STRAIGHT, DETOUR
    }

    @Override
    public List<String> getAttributes() {
        List<String> l = new ArrayList<String>();
        for (ATTRIBS att : ATTRIBS.values()) {
            l.add(att.name());
        }
        return l;
    }

    @Override
    public void setAttributes(List<String> list) {
        if (list.size() != 1) {
            throw new IllegalArgumentException("Illegal size of attributelist");
        }
        selected = ATTRIBS.valueOf(list.get(0));
    }

    @Override
    public List<String> getSelectedAttributes() {
        List<String> l = new ArrayList<String>(1);
        l.add(selected.name());
        return l;
    }

    @Override
    public int getNumAllowedAttrbutes() {
        return allowedAttribs;
    }

    @Override
    public Statistics getStatistics() {
        Statistics stats = new Statistics();
        stats.put("Runtime", "60");
        stats.put("Nodes visited", "15000");
        stats.put("Paths in Queue", "100");
        return stats;
    }

    @Override
    public Simplex1Result getResult() {
        Simplex1Result s1result = new Simplex1Result();
        Path p1 = new Path(getStartNode(), getEndNode());
        s1result.addResult(p1, 150d);

        OSMNode start = getStartNode();
        OSMNode end = getEndNode();
        OSMNode intermediate = new OSMNode(-1);
        double lat = (start.getLat() + end.getLat()) / 2;
        double lon = (start.getLon() + end.getLon()) / 2;
        lat += (start.getLat() - end.getLat()) * 0.3;
        intermediate.setLat(lat);
        intermediate.setLon(lon);

        Path p2 = new Path(start, intermediate);
        p2 = new Path(p2, end);
        s1result.addResult(p2, 170d);

        return s1result;
    }

    @Override
    public void run() {
        System.out.println("doing nothing");
    }

    @Override
    public String getName() {
        return "Demo algorithm 1D";
    }
}
