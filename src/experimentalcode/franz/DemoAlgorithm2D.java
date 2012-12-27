package experimentalcode.franz;

import de.lmu.ifi.dbs.paros.graph.Graph;
import de.lmu.ifi.dbs.paros.graph.Path;
import experimentalcode.franz.osm.OSMNode;
import java.util.ArrayList;
import java.util.List;

public class DemoAlgorithm2D extends Algorithm<OSMNode, Graph, Path> {

    private final int allowedAttribs = 2;
    private List<ATTRIBS> selected = new ArrayList(2);

    private enum ATTRIBS {

        DISTANCE, TIME
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
        selected.clear();
        if (list.size() >= 2) {
            selected.add(ATTRIBS.valueOf(list.get(0)));
            selected.add(ATTRIBS.valueOf(list.get(1)));
        }
    }

    @Override
    public List<String> getSelectedAttributes() {
        List<String> l = new ArrayList<String>(2);
        for (ATTRIBS attrib : selected) {
            l.add(attrib.name());
        }
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
    public Simplex2Result getResult() {
        Simplex2Result s2result = new Simplex2Result();
        Path p = new Path(getStartNode(), getEndNode());
        s2result.addResult(p, new double[]{150d, 10d});

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
        s2result.addResult(p2, new double[]{170d, 5d});

        return s2result;
    }

    @Override
    public void run() {
//        if (selected.size() == 0) {
//            System.err.println("not 2 attribs selected");
//        }
        System.out.println("doing nothing");
    }

    @Override
    public String getName() {
        return "Demo algorithm 2D";
    }
}
