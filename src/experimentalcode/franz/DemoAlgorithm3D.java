package experimentalcode.franz;

import de.lmu.ifi.dbs.paros.graph.Graph;
import de.lmu.ifi.dbs.paros.graph.Path;
import experimentalcode.franz.osm.OSMNode;
import java.util.ArrayList;
import java.util.List;

public class DemoAlgorithm3D extends Algorithm<OSMNode, Graph, Path> {

    private final int allowedAttribs = 3;
    private List<ATTRIBS> selected = new ArrayList(3);

    private enum ATTRIBS {

        DISTANCE, SPEED, HEIGHT
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
        if (list.size() >= 3) {
            selected.add(ATTRIBS.valueOf(list.get(0)));
            selected.add(ATTRIBS.valueOf(list.get(1)));
            selected.add(ATTRIBS.valueOf(list.get(2)));
        }
    }

    @Override
    public List<String> getSelectedAttributes() {
        List<String> l = new ArrayList<String>(3);
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
    public Simplex3Result getResult() {
        Simplex3Result s3result = new Simplex3Result();
        Path p = new Path(getStartNode(), getEndNode());
        s3result.addResult(p, new double[]{150d, 5d, 6d});

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
        s3result.addResult(p2, new double[]{170d, 4d, 7d});

        return s3result;
    }

    @Override
    public void run() {
//        if (selected.size() == 0) {
//            System.err.println("not 3 attribs selected");
//        }
        System.out.println("doing nothing");
    }

    @Override
    public String getName() {
        return "Demo algorithm 3D";
    }
}
