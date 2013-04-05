/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools.osgi;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Embedder configuration based on a particular filesystem layout. It supports
 * automatically installing or starting bundles found in a particular directory
 * structure. The directories under the bundle root are expected to be named like so:
 * {start-level}_{name}/{initial_state}.
 * E.g.:
 *
 * <pre>
 *     [root]/
 *          framework.properties
 *          001_system/
 *              start/
 *                  org.apache.felix.shell-1.2.0.jar
 *                  org.apache.felix.shell.tui-1.2.0.jar
 *                  ...
 *              install/
 *                  org.apache.felix.shell.remote-1.0.4.jar
 *                  ...
 *          002_infrastructure/
 *              start/
 *                  ...
 *              install/
 *                  ...
 * </pre>
 *
 * The <tt>_{name}</tt> part of the first level directory names is optional -- it's just for your
 * future reference. The <tt>{start-level}</tt> part is the desired start level for the bundles
 * as defined by the OSGi Start Level service. It must be a positive integer.
 * <p>
 * The optional file <tt>framework.properties</tt> defines any additional properties you need to
 * pass to {@link org.osgi.framework.launch.FrameworkFactory#newFramework}.
 *
 * @author Rhett Sutphin
 */
public class EmbedderFilesystemConfiguration implements EmbedderConfiguration {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final Pattern INITIAL_DIGITS = Pattern.compile("^\\d+");

    private File root;

    private Map<String, Object> frameworkProperties;
    private Collection<InstallableBundle> installableBundles;

    public EmbedderFilesystemConfiguration() {
    }

    public EmbedderFilesystemConfiguration(File root) {
        this.setRoot(root);
    }

    public Map<String, Object> getFrameworkProperties() {
        if (frameworkProperties == null) {
            loadFrameworkProperties();
        }

        return frameworkProperties;
    }

    @SuppressWarnings( { "unchecked" })
    private synchronized void loadFrameworkProperties() {
        if (frameworkProperties != null) return;
        File propFile = new File(getRoot(), "framework.properties");
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(propFile));
        } catch (FileNotFoundException e) {
            log.debug("No properties file ({}) in embedder configuration root", propFile);
            frameworkProperties = Collections.emptyMap();
        } catch (IOException e) {
            throw new StudyCalendarSystemException("Could not read %s", propFile, e);
        }
        replaceDynamicProperties(props);
        if (frameworkProperties == null) {
            frameworkProperties = new HashMap(props);
        }
    }

    @SuppressWarnings( { "unchecked" })
    private void replaceDynamicProperties(Properties props) {
        Enumeration<String> names = (Enumeration<String>) props.propertyNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            String value = props.getProperty(name);
            props.setProperty(name, value.replaceAll("\\$\\{root\\}", getRoot().getAbsolutePath()));
        }
    }

    public Collection<InstallableBundle> getBundlesToInstall() {
        if (installableBundles == null) {
            loadInstallableBundles();
        }

        return installableBundles;
    }

    private synchronized void loadInstallableBundles() {
        if (installableBundles != null) return;
        installableBundles = new LinkedList<InstallableBundle>();

        for (File levelDir : getRoot().listFiles(LevelDirectoryFilter.INSTANCE)) {
            installableBundles.addAll(findBundlesForLevel(levelDir));
        }
    }

    private Collection<InstallableBundle> findBundlesForLevel(File levelDir) {
        Collection<InstallableBundle> bundles = new LinkedList<InstallableBundle>();
        int level = extractLevelFromDirectory(levelDir.getName());

        findBundlesForState(bundles, new File(levelDir, "install"), level, false);
        findBundlesForState(bundles, new File(levelDir, "start"), level, true);

        return bundles;
    }

    private void findBundlesForState(
        Collection<InstallableBundle> bundles, File stateDir, int level, boolean shouldStart
    ) {
        File[] jars = stateDir.listFiles(new JarFilter());
        if (jars == null) return;
        for (File jar : jars) {
            bundles.add(new InstallableBundleImpl(level, jar.toURI().toString(), shouldStart));
        }
    }

    private int extractLevelFromDirectory(String name) {
        Matcher matcher = INITIAL_DIGITS.matcher(name);
        matcher.find();
        return Integer.parseInt(matcher.group(0));
    }

    ////// CONFIGURATION

    public void setRoot(File baseDir) {
        this.root = baseDir;
    }

    public File getRoot() {
        return root;
    }

    private static class LevelDirectoryFilter implements FileFilter {
        public static final FileFilter INSTANCE = new LevelDirectoryFilter();

        private LevelDirectoryFilter() { }

        public boolean accept(File file) {
            return file.isDirectory() && Character.isDigit(file.getName().charAt(0));
        }
    }

    private static class JarFilter implements FileFilter {
        public static final FileFilter INSTANCE = new LevelDirectoryFilter();

        private JarFilter() { }

        public boolean accept(File file) {
            return file.isFile() && file.getName().endsWith(".jar");
        }
    }
}
