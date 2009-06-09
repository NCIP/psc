package org.dynamicjava.osgi.da_launcher.internal;

import org.dynamicjava.osgi.da_launcher.LauncherSettings;
import org.dynamicjava.osgi.da_launcher.internal.bundle.BundleGroupBasedBundleManager;
import org.dynamicjava.osgi.da_launcher.internal.bundle.BundleManager;
import org.dynamicjava.osgi.da_launcher.internal.config.LauncherConfig;
import org.dynamicjava.osgi.da_launcher.internal.config.LoggingConfig;
import org.dynamicjava.osgi.da_launcher.internal.startup.FilesIntegrityValidator;
import org.dynamicjava.osgi.da_launcher.internal.startup.LauncherConfigLoader;
import org.dynamicjava.osgi.da_launcher.internal.startup.LoggersInitializer;
import org.dynamicjava.osgi.da_launcher.internal.startup.OsgiFrameworkLoader;

public class LauncherObjectsFactory {
	
	public LoggersInitializer createLoggersInitializer(LoggingConfig loggingConfig) {
		return new LoggersInitializer(getSettings(), loggingConfig);
	}
	
	public FilesIntegrityValidator createFilesIntegrityValidator() {
		return new FilesIntegrityValidator(getSettings());
	}
	
	public OsgiFrameworkLoader createOsgiFrameworkLoader(LauncherConfig launcherConfig) {
		return new OsgiFrameworkLoader(getSettings(), launcherConfig);
	}
	
	public LauncherConfigLoader createLauncherConfigLoader() {
		return new LauncherConfigLoader(getSettings());
	}
	
	public BundleManager createBundleManager(LauncherContext context) {
		//return new DefaultBundleManager(context, this);
		return new BundleGroupBasedBundleManager(context, this);
	}
	
	
	public LauncherObjectsFactory(LauncherSettings settings) {
		this.settings = settings;
	}
	
	private final LauncherSettings settings;
	protected LauncherSettings getSettings() {
		return settings;
	}
	
}
