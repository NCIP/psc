/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.osgi.plugininstaller;

import edu.northwestern.bioinformatics.studycalendar.osgi.OsgiLayerIntegratedTestCase;
import edu.northwestern.bioinformatics.studycalendar.osgi.plugininstaller.internal.PluginInstaller;
import org.apache.commons.io.FileUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.startlevel.StartLevel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.osgi.OsgiLayerIntegratedTestHelper.*;

/**
 * @author Rhett Sutphin
 */
public class PluginInstallerTest extends OsgiLayerIntegratedTestCase {
    private static final String OUTSIDE_BUNDLE_NAME = "com.example.outside-bundle";
    private static final long WAIT_FOR_FILEINSTALL = 5000L * (System.getenv("JOB_NAME") == null ? 1 : 10);

    private File outsideBundleFilename;
    private List<File> paths;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        outsideBundleFilename = new File(
            getModuleRelativeDirectory("osgi-layer:integrated-tests", "src/test/resources"),
            "outsideBundle.jar");
        // stop host beans so that the (un-set-up) persistence manager doesn't interfere with things
        stopBundle("edu.northwestern.bioinformatics.psc-osgi-layer-host-services");

        paths = Arrays.asList(
            PluginInstaller.pluginsPath(),
            PluginInstaller.librariesPath(),
            PluginInstaller.configurationsPath()
        );

        for (File path : paths) {
            //noinspection ResultOfMethodCallIgnored
            path.mkdirs();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        for (File path : paths) {
            FileUtils.deleteDirectory(path);
        }
        Thread.sleep(WAIT_FOR_FILEINSTALL);
        // re-start in case any other tests depend on it
        startBundle("edu.northwestern.bioinformatics.psc-osgi-layer-host-services");
    }

    public void testStartsBundlesInPluginDirectory() throws Exception {
        deployOutsideBundleAndWait(PluginInstaller.pluginsPath());

        assertEquals("Not started", Bundle.ACTIVE, findBundle(OUTSIDE_BUNDLE_NAME).getState());
    }

    public void testSetsStartLevelTo25ForBundlesInPluginDirectory() throws Exception {
        deployOutsideBundleAndWait(PluginInstaller.pluginsPath());

        assertEquals("Wrong level", 25,
            getStartLevelService().getBundleStartLevel(findBundle(OUTSIDE_BUNDLE_NAME)));
    }

    public void testDoesNotStartBundlesInLibraryDirectory() throws Exception {
        deployOutsideBundleAndWait(PluginInstaller.librariesPath());

        assertEquals("Incorrectly started", Bundle.INSTALLED, findBundle(OUTSIDE_BUNDLE_NAME).getState());
    }

    public void testSetsStartLevelTo24ForBundlesInLibraryDirectory() throws Exception {
        deployOutsideBundleAndWait(PluginInstaller.librariesPath());

        assertEquals("Wrong level", 24,
            getStartLevelService().getBundleStartLevel(findBundle(OUTSIDE_BUNDLE_NAME)));
    }

    private void deployOutsideBundleAndWait(File deployTo) throws IOException, InterruptedException {
        FileUtils.copyFileToDirectory(outsideBundleFilename, deployTo);
        System.out.println("Waiting for " + (WAIT_FOR_FILEINSTALL / 1000.0) + 's');
        Thread.sleep(WAIT_FOR_FILEINSTALL);
    }

    private StartLevel getStartLevelService() throws IOException {
        ServiceReference ref = getBundleContext().getServiceReference(StartLevel.class.getName());
        assertNotNull("Can't find start level service", ref);
        return (StartLevel) getBundleContext().getService(ref);
    }

    public void testAppliesConfigurationsInConfigurationDirectory() throws Exception {
        String servicePid = "psc.mocks.echo.A";
        startBundle(
            "edu.northwestern.bioinformatics.psc-osgi-layer-mock", ManagedService.class.getName());

        FileUtils.writeStringToFile(
            new File(PluginInstaller.configurationsPath(), servicePid + ".cfg"),
            "psc.mocks.String=quuxbar\n"
        );
        Thread.sleep(WAIT_FOR_FILEINSTALL);

        ServiceReference[] refs = getBundleContext().getAllServiceReferences(
            ManagedService.class.getName(), String.format("(%s=%s)", Constants.SERVICE_PID, servicePid));
        assertEquals("Wrong services found: " + Arrays.asList(refs), 1, refs.length);
        assertEquals("Configuration not applied",
            "quuxbar", refs[0].getProperty("psc.mocks.String"));
    }
}
