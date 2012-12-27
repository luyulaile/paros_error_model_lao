package experimentalcode.franz.osm;

import de.lmu.ifi.dbs.paros.graph.Graph;
import de.lmu.ifi.dbs.paros.graph.Node;
import experimentalcode.franz.OSMUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

//public class OSMGraph extends Graph<OSMNode, OSMLink> {
public class OSMGraph<N extends OSMNode, L extends OSMLink> extends Graph<N, L> {

    private Logger log = Logger.getLogger(OSMGraph.class.getName());
    private List<OSMLink<OSMNode>> linkList = new ArrayList<OSMLink<OSMNode>>();
    private HashMap<String, Integer> speed = new HashMap<String, Integer>();
    /**
     * All Nodes without links. These are either single highway-nodes (?) or
     * nodes without links which are usually nodes used for painting but not for
     * navigating.
     */
    private List<N> nodesWithoutLinks = new ArrayList<N>();

    public OSMGraph() {
        // http://wiki.openstreetmap.org/wiki/DE:Map_Features
        speed.put("default", 50); // myown definition
        //
        speed.put("motorway", 130);
        speed.put("motorway_link", 60);
        speed.put("trunk", 100);
        speed.put("trunk_link", 60);
        speed.put("primary", 100);
        speed.put("primary_link", 60);
        speed.put("secondary", 100);
        speed.put("secondary_link", 60);
        speed.put("tertiary", 100);
        speed.put("tertiary_link", 100);
        speed.put("unclassified", 50);
        speed.put("road", 50);
        speed.put("residential", 50);
        speed.put("living_street", 6);
        speed.put("service", 30);
        speed.put("track", 30);
        speed.put("pedestrian", 10);
        speed.put("raceway", 250);
        speed.put("services", 60);
        speed.put("bus_guideway", 60);
        speed.put("construction", 3);
        // Paths
        speed.put("path", 4);
        speed.put("cycleway", 15);
        speed.put("footway", 5);
        speed.put("bridleway", 5);
        speed.put("byway", 15);
        speed.put("steps", 3);
    }

    public OSMGraph(File nodeFile, File linkFile) throws IOException {
        this();
        readNodes(nodeFile);
        readWays(linkFile);
        splitLink();
        addNodes();
        cleanup();
        initializeDifficulty();
    }

    public int getLinkCount() {
        return linkList.size();
    }

    public int getNodesWithoutLinksCount() {
        return nodesWithoutLinks.size();
    }

    public void makeNavigableGraph() {
        connect();
    }

    
    public void serializeTo(File nodes, File edges) throws IOException {
        serializeNodes(nodes);
        serializeWays(edges);
    }
/**
 * write Ways into txt file.
 * @param edges
 * @throws IOException 
 */
    private void serializeWays(File edges) throws IOException {
        log.fine("Serializing edges");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(edges, false), "UTF8"), 5 * 1024 * 1024); // 5MB filebuffer
        for (OSMLink<OSMNode> link : linkList) {
            bw.append("id=" + link.getId());
            for (OSMNode node : link.getNodes()) {
                bw.append(",node=" + node.getName());
            }
            Map<String, String> attr = link.getAttr();
            for (Map.Entry<String, String> entry : attr.entrySet()) {
                assert !entry.getKey().equals("node") : "node in properties for link " + link;
                bw.append("," + entry.getKey() + "=\"" + entry.getValue() + "\"");
            }
            bw.newLine();
        }
        bw.close();
    }
