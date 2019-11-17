/* ====================================================================
 * /util/Configuration.java
 * 
 * (c) by Dirk Lehmann
 * ====================================================================
 */

package de.lehmannet.om.ui.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import de.lehmannet.om.ui.navigation.ObservationManager;

public class Configuration {

    private static final String CONFIG_FILE = "config";

    private static final String CONFIG_FILE_HEADER = "ObservationManager configfile" + "\n#Written by version: "
            + ObservationManager.VERSION + "\n#If you edit this, make sure you know what you're doing...";

    private Properties persistence = null;

    private boolean changed = false;

    public Configuration() {

        this(null);

    }

    public Configuration(String path) {

        try {
            this.loadConfiguration(path);
        } catch (IOException ioe) {
            System.out.println("Cannot find configuration file " + path + "\n" + ioe);
        }

    }

    private void loadConfiguration(String path) throws IOException {

        this.persistence = new Properties();

        path = this.getConfigPath(path) + File.separatorChar + CONFIG_FILE;
        if (!new File(path).exists()) {
            // No configuration found...must be first time user -> Create empty config
            return;
        }

        FileInputStream fis = new FileInputStream(path);
        BufferedInputStream bis = new BufferedInputStream(fis);
        try {
            this.persistence.load(bis);
        } catch (IOException ioe) {
            System.err.println("Cannot load configuration.\n" + ioe);
        }

    }

    public boolean saveConfiguration(String path) {

        if (!this.changed) {
            return true;
        }

        path = this.getConfigPath(path);
        new File(path).mkdirs(); // Create directories

        path = path + File.separatorChar + CONFIG_FILE;
        try {
            FileOutputStream fos = new FileOutputStream(path);
            this.persistence.store(new BufferedOutputStream(fos), Configuration.CONFIG_FILE_HEADER);
            fos.close();
        } catch (IOException ioe) {
            System.err.println("Cannot save configuration file " + path);
            return false;
        }

        return true;

    }

    public void setConfig(String key, String value) {

        if ((value == null) || ("".equals(value.trim()))) {
            this.persistence.remove(key);
            this.changed = true;
            return;
        }

        this.persistence.setProperty(key, value);
        this.changed = true;

    }

    public String getConfig(String key) {

        return this.persistence.getProperty(key);

    }

    public Set getConfigKeys() {

        return this.persistence.keySet();

    }

    public String getConfig(String key, String defaultValue) {

        return this.persistence.getProperty(key, defaultValue);

    }

    private String getConfigPath(String path) {

        if (path == null) { // Search in user home
            path = System.getProperty("user.home");
        }

        path = path + File.separatorChar + ObservationManager.WORKING_DIR;

        return path;

    }

}
