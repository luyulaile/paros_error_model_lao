/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package experimentalcode.franz;

import experimentalcode.franz.osm.OSMLink;
import experimentalcode.franz.osm.OSMNode;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.ImageIcon;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.TileFactory;
import org.jdesktop.swingx.painter.AbstractPainter;
import rbr.gm.v1.Prototype;
import rbr.gm.v1.Prototype.Actor;
import rbr.gm.v1.Prototype.Direction;

/**
 *
 * @author yilu
 */
public class DirectionPainter  extends AbstractPainter<JXMapViewer> {
     private final double pi4 = Math.PI / 4;
     private OSMNode node;
     private final int arrowSize = 5;
     private final Color color= Color.GREEN;
     private Prototype.Direction direction;
     private Prototype.Actor actor;
     ImageIcon imageVehicle;
     ImageIcon imageRobot;
    void clear() {
        node=null;
    }
    public void setNode(OSMNode node)
    {
        this.node=node;
    }
    public void setImage(ImageIcon i,ImageIcon robot)
    {
        this.imageVehicle=i;
        this.imageRobot=robot;
    }
    public void setDirection(Direction direction)
    {
        this.direction=direction;
    }
    
    public void setActor(Actor actor)
    {
        this.actor=actor;
    }
    @Override
    protected void doPaint(Graphics2D g, JXMapViewer map, int width, int height) {
        if(node==null) return;
        int no=0;
        /**Paint Vehicle**/
        TileFactory tf = map.getTileFactory();
         int zoom=map.getZoom();
         Point2D p = tf.geoToPixel(node.getGeoPosition(), zoom);
          Rectangle2D vp2 = OSMUtils.getViewport(map);
         if(actor==Actor.driver)
          g.drawImage(imageVehicle.getImage(),(int)(p.getX()-vp2.getX()-18),(int)(p.getY()-vp2.getY()-18),36,36,map);
         else
          g.drawImage(imageRobot.getImage(),(int)(p.getX()-vp2.getX()-18),(int)(p.getY()-vp2.getY()-18),36,36,map);   
         //System.out.println((int)(p.getX()-4));
         //System.out.println((int)(p.getY()-4));
         
        switch(direction)
        {
            case first:
                no=1;
             break;
            case second:
                no=2;
                break;
            case third:
                no=3;
                break;
            case fourth:
                no=4;
                break;
            case fifth:
                no=5;
                break;
            case sixth:
                no=6;
                break;
            case negativefirst:
                no=-1;
                break;
            case negativesecond:
                no=-2;
                break;
            case negativethird:
                no=-3;
                break;
            case negativefourth:
                no=-4;
                break;
            case negativefifth:
                no=-5;
                break;
            case negativesixth:
                no=-6;
                break;              
        };
        if(no>=0)
        {
          OSMLink  link=(OSMLink) node.getOutLinks().get(no-1);
          
         
            //draw line
                    int x1, y1, x2, y2;
                    
            OSMNode srcNode=node;
            OSMNode dstNode=(OSMNode)(node== link.getTarget()?link.getSource():link.getTarget());
           Point2D src = tf.geoToPixel(srcNode.getGeoPosition(), zoom);
            Point2D dst = tf.geoToPixel(dstNode.getGeoPosition(), zoom);
            x1 = (int) (src.getX() - vp2.getX());
            y1 = (int) (src.getY() - vp2.getY());
            x2 = (int) (dst.getX() - vp2.getX());
            y2 = (int) (dst.getY() - vp2.getY());
            g.setColor(color);
            g.drawLine(x1, y1, x2, y2);
            
            // angle of the link
        double theta = Math.atan2(y1 - y2, x1 - x2); // -pi;pi
        int dx = (int) (arrowSize * Math.cos(theta + pi4));
        int dy = (int) (arrowSize * Math.sin(theta + pi4));

        // draw arrows between 2 nodes
        int sx = (x1 + x2) >> 1; // bit shift is faster than /2
        int sy = (y1 + y2) >> 1;
        g.setColor(color);
        g.drawLine(sx, sy, sx + dx, sy + dy);
        dx = (int) (arrowSize * Math.cos(theta - pi4));
        dy = (int) (arrowSize * Math.sin(theta - pi4));
        g.setColor(color);
        g.drawLine(sx, sy, sx + dx, sy + dy);
        }
        else  //draw the reverse direction
        {
            g.setColor(color);
             OSMLink  link=(OSMLink) node.getInLinks().get(no*(-1)-1);
        
            //draw line
                    int x1, y1, x2, y2;
                    
            OSMNode srcNode=node;
            OSMNode dstNode=(OSMNode)(node== link.getTarget()?link.getSource():link.getTarget());
           Point2D src = tf.geoToPixel(srcNode.getGeoPosition(), zoom);
            Point2D dst = tf.geoToPixel(dstNode.getGeoPosition(), zoom);
            
            
            x1 = (int) (dst.getX() - vp2.getX());
            y1 = (int) (dst.getY() - vp2.getY());
            x2 = (int) (2*dst.getX()-src.getX()- vp2.getX());
            y2 = (int) (2*dst.getY()-src.getY() - vp2.getY());
            g.drawLine(x1, y1, x2, y2);
            
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
    }
    
}
