/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools.osgi;

import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.Version;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.service.startlevel.StartLevel;

import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Rhett Sutphin
 */
public class EmbedderTest {
    private Embedder embedder;
    private StaticEmbedderConfiguration configuration;

    private FrameworkFactory frameworkFactory;
    private MockFramework framework;
    private InstallingMockBundleContext bundleContext;
    private StartLevel startLevelService;

    private MockRegistry mocks = new MockRegistry();

    @Before
    @SuppressWarnings( { "RawUseOfParameterizedType" })
    public void before() throws Exception {
        startLevelService = new MockStartLevel();
        bundleContext = new InstallingMockBundleContext(startLevelService);
        framework = new MockFramework(bundleContext);

        frameworkFactory = mocks.registerMockFor(FrameworkFactory.class);
        expect(frameworkFactory.newFramework((Map) notNull())).andStubReturn(framework);

        configuration = new StaticEmbedderConfiguration();
        embedder = new Embedder() {
            @Override
            StartupWatcher createWatcher(int expectedStartLevel) {
                return new StartupWatcher(expectedStartLevel) {
                    @Override public void waitForStart(long timeoutMilliseconds) { }
                };
            }
        };
        embedder.setConfiguration(configuration);
        embedder.setFrameworkFactory(frameworkFactory);
    }

    @Test
    public void bundleIsInstalledFromSpecifiedLocation() throws Exception {
        configuration.getBundlesToInstall().add(new InstallableBundleImpl(1, "file://example.jar", false));
        doStart();

        assertThat(bundleContext.bundlesInstalled().get(0).getLocation(), is("file://example.jar"));
    }

    @Test
    public void startsStartedBundle() throws Exception {
        configuration.getBundlesToInstall().add(new InstallableBundleImpl(1, "file://example", true));
        doStart();

        assertThat(bundleContext.bundlesInstalled().get(0).getStartFlags(), is(0));
    }

    @Test
    public void doesNotStartNonstartedBundle() throws Exception {
        configuration.getBundlesToInstall().add(new InstallableBundleImpl(1, "file://example", false));
        doStart();

        assertThat(bundleContext.bundlesInstalled().get(0).isStarted(), is(false));
    }

    @Test
    public void startLevelSetForInstalledBundle() throws Exception {
        configuration.getBundlesToInstall().add(new InstallableBundleImpl(4, "file://example", false));
        doStart();

        Bundle installedBundle = bundleContext.bundlesInstalled().get(0);
        assertThat(startLevelService.getBundleStartLevel(installedBundle), is(4));
    }

    @Test
    public void installsBundlesByStartLevel() throws Exception {
        configuration.getBundlesToInstall().add(new InstallableBundleImpl(1, "file://one", false));
        configuration.getBundlesToInstall().add(new InstallableBundleImpl(3, "file://two", false));
        configuration.getBundlesToInstall().add(new InstallableBundleImpl(1, "file://three", false));
        configuration.getBundlesToInstall().add(new InstallableBundleImpl(2, "file://four", false));
        doStart();

        assertThat(bundleContext.bundlesInstalled().get(0).getLocation(), is("file://one"));
        assertThat(bundleContext.bundlesInstalled().get(1).getLocation(), is("file://three"));
        assertThat(bundleContext.bundlesInstalled().get(2).getLocation(), is("file://four"));
        assertThat(bundleContext.bundlesInstalled().get(3).getLocation(), is("file://two"));
    }

    @Test
    public void startLevelSetToMaxLevelOfAllBundles() throws Exception {
        configuration.getBundlesToInstall().add(new InstallableBundleImpl(9, "file://example", false));
        configuration.getBundlesToInstall().add(new InstallableBundleImpl(2, "file://example2", false));
        doStart();

        assertThat(startLevelService.getStartLevel(), is(9));
    }

    @Test
    public void providedConfigurationPropertiesAreUsed() throws Exception {
        configuration.getBundlesToInstall().add(new InstallableBundleImpl(1, "file://example", false));
        configuration.getFrameworkProperties().put("something", "here");

        expect(frameworkFactory.newFramework(configuration.getFrameworkProperties())).
            andReturn(framework);
        doStart();

        // mock verify is sufficient
    }

    private void doStart() {
        mocks.replayMocks();
        embedder.start();
        mocks.verifyMocks();
    }

    private static class MockFramework extends RecordingMockBundle implements Framework {
        private MockFramework(BundleContext bundleContext) {
            super("System Bundle", bundleContext);
            setBundleId(0);
        }

        public void init() throws BundleException {
        }

        public FrameworkEvent waitForStop(long l) throws InterruptedException {
            throw new UnsupportedOperationException();
        }

        public Map getSignerCertificates(int i) {
            throw new UnsupportedOperationException("getSignerCertificates not implemented");
            // return null;
        }

        public Version getVersion() {
            throw new UnsupportedOperationException("getVersion not implemented");
            // return null;
        }
    }

}
