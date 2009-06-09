package org.dynamicjava.osgi.da_launcher.internal.support;

import org.dynamicjava.osgi.commons.utilities.StringUtils;
import org.dynamicjava.osgi.da_launcher.LauncherSettings;
import org.dynamicjava.osgi.da_launcher.internal.LauncherContext;
import org.dynamicjava.osgi.da_launcher.internal.config.LauncherConfig;
import org.osgi.framework.BundleContext;

public class DefaultLauncherContext implements LauncherContext {
	
	//@Override
	public LauncherSettings getSettings() {
		return settings;
	}
	private LauncherSettings settings;
	protected void setSettings(LauncherSettings settings) {
		this.settings = settings;
	}
	
	//@Override
	public LauncherConfig getConfig() {
		return config;
	}
	private LauncherConfig config;
	protected void setConfig(LauncherConfig config) {
		this.config = config;
	}
	
	//@Override
	public BundleContext getBundleContext() {
		return bundleContext;
	}
	private BundleContext bundleContext;
	protected void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}
	
	//@Override
	public String processProperty(String propertyValue) {
		if (!StringUtils.hasText(propertyValue)) {
			return null;
		}
		
		return StringUtils.replace(propertyValue, "${da-launcher:home}",
				getSettings().getDirectories().getHomeDir());
	}
	
	
	public DefaultLauncherContext(LauncherSettings settings,
			LauncherConfig config, BundleContext bundleContext) {
		setSettings(settings);
		setConfig(config);
		setBundleContext(bundleContext);
	}
	
}