/**
 * write nodes into txt file
 * @param nodes
 * @throws IOException 
 */
    private void serializeNodes(File nodes) throws IOException {
        log.fine("Serializing nodes with links");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(nodes, false), "UTF8"), 5 * 1024 * 1024); // 5MB filebuffer
        for (OSMNode node : getNodes()) {
            bw.append(nodeToString(node));
            bw.newLine();
        }
        log.fine("Serializing nodes without links");
        for (OSMNode node : nodesWithoutLinks) {
            bw.append(nodeToString(node));
            bw.newLine();
        }
        bw.close();
    }

    private String nodeToString(OSMNode node) {
        StringBuffer bw = new StringBuffer(50);
        bw.append(String.format(Locale.US, "id=%d,lat=%f,lon=%f",
                node.getName(), node.getLat(), node.getLon()));
        if (node.getHeight() > 0) {
            bw.append(String.format(Locale.US, ",height=%.3f", node.getHeight()));
        }
        Map<String, String> attr = node.getAttr();
        for (Map.Entry<String, String> entry : attr.entrySet()) {
            bw.append("," + entry.getKey() + "=\"" + entry.getValue() + "\"");
        }
        return bw.toString();
    }

    /**
     * Remove nodes without any links. This means also nodes that are used for 
     * painting only.
     */
    private void cleanup() {
        log.fine("removing nodes without links");
        nodesWithoutLinks.clear();
        List<N> list = new ArrayList<N>(getNodes());
        for (N n : list) {
            if (Thread.interrupted()) {
                return;
            }
            if (n.getLinks().size() == 0) {
                removeNode(n);
                nodesWithoutLinks.add(n);
            }
        }
        log.fine("remaining nodes: " + getNodes().size() + ". Removed " + nodesWithoutLinks.size() + " nodes");
    }

    private void readWays(File linkFile) throws IOException,
            NumberFormatException {
        log.fine("reading ways from " + linkFile.getAbsolutePath());

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(linkFile), "UTF8"), 1 * 1024 * 1024);
            String line = null;
            while ((line = br.readLine()) != null && !Thread.interrupted()) {
                List<String[]> values = splitLine(line);
                OSMNode src = getNode(Integer.parseInt(values.get(1)[1]));
                OSMNode dst = null;

                int lastNode = values.size();
                // find last "node" value. Search from the end of the list to the front
                while (--lastNode >= 0 && dst == null) {
                    if (values.get(lastNode)[0].equals("node")) {
                        dst = getNode(Integer.parseInt(values.get(lastNode)[1]));
                        break;
                    }
                }
                assert lastNode >= 0 : line;
                if (src != null && dst != null) {
                    // is it a one way street?
                    boolean highway = false;
                    boolean oneway = false;
                    for (String[] pair : values) {
                        oneway |= pair[0].equals("oneway") && pair[1].equals("yes");
                        highway |= pair[0].equals("highway");
                    }
                    if (!highway) {
                        continue;
                    }

                    OSMLink<OSMNode> l = new OSMLink(src, dst, oneway);
                    assert values.get(0)[0].equals("id") : "id not found in line " + line;
                    l.setId(Integer.parseInt(values.get(0)[1]));

                    linkList.add(l);
                    // add intermediate nodes to the link between 2 nodes
                    for (int i = 1; i <= lastNode; i++) {
                        OSMNode node = getNode(Integer.parseInt(values.get(i)[1]));
                        if (node != null) {
                            l.setNodes(node);
                        }
                    }
                    // add attributes
                    for (int i = lastNode + 1; i < values.size(); i++) {
                        String[] pair = values.get(i);
                        /*if (pair[0].equals("distance")) {
                        l.setDistance(OSMUtils.dist(l));
                        } else*/
                        if (pair[0].equals("ascend")) {
                            l.setAscend(Double.parseDouble(pair[1]));
                        } else if (pair[0].equals("descend")) {
                            l.setDescend(Double.parseDouble(pair[1]));
                        } else if (pair[0].equals("incline")) { // Steigung/Gef�lle
                            // remove all non digits (like "%")
                            pair[1] = pair[1].replaceAll("[^\\d]", "");
                            l.setAttr(pair[0], pair[1]);
                            // TODO: check correctness!
//                    } else if (pair[0].equals("distance")) {
//                        try {
//                            Double dist = Double.parseDouble(pair[1]);
//                            if (dist != null && dist > 0) {
//                                l.setDistance(dist);
//                            }
//                        } catch (NumberFormatException ignore) {
//                        }
                        } else {
                            assert !pair[0].equals("node") : "node at pos " + i + " in line where it should not be? Line: " + line;
                            l.setAttr(pair[0], pair[1]);
                        }
                    }
                    if (l.getDistance() <= 0) {
                        l.setDistance(OSMUtils.dist(l));
                    }
                    if (l.getAscend() == 0 && l.getDescend() == 0 && l.getSource().getHeight() != l.getTarget().getHeight()) {
                        double height = l.getTarget().getHeight() - l.getSource().getHeight();
                        if (height < 0) {
                            l.setDescend(-height);
                        } else {
                            l.setAscend(height);
                        }
                    }
                    setSpeed(l);
                }
            }
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                throw e;
            }
        }
        log.fine("number of links: " + linkList.size());
    }

    private void readNodes(File nodeFile) throws NumberFormatException,
            IOException {
        log.fine("reading nodes from " + nodeFile.getAbsolutePath());

        // read nodes
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(nodeFile), "UTF8"), 1 * 1024 * 1024);
            String line;
            while ((line = br.readLine()) != null && !Thread.interrupted()) {
                List<String[]> values = splitLine(line);
                OSMNode n = new OSMNode(Integer.parseInt(values.get(0)[1]));
                n.setLat(Double.parseDouble(values.get(1)[1]));
                n.setLon(Double.parseDouble(values.get(2)[1]));
                int i = 3;
                // height need not be set!
                if (values.size() >= i + 1 && values.get(3)[0].equals("height")) {
                    n.setHeight(Double.parseDouble(values.get(i++)[1]));
                }
                // remaining pairs must be attributes
                for (; i < values.size(); i++) {
                    n.setAttr(values.get(i)[0], values.get(i)[1]);
                }
                addNode((N) n);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                throw e;
            }
        }
        log.fine("number of nodes: " + getNodes().size());
    }

    private List<String[]> splitLine(String s) {
        List<String[]> list2 = new ArrayList<String[]>();
        final int QUOT = '"';
        final int SEP = ',';
        final int LENGTH = s.length();
        int indexA = 0;
        int indexB = 0;
        int stopchar = '-';

        while (indexA < LENGTH) {
            String[] pair = new String[2];
            // read key
            if (s.charAt(indexA) == QUOT) {
                stopchar = QUOT;
                indexA++;
            } else {
                stopchar = '=';
            }
            indexB = s.indexOf(stopchar, indexA);
            assert indexA >= 0 : "indexA must be >= 0 but was " + indexA + " in line: " + s;
            assert indexB >= 0 : "indexB must be >= 0 but was " + indexB + " in line: " + s;
            pair[0] = s.substring(indexA, indexB);
            indexA = indexB;
            if (stopchar == QUOT) {
                indexA++;
            }
            indexA++; // step over =

            // read value
            if (s.charAt(indexA) == QUOT) {
                stopchar = QUOT;
                indexA++;
            } else {
                stopchar = SEP;
            }
            indexB = s.indexOf(stopchar, indexA);
            indexB = (indexB == -1) ? LENGTH - 1 : indexB;
            pair[1] = s.substring(indexA, indexB);
            indexA = indexB;
            if (stopchar == QUOT) {
                indexA++;
            }
            indexA++; // Step over ,
            list2.add(pair);
        }
        return list2;
    }

    public Node getNode(Double lat, Double lon) {
        Collection<N> nodes = this.getNodes();
        for (N aktN : nodes) {
            if (aktN.getLat() == lat && aktN.getLon() == lon) {
                return aktN;
            }
        }
        return null;
    }

    /**
     * AC and BD might be linked, but not AB, BC.
     * So split AC into AB,BC.
     *
     * A--B--C
     *    |
     *    D
     */
    private void connect() {
        log.info("connecting ways for routing");
        int linkCountA = linkList.size();
        List<OSMNode> nodes = new ArrayList<OSMNode>(1000);
        for (OSMNode n : getNodes()) {
            if (n.getLinks().size() > 0) {
                nodes.add(n);
            }
        }

        for (int iL = 0; iL < linkList.size() && !Thread.interrupted(); iL++) {
            if (log.isLoggable(Level.FINE) && iL % 100 == 0) {
                log.fine("processed " + iL + " / " + linkList.size() + " links");
            }

            int removes = 0;
            int adds = 0;
            OSMLink<OSMNode> l = linkList.get(iL);
            List<OSMNode> linkNodes = l.getNodes();
            for (int iN = 0; l != null && iN < linkNodes.size() && !Thread.interrupted(); iN++) {
                OSMNode innerNode = linkNodes.get(iN);
                List<OSMLink<OSMNode>> links = innerNode.getLinks();
                if (links.size() > 0 && !links.contains(l)) {
                    linkList.remove(iL);
                    removes++;
                    List<OSMLink<OSMNode>> newLinks = OSMUtils.split(l, innerNode);
                    for (OSMLink<OSMNode> aLink : newLinks) {
                        linkList.add(iL, aLink);
                        adds++;
                    }
                    l = null;
                    linkNodes = null;
                    iL--;
                }
            }
        }
        log.info("added " + (linkList.size() - linkCountA) + " links.");
    }

    // http://wiki.openstreetmap.org/wiki/MaxSpeed_Overlay_Kosmos_Rules
    // http://wiki.openstreetmap.org/wiki/DE:MaxSpeed_Karte
    private void setSpeed(OSMLink l) {
        assert speed != null : "speed object is null?";
        assert l != null : "link is null?";

        String maxSpeedValue = l.getAttr("maxspeed");
        if (maxSpeedValue != null) { // maxSpeed set. try to use it
            if (maxSpeedValue.contains(";")) {
                log.info("Link [" + l + "]: multiple values in maxspeed. Using first of: " + maxSpeedValue);
                maxSpeedValue = maxSpeedValue.split(";")[0];
            }
            try {
                l.setSpeed(Integer.parseInt(maxSpeedValue));
            } catch (NumberFormatException i) {
                // Not a number :-/
                if (maxSpeedValue.equals("walk")) {
                    l.setSpeed(speed.get("footway"));
                } else if (maxSpeedValue.equals("variable")) {
                    l.setSpeed(speed.get("footway"));
                } else {
                    log.info("Link [" + l + "]: Unmapped maxspeed value: " + maxSpeedValue + ".Use highway type.");
                }
            }

            // okay we've been successfull with the maxspeed attribute
            if (l.getSpeed() > 0) {
                return;
            }
        }

        // no maxspeed set or it was unparseable. Try to map the highway types
        String highway = l.getAttr("highway");
        if (highway != null) {
            Integer maxSpeedInt = speed.get(highway);

            if (maxSpeedInt == null && highway.contains(";")) {
                log.fine("Link [" + l + "]: multiple highway settings. Using first of: " + highway);
                maxSpeedInt = speed.get(highway.split(";")[0]);
            }
            if (maxSpeedInt == null) {
                log.fine("Link [" + l + "]: unknown highway type: " + highway + ". Setting default.");
                maxSpeedInt = speed.get("default");
            }
            l.setSpeed(maxSpeedInt);
            return;

        }

        // neither maxspeed nor highway gave a hint about the speed of this link
        log.info("Link [" + l + "]: no highway type set, using default.");
        l.setSpeed(speed.get("default"));
    }

    /**
     * make all the link become link with only two nodes.
     */
    private void splitLink() {
        ArrayList<OSMLink<OSMNode>> newLinkList = new ArrayList<OSMLink<OSMNode>>();
        for(OSMLink link:linkList)
        {
            //System.out.println("orignial link:"+link.toString());
            OSMNode source=(OSMNode) link.getSource();
            OSMNode dest=(OSMNode) link.getTarget();
            List<OSMNode> list=link.getNodes();
            int indexOfSource=list.indexOf(source);
            int indexOfDest=list.indexOf(dest);
            //System.out.println("index of source:"+indexOfSource+", index of dest :"+indexOfDest);
            if(link.isOneway()==true)
            {
                for(int i=indexOfSource;i<indexOfDest;i++)
                {
                    OSMNode temp=list.get(i);
                    OSMNode temp2=list.get(i+1);
                    ArrayList<OSMNode> nodesList=new ArrayList<OSMNode>();
                    nodesList.add(temp);
                    nodesList.add(temp2);
                    //System.out.println("Before add link :"+temp.getLinks());
                    OSMLink tl=new OSMLink(temp,temp2,true);
                    tl.setId(link.getId());
                    tl.setNodes(nodesList);
                    tl.setDistance(OSMUtils.dist(temp,temp2));
                    //System.out.println("the distance of the link is:"+OSMUtils.dist(temp,temp2));
                    tl.setSpeed(link.getSpeed());
                    //System.out.println("After add link :"+temp.getLinks());
                    newLinkList.add(tl);
                    temp.addLink(tl);
                    temp2.addLink(tl);
                }
            }
            else // not one way
            {
                  for(int i=indexOfSource;i<indexOfDest;i++)
                {
                  OSMNode temp=list.get(i);
                    OSMNode temp2=list.get(i+1);
                    ArrayList<OSMNode> nodesList=new ArrayList<OSMNode>();
                    nodesList.add(temp);
                    nodesList.add(temp2);
                    //System.out.println("Before add link :"+temp.getLinks());
                    OSMLink tl=new OSMLink(temp,temp2,false);
                    tl.setId(link.getId());
                    tl.setNodes(nodesList);
                    tl.setDistance(OSMUtils.dist(temp,temp2));
                    //System.out.println("the distance of the link is:"+OSMUtils.dist(temp,temp2));
                    tl.setSpeed(link.getSpeed());
                    //System.out.println("After add link :"+temp.getLinks());
                    newLinkList.add(tl);
                    temp.addLink(tl);
                    temp2.addLink(tl);
                }
//                 for(int i=indexOfDest;i>indexOfSource;i--)
//                {
//                    OSMNode temp=list.get(i);
//                    OSMNode temp2=list.get(i-1);
//                    ArrayList<OSMNode> nodesList=new ArrayList<OSMNode>();
//                    nodesList.add(temp);
//                    nodesList.add(temp2);
//                    //System.out.println("Before add link :"+temp.getLinks());
//                    OSMLink tl=new OSMLink(temp,temp2,true);
//                    tl.setId(link.getId());
//                    tl.setNodes(nodesList);
//                    tl.setDistance(OSMUtils.dist(temp,temp2));
//                    System.out.println("the distance of the link is:"+OSMUtils.dist(temp,temp2));
//                    tl.setSpeed(link.getSpeed());
//                    //System.out.println("After add link :"+temp.getLinks());
//                    newLinkList.add(tl);
//                }
            }
            
            source.removeLink(link);
            dest.removeLink(link);
        }
        this.linkList=newLinkList;
    }
    
    double DistanceUnit=0.042;
     /**
     * make all the link become link with only two nodes.
     */
    private void addNodes() {
        ArrayList<OSMLink<OSMNode>> newLinkList = new ArrayList<OSMLink<OSMNode>>();
        int n=0;
        int id=100000;
        for(OSMLink link:linkList)
        {
               if(link.getDistance()>1.5*DistanceUnit)
               {
                   if(link.isOneway()==true)
                   {
                       n=(int) Math.round(link.getDistance()/DistanceUnit);
                   
                   OSMNode start=(OSMNode) link.getSource();
                   OSMNode dest=(OSMNode) link.getTarget();
                   
                   // we need to avoid the repeate process.
                   
                   OSMNode nodeArray[]=new OSMNode[n+1];
                   nodeArray[0]=start;
                   nodeArray[n]=dest;
                   for(int i=1;i<n;i++)//create n-1 nodes along the link
                   {
                      OSMNode temp=new OSMNode(id++);
                       temp.setLat(start.getLat()+i*(dest.getLat()-start.getLat())/n);
                       temp.setLon(start.getLon()+i*(dest.getLon()-start.getLon())/n);
                       temp.setHeight(start.getHeight()+i*(dest.getHeight()-start.getHeight())/n);
                       addNode((N)temp);
                       nodeArray[i]=temp;

                   }
                                      

                   for(int i=0;i<n;i++)//create n link
                   {
                   OSMNode temp=nodeArray[i];
                   OSMNode temp2=nodeArray[i+1];
                    ArrayList<OSMNode> nodesList=new ArrayList<OSMNode>();
                    nodesList.add(temp);
                    nodesList.add(temp2);
                    //System.out.println("Before add link :"+temp.getLinks());
                    OSMLink tl=new OSMLink(temp,temp2,true);
                    tl.setId(link.getId());
                    tl.setNodes(nodesList);
                    tl.setDistance(OSMUtils.dist(temp,temp2));
                  
                    temp.addLink(tl);
                    temp2.addLink(tl);
                   // System.out.println("temp:"+temp.toString()+", get links:"+temp.getLinks());
                   // System.out.println("temp2:"+temp2.toString()+", get links:"+temp.getLinks());
                    newLinkList.add(tl);
                  
                   }
                   
                   start.removeLink(link);
                   dest.removeLink(link);
                   }
                   else
                   {
                                              n=(int) Math.round(link.getDistance()/DistanceUnit);
                   
                   OSMNode start=(OSMNode) link.getSource();
                   OSMNode dest=(OSMNode) link.getTarget();
                   
                   // we need to avoid the repeate process.
                   
                   OSMNode nodeArray[]=new OSMNode[n+1];
                   nodeArray[0]=start;
                   nodeArray[n]=dest;
                   for(int i=1;i<n;i++)//create n-1 nodes along the link
                   {
                      OSMNode temp=new OSMNode(id++);
                       temp.setLat(start.getLat()+i*(dest.getLat()-start.getLat())/n);
                       temp.setLon(start.getLon()+i*(dest.getLon()-start.getLon())/n);
                       temp.setHeight(start.getHeight()+i*(dest.getHeight()-start.getHeight())/n);
                       addNode((N)temp);
                       nodeArray[i]=temp;

                   }
                                      

                   for(int i=0;i<n;i++)//create n link
                   {
                   OSMNode temp=nodeArray[i];
                   OSMNode temp2=nodeArray[i+1];
                    ArrayList<OSMNode> nodesList=new ArrayList<OSMNode>();
                    nodesList.add(temp);
                    nodesList.add(temp2);
                    //System.out.println("Before add link :"+temp.getLinks());
                    OSMLink tl=new OSMLink(temp,temp2,false);
                    tl.setId(link.getId());
                    tl.setNodes(nodesList);
                    tl.setDistance(OSMUtils.dist(temp,temp2));
                  
                    temp.addLink(tl);
                    temp2.addLink(tl);
                  //  System.out.println("temp:"+temp.toString()+", get links:"+temp.getLinks());
                  //  System.out.println("temp2:"+temp2.toString()+", get links:"+temp.getLinks());
                    newLinkList.add(tl);
                  
                   }
                   
                   start.removeLink(link);
                   dest.removeLink(link);
                   }
               }
               else  // if we don't need to add link ,we keep the link.
               {
                   newLinkList.add(link);
               }
        }
        this.linkList=newLinkList;
    }

    private void initializeDifficulty() {
     for(OSMNode node:this.getNodes())
     {
         if(node.getName()>495265309&&node.getName()<605265309)
             node.difficulty=2;
         else
//             if(node.getName()>1692395008&&node.getName()<=796541454)
         if(node.getName()>1692395008&&node.getName()<=706541454)
                 node.difficulty=1;
             else
//                 if(node.getName()>42428000&&node.getName()<52428000)
                   if(node.getName()>42428000&&node.getName()<42438000)
                     node.difficulty=1;
                   else
                       if(node.getName()>100000&&node.getName()<100060)
                           node.difficulty=1;
         else
                 node.difficulty=0;
     }
    }
}
