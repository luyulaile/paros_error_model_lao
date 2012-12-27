/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package experimentalcode.franz;

import experimentalcode.franz.osm.OSMLink;
import experimentalcode.franz.osm.OSMNode;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.TileFactory;
import org.jdesktop.swingx.painter.AbstractPainter;

/**
 *
 * @author yilu
 */
public class GMPathPainter extends AbstractPainter<JXMapViewer> {
private final Color color = Color.YELLOW;
private final Color  colorBorder = Color.DARK_GRAY;
private final Color  colorFill=Color.YELLOW;
private ArrayList<OSMNode> list;
    
    @Override
    protected void doPaint(Graphics2D gd, JXMapViewer map, int i, int i1) {
        if(list==null||list.size()==0)
            return;
        gd.setColor(color);
        int zoom=map.getZoom();
        TileFactory tf = map.getTileFactory();
        Rectangle2D vp2 = OSMUtils.getViewport(map);
        
      //   paintLink(list,gd, tf, zoom, vp2);
        highLightNodes(list,gd, tf, zoom, vp2);
    }
     private void paintLink(List<OSMNode> nodes, Graphics2D g, TileFactory tf, int zoom, Rectangle2D vp2) {
       //System.out.println("In paintLink():nodes: "+nodes.toString());
       //System.out.println("In paintLink():link: "+link.getNodes().toString());
       //System.out.println("In paintLink():link get Source and target: "+link.getSource()+link.getTarget());
       
        //System.out.println("pixDist: "+pixDist);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        Point2D dst = null;
        
        Point2D src =tf.geoToPixel(nodes.get(0).getGeoPosition(),zoom); 
                
        int x1, y1, x2, y2;
        double delta = -1;
        for (int i = 1; i < nodes.size(); i++) {
            src=tf.geoToPixel(nodes.get(i-1).getGeoPosition(),zoom); 
            dst = tf.geoToPixel(nodes.get(i).getGeoPosition(),zoom);
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
            //if (link.isOneway() && delta > (arrowSize << 1)) {
               // paintOneWay(x1, x2, y1, y2, g);  //restirected to paint one way
            }
            src = dst;
        }
    
        void clear() {
            if(list==null)return;
        list.clear();
    }
        private int length(OSMLink<OSMNode> link, TileFactory tf, int zoom) {
        Point2D a = tf.geoToPixel(link.getSource().getGeoPosition(), zoom);
        Point2D b = tf.geoToPixel(link.getTarget().getGeoPosition(), zoom);
        return (int) a.distance(b);
    }
        
    public void setNodes(ArrayList<OSMNode> list)
    {
        this.list=list;
    }

    private void highLightNodes(List<OSMNode> nodes, Graphics2D g, TileFactory tf, int zoom, Rectangle2D vp) {
                for (OSMNode node : nodes) {
            Point2D srcPoint = tf.geoToPixel(node.getGeoPosition(), zoom);
            if (!vp.contains(srcPoint)) {
                continue;
            }
            // paint nodes as little dots
            int x = (int) (srcPoint.getX() - vp.getX());
            int y = (int) (srcPoint.getY() - vp.getY());
            g.setColor(colorBorder);
            g.drawOval(x - 4, y - 4, 8, 8);
            g.setColor(colorFill);
            g.fillOval(x - 3, y - 3, 6, 6);
    }
    }
}
