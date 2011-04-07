package edu.northwestern.bioinformatics.studycalendar.tools.osgi;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.service.startlevel.StartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A generic OSGi framework embedder based on OSGi R4.2's framework.launch API. Bundles to
 * install/start at framework initialization can be defined using an {@EmbedderConfiguration}.
 *
 * @author Rhett Sutphin
 */
public class Embedder {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private FrameworkFactory frameworkFactory;
    private EmbedderConfiguration configuration;

    private Framework framework;

    /**
     * Initialize and start the embedded OSGi framework instance. When this method returns,
     * all the configured bundles will be installed at the specified start levels and the start
     * level will be the maximum start level for any bundle in the configuration.
     */
    public BundleContext start() {
        StartupWatcher watcher;
        SortedMap<Integer, Collection<InstallableBundle>> byStartLevel =
            partition(getConfiguration().getBundlesToInstall());

        try {
            framework = getFrameworkFactory().newFramework(getConfiguration().getFrameworkProperties());
            log.debug("Initializing OSGi framework {}", framework);
            framework.init();

            watcher = createWatcher(byStartLevel.lastKey());
            framework.getBundleContext().addFrameworkListener(watcher);

            log.debug("Starting OSGi framework {}", framework);
            framework.start();

            StartLevel startLevelService = getStartLevelService(framework.getBundleContext());
            for (Integer startLevel : byStartLevel.keySet()) {
                installAndStartOneLevel(startLevelService, startLevel, byStartLevel.get(startLevel));
            }
        } catch (BundleException be) {
            throw new StudyCalendarSystemException("Could not start embedded OSGi layer", be);
        }

        watcher.waitForStart(30 * 1000);
        return framework.getBundleContext();
    }

    // package level for overrides when testing
    StartupWatcher createWatcher(int expectedStartLevel) {
        return new StartupWatcher(expectedStartLevel);
    }

    private static StartLevel getStartLevelService(BundleContext bundleContext) {
        ServiceReference ref =
            bundleContext.getServiceReference(StartLevel.class.getName());
        if (ref == null) {
            throw new StudyCalendarSystemException(
                "Could not get a reference to an instance of the start level service");
        }
        return (StartLevel) bundleContext.getService(ref);
    }

    private void installAndStartOneLevel(
        StartLevel startLevelService, Integer startLevel, Collection<InstallableBundle> installableBundles
    ) throws BundleException {
        List<Bundle> toStart = new ArrayList<Bundle>(installableBundles.size());
        for (InstallableBundle bundle : installableBundles) {
            log.debug("Installing {} at level {}", bundle.getLocation(), startLevel);
            Bundle installed = framework.getBundleContext().installBundle(bundle.getLocation());
            startLevelService.setBundleStartLevel(installed, startLevel);
            if (bundle.getShouldStart()) toStart.add(installed);
        }

        log.debug("Marking bundles in level {} as startable", startLevel);
        // Defer starting so that all bundles in the same level are available before trying to
        // start any of them. (Start level controls take care of this for all levels except for 1,
        // so this is just for the benefit of that level.)
        for (Bundle bundle : toStart) bundle.start();

        log.debug("Requesting that current start level be {}", startLevel);
        startLevelService.setStartLevel(startLevel);
    }

    private SortedMap<Integer, Collection<InstallableBundle>> partition(
        Collection<InstallableBundle> installable
    ) {
        SortedMap<Integer, Collection<InstallableBundle>> partitioned =
            new TreeMap<Integer, Collection<InstallableBundle>>();
        for (InstallableBundle installableBundle : installable) {
            int level = installableBundle.getStartLevel();
            if (!partitioned.containsKey(level)) {
                partitioned.put(level, new LinkedList<InstallableBundle>());
            }
            partitioned.get(level).add(installableBundle);
        }
        return partitioned;
    }

    public void stop() {
        if (framework == null) return;
        try {
            log.debug("Requesting that OSGi framework stop");
            framework.stop();
            log.debug("Waiting for OSGi framework to stop");
            framework.waitForStop(15000);
        } catch (BundleException e) {
            throw new StudyCalendarSystemException("Stopping the embedded OSGi layer failed", e);
        } catch (InterruptedException e) {
            throw new StudyCalendarSystemException("Interrupted while waiting for the embedded OSGi layer to stop", e);
        }
    }

    ////// CONFIGURATION

    public EmbedderConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(EmbedderConfiguration configuration) {
        this.configuration = configuration;
    }

    public FrameworkFactory getFrameworkFactory() {
        return frameworkFactory;
    }

    public void setFrameworkFactory(FrameworkFactory frameworkFactory) {
        this.frameworkFactory = frameworkFactory;
    }
}
