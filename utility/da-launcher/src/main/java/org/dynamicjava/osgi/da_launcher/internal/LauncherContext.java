package org.dynamicjava.osgi.da_launcher.internal;

import org.dynamicjava.osgi.da_launcher.LauncherSettings;
import org.dynamicjava.osgi.da_launcher.internal.config.LauncherConfig;
import org.osgi.framework.BundleContext;

public interface LauncherContext {
	
	/**
	 * Returns the settings of the DA-Launcher.
	 */
	LauncherSettings getSettings();
	
	/**
	 * Returns the configurations specified by the user.
	 */
	LauncherConfig getConfig();
	
	/**
	 * Returns the bundle context of the launched OSGi framework.
	 */
	BundleContext getBundleContext();
	
	String processProperty(String propertyValue);
	
}
