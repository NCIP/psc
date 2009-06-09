package org.dynamicjava.osgi.da_launcher.internal.config;

public class LauncherConfig {
	
	private OsgiFrameworkConfig osgiFrameworkConfig;
	public OsgiFrameworkConfig getOsgiFrameworkConfig() {
		return osgiFrameworkConfig;
	}
	public void setOsgiFrameworkConfig(OsgiFrameworkConfig osgiFrameworkConfig) {
		this.osgiFrameworkConfig = osgiFrameworkConfig;
	}
	
	private GeneralSettings generalSettings = new GeneralSettings();
	public GeneralSettings getGeneralSettings() {
		return generalSettings;
	}
	public void setGeneralSettings(GeneralSettings generalSettings) {
		this.generalSettings = generalSettings;
	}
	
	private LoggingConfig loggingConfig;
	public LoggingConfig getLoggingConfig() {
		return loggingConfig;
	}
	public void setLoggingConfig(LoggingConfig loggingConfig) {
		this.loggingConfig = loggingConfig;
	}
	
	private BundleGroupsConfig bundleGroupsConfig;
	public BundleGroupsConfig getBundleGroupsConfig() {
		return bundleGroupsConfig;
	}
	public void setBundleGroupsConfig(BundleGroupsConfig bundleGroupsConfig) {
		this.bundleGroupsConfig = bundleGroupsConfig;
	}
	
}
