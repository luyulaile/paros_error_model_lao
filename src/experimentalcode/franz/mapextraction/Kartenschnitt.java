package experimentalcode.franz.mapextraction;

import java.io.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.util.*;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Fang Zeng
 */
public class Kartenschnitt extends DefaultHandler {

    static String srcFile = "c:/temp/osm/oberbayern.osm";  //parameter
//    double lat = (48.2609+48.0399)/2; //parameter
//    double lon = (11.4007+11.7344)/2; //parameter
//    double length = 30;   // length of the rectangle in kilometers
//    String dstFile = "c:/temp/osm/muenchen.osm";  //parameter
//    double lat = (47.7886 + 47.7329) / 2; //parameter
//    double lon = (11.5227 + 11.6047) / 2; //parameter
//    double length = 5;   // length of the rectangle in kilometers
//    String dstFile = "c:/temp/osm/toelz.osm";  //parameter
// M�nchen + T�lz
    double lat = (48.1953 + 47.7411)/2; //parameter
    double lon = (11.4007 + 11.7344) / 2; //parameter
    double length = 70;   // length of the rectangle in kilometers
    String dstFile = "c:/temp/osm/muenchen_toelz.osm";  //parameter

    double lattop = lat + getLatDiff(lat, lon, length);   //maximum lat of map
    double latdown = lat - getLatDiff(lat, lon, length);  //minimum lat of map
    double lonleft = lon - getLonDiff(lat, lon, length);  //minimum lon of map
    double lonright = lon + getLonDiff(lat, lon, length); //maximum lon of map
    HashSet<Long> nodehash = new HashSet<Long>();    // the nodes in map
    HashSet<Long> wayhash = new HashSet<Long>();     // the ways in map
    boolean findnode = false;   // whether the node belongs to the map
    boolean hastag = false;     // whether a node has tag.
    boolean rightway = false;   // whether the way belongs to the map 
    boolean rightrelation = false; // whether the relation belongs to the map
    private BufferedWriter out;
    StringBuilder buf = new StringBuilder();
    int progress = 0;

    public static void main(String[] args) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        SAXParser parser = factory.newSAXParser();
        parser.parse(new File(srcFile), new Kartenschnitt());
    }

    @Override
    public void startDocument() {
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dstFile, false),"UTF8"), 20 * 1024 * 1024); // 20MB filebuffer
            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            out.write("<osm version=\"0.6\" generator=\"OpenStreetMap server\">\n");
            out.write("<bounds minlat=\"" + latdown + "\" minlon=\"" + lonleft + "\" maxlat=\"" + lattop + "\" maxlon=\"" + lonright + "\"/>\n");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        try {
            if (qName.equals("node")) {
                double latitude = Double.parseDouble(attributes.getValue("lat"));
                double longitude = Double.parseDouble(attributes.getValue("lon"));

                if (latitude > latdown && latitude < lattop && longitude > lonleft && longitude < lonright) {
                    findnode = true;
                    out.write("<node ");
                    writeAttributes(attributes, out);
                    nodehash.add(Long.parseLong(attributes.getValue("id")));
                }
            }

            if (qName.equals("way")) {
                buf.append("<way ");
                writeAttributes(attributes, buf);
                buf.append(">\n");
            }

            if (qName.equals("nd")) {
                buf.append("\t<nd " + attributes.getQName(0) + "=" + "\"" + quote(attributes.getValue("ref")) + "\"/>\n");
                if (rightway == false) {
                    long ref = Long.parseLong(attributes.getValue("ref"));
                    if (nodehash.contains(ref)) {
                        rightway = true;
                        wayhash.add(ref);
                    }
                }
            }

            if (qName.equals("relation")) {
                buf.append("<relation ");
                writeAttributes(attributes, buf);
                buf.append(">\n");
            }

            if (qName.equals("member")) {
                buf.append("\t<member " + 
                        attributes.getQName(0) + "=\"" + quote(attributes.getValue(0)) + "\" " +
                        attributes.getQName(1) + "=\"" + quote(attributes.getValue(1)) + "\" " +
                        attributes.getQName(2) + "=\"" + quote(attributes.getValue(2)) + "\"/>\n");
                if (!rightrelation && attributes.getValue("type").equals("node")) {
                    rightrelation = nodehash.contains(Long.parseLong(attributes.getValue("ref")));
                }
                if (!rightrelation && attributes.getValue("type").equals("way")) {
                    rightrelation = wayhash.contains(Long.parseLong(attributes.getValue("ref")));
                }
            }
            if (qName.equals("tag")) {
                if (findnode) {
                    if (!hastag) { // close node-tag
                        out.append(">");
                    }
                    hastag = true;
                    out.newLine();
                    out.write("\t<tag ");
                    writeAttributes(attributes, out);
                    out.write("/>");
                }
                if (rightway) {
                    buf.append("\t<tag " + 
                            attributes.getQName(0) + "=\"" + quote(attributes.getValue(0)) + "\" " +
                            attributes.getQName(1) + "=\"" + quote(attributes.getValue(1)) + "\"/>\n");
                }
                if (rightrelation) {
                    buf.append("\t<tag " + 
                            attributes.getQName(0) + "=\"" + quote(attributes.getValue(0)) + "\" " +
                            attributes.getQName(1) + "=\"" + quote(attributes.getValue(1)) + "\"/>\n");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        try {
            if (qName.equals("node") && findnode) {
                findnode = false;
                if (hastag) {
                    hastag = false;
                    out.newLine();
                    out.write("</node>");
                } else {
                    out.write("/>");
                }
                out.newLine();
            }

            if (qName.equals("way")) {
                if (rightway) {
                    out.write(buf.toString());
                    out.write("</way>");
                    out.newLine();
                    rightway = false;
                }

                buf.delete(0, buf.length());
            }

            if (qName.equals("relation")) {
                if (rightrelation) {
                    out.write(buf.toString());
                    out.write("</relation>");
                    out.newLine();
                    rightrelation = false;
                }

                buf.delete(0, buf.length());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void endDocument() {
        try {
            out.write("</osm>");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    public static double getLatDiff(Double lat, Double lon, Double d) {
        //lon1=lon2
        double r = 6371.009;
        double latdiff = d * 180 / (2 * r * 3.1416);
        return latdiff;
    }

    public static double getLonDiff(Double lat, Double lon, Double d) {
        //lat1=lat2
        double r = 6371.009;
        double londiff = d * 180 / (2 * r * Math.abs(Math.cos(lat)) * 3.1416);
        return londiff;
    }

    private void writeAttributes(Attributes attributes, Appendable dest) throws
            IOException {
        for (int i = 0; i < attributes.getLength() - 1; i++) {
            dest = dest.append(attributes.getQName(i) + "=\"" + quote(attributes.getValue(i)) + "\" ");
        }
        dest.append(attributes.getQName(attributes.getLength() - 1) + "=\"" + quote(attributes.getValue(attributes.getLength() - 1)) + "\" ");
    }

    /**
     * Character  	        Escape Code
     * Ampersand 	& 	&amp;
     * Single Quote 	' 	&apos;
     * Double Quote 	" 	&quot;
     * Greater Than 	> 	&gt;
     * Less Than 	< 	&lt;
     * @param value
     * @return
     */
    private String quote(String value) {
        value = value.replace("&", "&amp;");
        value = value.replace("'", "&apos;");
        value = value.replace("\"", "&quot;");
        value = value.replace("<", "&gt;");
        value = value.replace(">", "&lt;");
        return value;
    }
}
										   