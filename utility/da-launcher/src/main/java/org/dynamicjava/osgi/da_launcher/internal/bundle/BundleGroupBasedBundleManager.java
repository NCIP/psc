package org.dynamicjava.osgi.da_launcher.internal.bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dynamicjava.osgi.commons.utilities.ShutdownableState;
import org.dynamicjava.osgi.da_launcher.internal.InternalLauncherConstants;
import org.dynamicjava.osgi.da_launcher.internal.LauncherContext;
import org.dynamicjava.osgi.da_launcher.internal.LauncherObjectsFactory;
import org.dynamicjava.osgi.da_launcher.internal.bundle.group.BundleGroup;
import org.dynamicjava.osgi.da_launcher.internal.config.BundleGroupsConfig;

public class BundleGroupBasedBundleManager implements BundleManager {
	
	//@Override
	public void run() {
		getShutdownableState().run();
		
		activateBundleGroups();
	}
	
	//@Override
	public void shutdown() {
		BundleGroup[] bundleGroups = getBundleGroups().toArray(new BundleGroup[0]);
		Arrays.sort(bundleGroups, getBundleGroupStartOrderComparator());
		
		/// We stop bundle groups in the reverse of start order
		for (int i = bundleGroups.length - 1; i >= 0; i--) {
			try {
				bundleGroups[i].stop();
			} catch (Throwable ex) {
				// TODO place bundle group name
				logger.log(Level.WARNING, "Failed to stop bundle group", ex);
			}
		}
		
		getShutdownableState().shutdown();
	}
	
	
	protected void activateBundleGroups() {
		BundleGroup[] bundleGroups = getBundleGroups().toArray(new BundleGroup[0]);
		Arrays.sort(bundleGroups, getBundleGroupInstallOrderComparator());
		
		for (BundleGroup bundleGroup : bundleGroups) {
			bundleGroup.start(true, false);
		}
		
		Arrays.sort(bundleGroups, getBundleGroupStartOrderComparator());
		for (BundleGroup bundleGroup : bundleGroups) {
			if (bundleGroup.getSettings().isAutoStartBundles()) {
				bundleGroup.startGroupBundles();
			}
		}
	}
	
	
	public BundleGroupBasedBundleManager(LauncherContext launcherContext,
			LauncherObjectsFactory launcherObjectsFactory) {
		this.launcherContext = launcherContext;
		this.launcherObjectsFactory = launcherObjectsFactory;
		init();
	}
	
	protected void init() {
		BundleGroupsConfig bundleGroupsConfig = getLauncherContext().getConfig().getBundleGroupsConfig();
		if (bundleGroupsConfig == null) {
			bundleGroupsConfig = new BundleGroupsConfig(getLauncherContext());
			bundleGroupsConfig.load(new File(
					getLauncherContext().getSettings().getFiles().getBundleGroupsConfigFile()));
		}
		
		setBundleGroups(new ArrayList<BundleGroup>(bundleGroupsConfig.getBundleGroups()));
	}
	
	
	private final LauncherContext launcherContext;
	public LauncherContext getLauncherContext() {
		return launcherContext;
	}
	
	private final LauncherObjectsFactory launcherObjectsFactory;
	protected LauncherObjectsFactory getLauncherObjectsFactory() {
		return launcherObjectsFactory;
	}
	
	private List<BundleGroup> bundleGroups;
	protected List<BundleGroup> getBundleGroups() {
		return bundleGroups;
	}
	protected void setBundleGroups(List<BundleGroup> bundleGroups) {
		this.bundleGroups = bundleGroups;
	}
	
	private final ShutdownableState shutdownableState = new ShutdownableState("Bundle Manager");
	protected ShutdownableState getShutdownableState() {
		return shutdownableState;
	}
	
	
	Comparator<? super BundleGroup> bundleGroupInstallOrderComparator = new Comparator<BundleGroup>() {
		public int compare(BundleGroup bundleGroup1, BundleGroup bundleGroup2){
			return new Integer(bundleGroup1.getSettings().getInstallOrder()).compareTo(
					new Integer(bundleGroup2.getSettings().getInstallOrder()));
		}
	};
	protected Comparator<? super BundleGroup> getBundleGroupInstallOrderComparator() {
		return bundleGroupInstallOrderComparator;
	}
	
	Comparator<? super BundleGroup> bundleGroupStartOrderComparator = new Comparator<BundleGroup>() {
		public int compare(BundleGroup bundleGroup1, BundleGroup bundleGroup2){
			return new Integer(bundleGroup1.getSettings().getInstallOrder()).compareTo(
					new Integer(bundleGroup2.getSettings().getInstallOrder()));
		}
	};
	protected Comparator<? super BundleGroup> getBundleGroupStartOrderComparator() {
		return bundleGroupStartOrderComparator;
	}
	
	
	protected static final Logger logger = Logger.getLogger(InternalLauncherConstants.Loggers.BUNDLE_LIFECYCLE_EVENTS);
	
}
