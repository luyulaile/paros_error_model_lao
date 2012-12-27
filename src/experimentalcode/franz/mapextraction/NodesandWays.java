package experimentalcode.franz.mapextraction;

import experimentalcode.franz.osm.OSMGraph;
import java.io.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.lang.ref.SoftReference;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.util.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.zip.ZipFile;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Fang Zeng
 */
public class NodesandWays extends DefaultHandler {

    static File osmFile = new File("C:/temp/osm/muenchen_toelz.osm");  //parameter
    static File out = new File("c:/temp/osm/muenchen_toelz");
    // -------------------------------------------------------------------------
    private static final String WAYSFILE = "ways.txt";
    private static final String NODESFILE = "nodes.txt";
    private static final File SRTMbase = new File("C:/temp/osm/SRTM3/Eurasia");
    // -------------------------------------------------------------------------
    private static int BUFFERED_SIZE = 1024 * 1024 * 5;
    private static boolean filter_data = true;  //true = ALL nodes, false = only start/end
    private static boolean findref = false;
    private static final Logger log = Logger.getLogger(NodesandWays.class.getName());
    // -
    String tmp;
    String a[] = new String[1];
    StringBuffer buf = new StringBuffer();
    double[] l = new double[4];
    private Hashtable<String, String[]> nodehash = new Hashtable<String, String[]>();
    private BufferedWriter nodes, ways;
    // SRTM Cache. Cache for opened SRTM Files as long as there is memory left.
    static HashMap<File, SoftReference<BufferedInputStream>> srtmMap = new HashMap<File, SoftReference<BufferedInputStream>>();
    private boolean openedNode = false; // indicates that a <node> is currently processed
    private boolean openedWay = false;// indicates that a <way> is currently processed
    private double distance = 0;

    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().readConfiguration(NodesandWays.class.getResourceAsStream("../logging.properties"));

        try {
            log.info("parse XML to TXT: "+osmFile);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(true);
            SAXParser parser = factory.newSAXParser();
            parser.parse(osmFile, new NodesandWays());
            for (SoftReference<BufferedInputStream> ref : srtmMap.values()) {
                BufferedInputStream bis = ref.get();
                if (bis != null) {
                    bis.close();
                }
            }
            srtmMap.clear();
        } catch (SAXParseException ex) {
            log.severe(ex.getMessage());
            log.severe("line: " + ex.getLineNumber());
            log.severe("column: " + ex.getColumnNumber());
            System.exit(1);
        }

