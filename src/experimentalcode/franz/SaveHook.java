package experimentalcode.franz;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * class for (re)storing image-, data- and plot directory
 *
 *  SaveHook s = new SaveHook(viewer);
 *  s.restore();
 *  Runtime.getRuntime().addShutdownHook(s);
 */
class SaveHook extends Thread {

    public static final String SETTINGS_FILE = "settings.properties";
    private Demo v;
    private File propFile;

    SaveHook(Demo v) {
        this.v = v;
        String home = System.getProperty("user.home");
        if (home != null) {
            propFile = new File(new File(home, ".PAROS"), SETTINGS_FILE);
        }
    }

    @SuppressWarnings("static-access")
    public void restore() throws IOException {
        if (propFile != null && propFile.exists()) {
            Properties p = new Properties();
            p.load(new FileReader(propFile));
            if (p.containsKey("lru.graph.dir")) {
                v.setLruDir(p.getProperty("lru.graph.dir"));
            }
            if (p.containsKey("autoload.graph")) {
                v.setAutoLoad(new Boolean(p.getProperty("autoload.graph")));
            }
        }
    }

    @Override
    @SuppressWarnings("static-access")
    public void run() {
        if (propFile != null) {
            propFile.getParentFile().mkdirs();
            Properties p = new Properties();
            p.setProperty("lru.graph.dir", v.getLruDir());
            p.setProperty("autoload.graph", Boolean.toString(v.isAutoLoad()));
            try {
                p.store(new FileWriter(propFile), null);
            } catch (IOException ex) {
                Logger.getLogger(SaveHook.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
