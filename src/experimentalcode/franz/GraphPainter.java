package experimentalcode.franz;


import experimentalcode.franz.osm.OSMGraph;
import experimentalcode.franz.osm.OSMLink;
import experimentalcode.franz.osm.OSMNode;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.WeakHashMap;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.TileFactory;
import org.jdesktop.swingx.painter.AbstractPainter;


/**
 * @author graf
 */
class GraphPainter extends AbstractPainter<JXMapViewer> {

    private final Color color = new Color(0, 100, 0);
    private final Color colorOneWay = Color.blue;
    private final Color colorBlocked= Color.black;
    private final Color colorDifficult=Color.RED;
    private OSMGraph<OSMNode<OSMLink>, OSMLink<OSMNode>> graph;
    private WeakHashMap<OSMNode, Point2D> geo2pixel = new WeakHashMap<OSMNode, Point2D>(1000);
    private int lastZoom = -1;
    private final double pi4 = Math.PI / 4;
    private final int arrowSize = 5;

    void setGraph(OSMGraph<OSMNode<OSMLink>, OSMLink<OSMNode>> graph) {
        this.graph = graph;
        this.geo2pixel.clear();
    }

    @Override
    protected void doPaint(Graphics2D g, JXMapViewer map, int width, int height) {
        if (graph == null) {
            return;
        }

        int zoom = map.getZoom();//this is a test
        if (lastZoom != zoom) {
            geo2pixel.clear();
            lastZoom = zoom;
        }

        // figure out which waypoints are within this map viewport so, get the bounds
        Rectangle2D vp2 = OSMUtils.getViewport(map);
        Rectangle viewportBounds = map.getViewportBounds();
      
        TileFactory tf = map.getTileFactory();
        

        // primitive cache for checking if a node has been painted in the neighbourhoud already
        // dividing by 2 is done in order to protect a little area around each pixel
        boolean[][] pixels = new boolean[1 + ((int) viewportBounds.getWidth()) >> 1][1 + ((int) viewportBounds.getHeight()) >> 1];

        List<OSMNode> nodes = new ArrayList<OSMNode>(graph.getNodes().size() / 10);
        HashSet processedLinks = new HashSet(1000);
        Point2D point = null;
        for (OSMNode<OSMLink> node : graph.getNodes()) {
            
            if(node.difficulty==2)
            {
               // System.out.println("find the blocked");
                g.setColor(colorBlocked);
            }
            else
                if(node.difficulty==1)
                {
                 //   System.out.println("find the difficult");
                    g.setColor(colorDifficult);
                }
            
            point = toPixel(node, tf, zoom);
            if (vp2.contains(point)) {
                { // paint node as little dot IF the neighbourhood is unpainted
                    int x = (int) (point.getX() - vp2.getX());
                    int y = (int) (point.getY() - vp2.getY());
                    if (!pixels[x >> 1][y >> 1]) {
                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                        g.drawRect(x - 1, y - 1, 2, 2);
                        pixels[x >> 1][y >> 1] = true;
                    }
                }

                // paint links
                for (OSMLink<OSMNode> link : node.getLinks()) {
                    if (processedLinks.add(link)) {
                        g.setColor(link.isOneway() ? colorOneWay : color);
                       // System.out.println("In doPaint():"+link.getNodes().toString());
                        paintLink(nodes, link, g, tf, zoom, vp2);
                    }
                }
            }
        }
    }

    private void paintLink(List<OSMNode> nodes, OSMLink<OSMNode> link, Graphics2D g, TileFactory tf, int zoom, Rectangle2D vp2) {
       //System.out.println("In paintLink():nodes: "+nodes.toString());
       //System.out.println("In paintLink():link: "+link.getNodes().toString());
       //System.out.println("In paintLink():link get Source and target: "+link.getSource()+link.getTarget());
        int pixDist = length(link, tf, zoom);
        if (pixDist <= 3) {
            return;
        }
        nodes.clear();
        //System.out.println("pixDist: "+pixDist);
        if (pixDist >= 20) {
            nodes.addAll(link.getNodes());
        } else {
            nodes.add(link.getSource());
            nodes.add(link.getTarget());
        }

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        Point2D dst;
        
        Point2D src = toPixel(nodes.get(0), tf, zoom);
        int x1, y1, x2, y2;
        double delta = -1;
        for (int i = 1; i < nodes.size(); i++) {
            dst = toPixel(nodes.get(i), tf, zoom);
            delta = src.distance(dst);
            // only paint links > 10px or links to the end 
            if (delta <= 10 && i < nodes.size() - 1) {
                continue;
            }
            x1 = (int) (src.getX() - vp2.getX());
            y1 = (int) (src.getY() - vp2.getY());
            x2 = (int) (dst.getX() - vp2.getX());
            y2 = (int) (dst.getY() - vp2.getY());
            g.drawLine(x1, y1, x2, y2);
            // paint arrows only on lines with a length of
            // 2*arrowsize. Bitshift again to be a bit faster
            if (link.isOneway() && delta > (arrowSize << 1)) {
                paintOneWay(x1, x2, y1, y2, g);  //restirected to paint one way
            }
            src = dst;
        }
    }

    // paint arrow
    private void paintOneWay(int x1, int x2, int y1, int y2, Graphics2D g) {
        // angle of the link
        double theta = Math.atan2(y1 - y2, x1 - x2); // -pi;pi
        int dx = (int) (arrowSize * Math.cos(theta + pi4));
        int dy = (int) (arrowSize * Math.sin(theta + pi4));
     
        // draw arrows between 2 nodes
        int sx = (x1 + x2) >> 1; // bit shift is faster than /2
        int sy = (y1 + y2) >> 1;
        g.drawLine(sx, sy, sx + dx, sy + dy);
        dx = (int) (arrowSize * Math.cos(theta - pi4));
        dy = (int) (arrowSize * Math.sin(theta - pi4));
        g.drawLine(sx, sy, sx + dx, sy + dy);
    }
    
 
    

    public  Point2D toPixel(OSMNode n, TileFactory tf, int zoom) {
        Point2D p = geo2pixel.get(n);
        if (p == null) {
            p = tf.geoToPixel(n.getGeoPosition(), zoom);
            geo2pixel.put(n, p);
        }
        return p;
    }

    private int length(OSMLink<OSMNode> link, TileFactory tf, int zoom) {
        Point2D a = toPixel(link.getSource(), tf, zoom);
        Point2D b = toPixel(link.getTarget(), tf, zoom);
        return (int) a.distance(b);
    }
}
