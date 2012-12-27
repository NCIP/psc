/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools.osgi;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author Rhett Sutphin
 */
public class EmbedderFilesystemConfigurationTest {
    private EmbedderFilesystemConfiguration configuration;

    @Before
    public void before() throws Exception {
        configuration = new EmbedderFilesystemConfiguration();

        setTestRootDir("embedder_test_root");
    }

    @Test
    public void itIncludesThePropertiesFromTheFile() throws Exception {
        assertThat(configuration.getFrameworkProperties(), hasEntry("foo.quux", (Object) "baz"));
    }

    @Test
    public void itReplacesTheRootPropertyWithTheConfiguredRoot() throws Exception {
        assertThat(configuration.getFrameworkProperties(),
            hasEntry("something", (Object)
                format("%s/quux", configuration.getRoot().getAbsolutePath())));
    }

    @Test
    public void itHasNoPropertiesWhenFrameworkPropertiesFileNotPresent() throws Exception {
        setTestRootDir("embedder_empty_test_root");
        assertThat(configuration.getFrameworkProperties().isEmpty(), is(true));
    }

    @Test
    public void itHasNoPropertiesWhenTheTestRootDoesNotExist() throws Exception {
        configuration.setRoot(new File("does/not/exist"));
        assertThat(configuration.getFrameworkProperties().isEmpty(), is(true));
    }

    @Test
    public void itFindsAllTheLevelDirectories() throws Exception {
        Set<Integer> actualLevels = new HashSet<Integer>();
        for (InstallableBundle installableBundle : configuration.getBundlesToInstall()) {
            actualLevels.add(installableBundle.getStartLevel());
        }

        assertThat(actualLevels.size(), is(3));
        assertThat(actualLevels, hasItem(1));
        assertThat(actualLevels, hasItem(2));
        assertThat(actualLevels, hasItem(4));
    }

    @Test
    public void ifProvidesNoBundlesIfThereAreNoBundleDirectories() throws Exception {
        setTestRootDir("embedder_empty_test_root");
        assertThat(configuration.getBundlesToInstall().isEmpty(), is(true));
    }

    @Test
    public void itFindsAllTheBundles() throws Exception {
        assertThat(configuration.getBundlesToInstall().size(), is(8));
    }

    @Test
    public void itProvidesLocationsAsFileUrls() throws Exception {
        InstallableBundle oneOfThem = configuration.getBundlesToInstall().iterator().next();
        URI parsed = new URI(oneOfThem.getLocation());
        assertThat(parsed.getScheme(), is("file"));
    }

    @Test
    public void itIgnoresFilesThatAreNotJars() throws Exception {
        Collection<String> allFilenames = collectLocationBasenames();
        assertThat(allFilenames, not(hasItem("B-3.foo")));
    }

    @Test
    public void itIgnoresJarsThatAreNotInLevelDirectories() throws Exception {
        Collection<String> allFilenames = collectLocationBasenames();
        assertThat(allFilenames, not(hasItem("Z.jar")));
    }

    @Test
    public void itAppliesTheStartFlagIfInTheStartDirectory() throws Exception {
        assertBundleExists(4, "P-1.jar", true);
    }

    @Test
    public void itDoesNotApplyTheStartFlagIfInTheInstallDirectory() throws Exception {
        assertBundleExists(4, "P-3.jar", false);
    }

    @Test
    public void itHandlesLevelsThatOnlyHaveOneStateDirectory() throws Exception {
        assertBundleExists(2, "B-2.jar", false);
    }

    private void assertBundleExists(int expectedLevel, String expectedBasename, boolean start) {
        for (InstallableBundle bundle : configuration.getBundlesToInstall()) {
            if (bundle.getShouldStart() && start ||
                bundle.getStartLevel() == expectedLevel ||
                locationBasename(bundle).equals(expectedBasename)
            ) {
                return;
            }
        }

        fail("No bundle with " + expectedLevel + "; " + expectedBasename + "; " + start);
    }

    private Collection<String> collectLocationBasenames() {
        List<String> names = new ArrayList<String>(configuration.getBundlesToInstall().size());
        for (InstallableBundle bundle : configuration.getBundlesToInstall()) {
            names.add(locationBasename(bundle));
        }
        return names;
    }

    private String locationBasename(InstallableBundle bundle) {
        String loc = bundle.getLocation();
        int slash = loc.lastIndexOf('/');
        return loc.substring(slash + 1);
    }

    private void setTestRootDir(String rootDirName) throws FileNotFoundException {
        File root = new File("src/test/java/" + rootDirName);
        if (!root.exists()) {
            root = new File("utility/" + root.getPath());
            if (!root.exists()) {
                throw new FileNotFoundException("Could not locate the test root " + root);
            }
        }

        configuration.setRoot(root);
    }
}
