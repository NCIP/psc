package edu.northwestern.bioinformatics.studycalendar.osgi.plugininstaller.internal;

import org.osgi.framework.Bundle;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class PluginInstaller {
    public static final String FILEINSTALL_FACTORY_PID = "org.apache.felix.fileinstall";

    private final Logger log = LoggerFactory.getLogger(getClass());
    private ConfigurationAdmin admin;
    private Bundle fileinstallBundle;

    private static File basePath, configurationsPath, pluginsPath, librariesPath;
    private Map<String, String> pluginsOptions, librariesOptions;

    public static File basePath() {
        if (basePath == null) {
            File catalinaBase = new File(System.getProperty("catalina.base"));
            String sep = System.getProperty("file.separator");
            basePath = new File(catalinaBase, String.format("conf%spsc%sbundles", sep, sep));
        }
        return basePath;
    }

    public static File configurationsPath() {
        if (configurationsPath == null) {
            configurationsPath = new File(basePath(), "configurations");
        }
        return configurationsPath;
    }

    public static File pluginsPath() {
        if (pluginsPath == null) {
            pluginsPath = new File(basePath(), "plugins");
        }
        return pluginsPath;
    }

    public static File librariesPath() {
        if (librariesPath == null) {
            librariesPath = new File(basePath(), "libraries");
        }
        return librariesPath;
    }

    public PluginInstaller(ConfigurationAdmin admin, Bundle fileinstallBundle) {
        this.admin = admin;
        this.fileinstallBundle = fileinstallBundle;

        this.pluginsOptions = new HashMap<String, String>();
        this.pluginsOptions.put("felix.fileinstall.bundles.new.start", "true");
        this.pluginsOptions.put("felix.fileinstall.start.level", "25");

        this.librariesOptions = new HashMap<String, String>();
        this.librariesOptions.put("felix.fileinstall.bundles.new.start", "false");
        this.librariesOptions.put("felix.fileinstall.start.level", "24");
    }

    public void startWatchers() throws IOException {
        startFileinstall(configurationsPath(), ".*\\.cfg$");
        startFileinstall(pluginsPath(), ".*\\.jar$", pluginsOptions);
        startFileinstall(librariesPath(), ".*\\.jar$", librariesOptions);
    }

    private void startFileinstall(File path, String pattern) throws IOException {
        startFileinstall(path, pattern, Collections.<String, String>emptyMap());
    }

    private void startFileinstall(
        File path, String pattern, Map<String, String> additionalProps
    ) throws IOException {
        log.info("Creating fileinstall configuration for {}", path.getCanonicalPath());
        Configuration configuration =
            admin.createFactoryConfiguration(
                FILEINSTALL_FACTORY_PID, fileinstallBundle.getLocation());

        Dictionary<String, String> props = new Hashtable<String, String>(additionalProps);
        props.put("felix.fileinstall.dir", path.getCanonicalPath());
        props.put("felix.fileinstall.filter", pattern);
        props.put("felix.fileinstall.noInitialDelay", "true");
        props.put("felix.fileinstall.log.level", "4");

        log.info("- using properties {}", props);
        configuration.update(props);
    }
}
