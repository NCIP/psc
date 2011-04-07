package edu.northwestern.bioinformatics.studycalendar.tools.osgi;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.service.startlevel.StartLevel;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Attempts to use the {@Embedder} with whatever OSGi framework implementation
 * is on the classpath.
 *
 * @author Rhett Sutphin
 */
public class EmbedderIntegrationTest {
    private Embedder embedder;
    private BundleContext actualBundleContext;

    @Before
    public void before() throws Exception {
        EmbedderConfiguration configuration = new StaticEmbedderConfiguration();
        configuration.getBundlesToInstall().add(new InstallableBundleImpl(1, findClasspathJar("slf4j-api"), false));
        configuration.getBundlesToInstall().add(new InstallableBundleImpl(2, findClasspathJar("logback-core"), false));
        configuration.getBundlesToInstall().add(new InstallableBundleImpl(2, findClasspathJar("logback-classic"), false));

        embedder = new Embedder();
        embedder.setConfiguration(configuration);
        embedder.setFrameworkFactory(getFrameworkFactory());

        actualBundleContext = embedder.start();
    }

    @After
    public void after() throws Exception {
        embedder.stop();
    }

    private String findClasspathJar(String jarnameSubstring) {
        String[] classpathEntries = System.getProperty("java.class.path").split(File.pathSeparator);
        for (String classpathEntry : classpathEntries) {
            if (classpathEntry.contains(jarnameSubstring)) {
                return new File(classpathEntry).toURI().toString();
            }
        }
        throw new IllegalStateException(
            "Could not find anything in the classpath matching " + jarnameSubstring);
    }

    // adapted from http://felix.apache.org/site/apache-felix-framework-launching-and-embedding.html
    private FrameworkFactory getFrameworkFactory() throws Exception {
        URL url = getClass().getClassLoader().getResource(
            "META-INF/services/org.osgi.framework.launch.FrameworkFactory");
        if (url != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            try {
                for (String s = br.readLine(); s != null; s = br.readLine()) {
                    s = s.trim();
                    // Try to load first non-empty, non-commented line.
                    if ((s.length() > 0) && (s.charAt(0) != '#')) {
                        return (FrameworkFactory) Class.forName(s).newInstance();
                    }
                }
            } finally {
                if (br != null) br.close();
            }
        }

        throw new IllegalStateException("Could not find framework factory.");
    }

    @Test
    public void itHasTheExpectedNumberOfBundles() throws Exception {
        Bundle[] actualBundles = actualBundleContext.getBundles();
        assertThat(actualBundles.length, is(4));
    }

    @Test
    public void itIsAtTheExpectedRunLevel() throws Exception {
        assertThat(getStartLevelService().getStartLevel(), is(2));
    }

    @Test
    public void itUsesTheSpecifiedRunLevels() throws Exception {
        Bundle logbackClassic = null;
        for (Bundle candidate : actualBundleContext.getBundles()) {
            if ("ch.qos.logback.classic".equals(candidate.getSymbolicName())) {
                logbackClassic = candidate; break;
            }
        }
        if (logbackClassic == null) throw new IllegalStateException("Missing expected bundle");

        assertThat(getStartLevelService().getBundleStartLevel(logbackClassic), is(2));
    }

    private StartLevel getStartLevelService() {
        ServiceReference ref = actualBundleContext.getServiceReference(StartLevel.class.getName());
        return (StartLevel) actualBundleContext.getService(ref);
    }
}