        // ---------
        {
            log.info("cleaning graph");
            File nodesFile = new File(out, NODESFILE);
            File waysFile = new File(out, WAYSFILE);
            OSMGraph graph = new OSMGraph(nodesFile, waysFile);
            graph.makeNavigableGraph();
            graph.serializeTo(nodesFile, waysFile);
            
        }
    }

    @Override
    public void startDocument() {
        try {
            out.mkdirs();
            nodes = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(out, NODESFILE)), "UTF8"), BUFFERED_SIZE);
            ways = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(out, WAYSFILE)), "UTF8"), BUFFERED_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        try {
            if (qName.equals("node")) {
                openedNode = true;
                String lat = attributes.getValue("lat");
                String lon = attributes.getValue("lon");
                double elevation = getElevation(Double.parseDouble(lat), Double.parseDouble(lon));
                nodes.write(String.format(Locale.US, "id=%s,lat=%s,lon=%s,height=%.3f",
                        attributes.getValue("id"), lat, lon, elevation));
                nodehash.put(attributes.getValue(0), new String[]{lat, lon});
            } else if (qName.equals("way")) {
                openedWay = true;
                findref = true;
                ways.write("id=" + attributes.getValue("id") + ",");
            } else if (qName.equals("nd")) {
                if (filter_data) {
                    ways.write("node=" + attributes.getValue("ref") + ",");
                } else {
                    if (findref) {
                        findref = false;
                        ways.write("node=" + attributes.getValue("ref") + ",");
                    }
                    tmp = "node=" + attributes.getValue("ref") + ",";
                }

                String[] tuple = nodehash.get(attributes.getValue("ref"));
                if (tuple != null) {
                    l[2] = Double.parseDouble(tuple[0]);
                    l[3] = Double.parseDouble(tuple[1]);
                }
                if (l[0] != 0.0) {
                    // TODO s.th is wrong either here or in the way, distances are calculated
                    // in the OSMGraph-class
                    // distance = distance + distance(l[0], l[1], l[2], l[3]);
                    distance = -1;
                }
                l[0] = l[2];
                l[1] = l[3];
            } else if (qName.equals("tag")) {
                if (openedWay) {
                    if (findref == false) {
                        ways.write(tmp);
                        findref = true;
                    }
                    ways.write(attributes.getValue("k") + "=\"" + clean(attributes.getValue("v")) + "\",");
                } else if (openedNode) {
                    nodes.write("," + attributes.getValue("k") + "=\"" + clean(attributes.getValue("v")) + "\"");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private String clean(String s) {
        s = s.replace("\"", "'");
        return s;
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        try {
            if (qName.equals("way")) {
                openedWay = false;
                if (!findref) {
                    ways.write(tmp);
                }
                ways.write("distance=" + distance);
                ways.newLine();
                distance = 0;
            } else if (qName.equals("node")) {
                openedNode = false;
                nodes.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void endDocument() {
        try {
            nodes.close();
            ways.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public double distance(Double lat1, Double lon1, Double lat2, Double lon2) {
        // SOMEHOW this distfunction differs from the one  used in the graph method
        // :-(
        double r = 6371.009;
        double lat = (lat1 - lat2) * 3.1416 / 180;
        double lon = (lon1 - lon2) * 3.1416 / 180;
        double latm = (lat1 + lat2) / 2;
        double d = r * Math.sqrt(Math.pow(lat, 2) + Math.pow(lon * Math.cos(latm), 2));

        // now integrate elevation
        double heightDiff = getElevation(lat2, lon2) - getElevation(lat1, lon1);
        heightDiff /= 1000; // m -> km
        d = Math.sqrt(d * d + heightDiff * heightDiff);

        return d;
    }

    public static double getElevation(double lat, double lon) {
        int nlat = Math.abs((int) Math.floor(lat));
        int nlon = Math.abs((int) Math.floor(lon));
        File file = new File("");
        double val = 0;
        try {
            if (lat > 0 & lon > 0) {
                if (nlon < 10) {
                    if (nlat < 10) {
                        file = new File("N0" + nlat + "E00" + nlon + ".hgt");
                    } else {
                        file = new File("N" + nlat + "E00" + nlon + ".hgt");
                    }
                } else {
                    if (nlon < 100) {
                        if (nlat < 10) {
                            file = new File("N0" + nlat + "E0" + nlon + ".hgt");
                        } else {
                            file = new File("N" + nlat + "E0" + nlon + ".hgt");
                        }
                    } else {
                        if (nlat < 10) {
                            file = new File("N0" + nlat + "E" + nlon + ".hgt");
                        } else {
                            file = new File("N" + nlat + "E" + nlon + ".hgt");
                        }
                    }
                }

            }
            if (lat < 0 & lon > 0) {
                if (nlon < 10) {
                    if (nlat < 10) {
                        file = new File("S0" + nlat + "E00" + nlon + ".hgt");
                    } else {
                        file = new File("S" + nlat + "E00" + nlon + ".hgt");
                    }
                } else {
                    if (nlon < 100) {
                        if (nlat < 10) {
                            file = new File("S0" + nlat + "E0" + nlon + ".hgt");
                        } else {
                            file = new File("S" + nlat + "E0" + nlon + ".hgt");
                        }
                    } else {
                        if (nlat < 10) {
                            file = new File("S0" + nlat + "E" + nlon + ".hgt");
                        } else {
                            file = new File("S" + nlat + "E" + nlon + ".hgt");
                        }
                    }
                }

            }
            if (lat > 0 & lon < 0) {
                if (nlon < 10) {
                    if (nlat < 10) {
                        file = new File("N0" + nlat + "W00" + nlon + ".hgt");
                    } else {
                        file = new File("N" + nlat + "W00" + nlon + ".hgt");
                    }
                } else {
                    if (nlon < 100) {
                        if (nlat < 10) {
                            file = new File("N0" + nlat + "W0" + nlon + ".hgt");
                        } else {
                            file = new File("N" + nlat + "W0" + nlon + ".hgt");
                        }
                    } else {
                        if (nlat < 10) {
                            file = new File("N0" + nlat + "W" + nlon + ".hgt");
                        } else {
                            file = new File("N" + nlat + "W" + nlon + ".hgt");
                        }
                    }
                }

            }
            if (lat < 0 & lon < 0) {
                if (nlon < 10) {
                    if (nlat < 10) {
                        file = new File("S0" + nlat + "W00" + nlon + ".hgt");
                    } else {
                        file = new File("S" + nlat + "W00" + nlon + ".hgt");
                    }
                } else {
                    if (nlon < 100) {
                        if (nlat < 10) {
                            file = new File("S0" + nlat + "W0" + nlon + ".hgt");
                        } else {
                            file = new File("S" + nlat + "W0" + nlon + ".hgt");
                        }
                    } else {
                        if (nlat < 10) {
                            file = new File("N0" + nlat + "W" + nlon + ".hgt");
                        } else {
                            file = new File("N" + nlat + "W" + nlon + ".hgt");
                        }
                    }
                }

            }
            double ilat = getILat(lat);
            double ilon = getILon(lon);
            int rowmin = (int) Math.floor(ilon);   // srtm3 3src-seconds   X:lon.    Y:Lat.
            int colmin = (int) Math.floor(ilat);
            short[] values = new short[4];
            values[0] = getValues(file, rowmin, colmin);
            values[1] = getValues(file, rowmin + 1, colmin);
            values[2] = getValues(file, rowmin, colmin + 1);
            values[3] = getValues(file, rowmin + 1, colmin + 1);
            double coefrowmin = rowmin + 1 - ilon;
            double coefcolmin = colmin + 1 - ilat;
            double val1 = values[0] * coefrowmin + values[1] * (1 - coefrowmin);
            double val2 = values[2] * coefrowmin + values[3] * (1 - coefrowmin);
            val = val1 * coefcolmin + val2 * (1 - coefcolmin);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return val;
    }

    public static short readShort(BufferedInputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        return (short) ((ch1 << 8) + (ch2 << 0));
    }

    public static double getILat(double lat) {
        double dlat = lat - Math.floor(lat);    //  z.b. lat=33.4567895  >   dlat=0.4567895
        double ilat = dlat * 1200;   //  1200=3600/3-arc-second
        return ilat;
    }

    public static double getILon(double lon) {
        double dlon = lon - Math.floor(lon);
        double ilon = dlon * 1200;
        return ilon;
    }

    public static short getValues(File file, int rowmin, int colmin) throws
            Exception {
        if (!file.exists()) {
            log.info("SRTM file " + file.getName() + " not found. Trying to uncompress.");
            File zipped = new File(SRTMbase, file.getName() + ".zip");
            if (!zipped.exists()) {
                log.severe("couldn't find SRTM file: " + file.getName() + ".zip. Returning height 0");
                return 0;
            }

            { // uncompress
                ZipFile zipfile = new ZipFile(zipped, ZipFile.OPEN_READ);
                InputStream ins = zipfile.getInputStream(zipfile.getEntry(file.getName()));

                BufferedOutputStream fw = new BufferedOutputStream(new FileOutputStream(file), BUFFERED_SIZE);
                byte[] buffer = new byte[1024 * 1024]; // 3mb
                int l = 0;
                while ((l = ins.read(buffer)) > 0) {
                    fw.write(buffer, 0, l);
                }
                fw.close();
                file.deleteOnExit();
                log.info("Uncompressed file " + zipped.getName() + " to " + file.getName());
            }
        }

        if (!file.exists()) {
            throw new IOException("couldn't find SRTM file: " + file.getName() + "(.zip)");
        }

        SoftReference<BufferedInputStream> inRef = srtmMap.get(file);
        BufferedInputStream in = (inRef != null) ? inRef.get() : null;
        if (in == null) {
            // 4M because the SRTM Files are ABOUT 3MB
            int srtmbuffer = 4*1024*1024;
            in = new BufferedInputStream(new FileInputStream(file), srtmbuffer);
            srtmMap.put(file, new SoftReference<BufferedInputStream>(in));
            in.mark(srtmbuffer);
        }
        in.reset();

        long starti = ((1200 - colmin) * 2402) + rowmin * 2;
        in.skip(starti);
        short readShort = readShort(in);
        return readShort;
    }
}
