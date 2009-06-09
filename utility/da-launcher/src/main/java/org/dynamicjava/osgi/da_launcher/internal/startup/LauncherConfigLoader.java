package org.dynamicjava.osgi.da_launcher.internal.startup;

import java.io.File;

import org.dynamicjava.osgi.da_launcher.LauncherSettings;
import org.dynamicjava.osgi.da_launcher.internal.config.GeneralSettings;
import org.dynamicjava.osgi.da_launcher.internal.config.LauncherConfig;
import org.dynamicjava.osgi.da_launcher.internal.config.LoggingConfig;
import org.dynamicjava.osgi.da_launcher.internal.config.OsgiFrameworkConfig;
import org.dynamicjava.osgi.da_launcher.internal.exceptions.ConfigurationException;

public class LauncherConfigLoader {
	
	public LauncherConfig loadConfig() {
		try {
			LauncherConfig result = new LauncherConfig();
			
			retrieveOsgiFrameworkConfig(result);
			retrieveLoggingConfigIfPossible(result);
			retrieveGeneralSettingsIfPossible(result);
			
			return result;
		} catch (Throwable ex) {
			throw new ConfigurationException(String.format("Failed to load DA-Launcher Configurations: %s",
					ex.getMessage()), ex);
		}
	}
	
	
	protected void retrieveOsgiFrameworkConfig(LauncherConfig result) {
		OsgiFrameworkConfig osgiFrameworkConfig = newOsgiFrameworkConfig();
		osgiFrameworkConfig.load(new File(getLauncherSettings().getFiles().getFrameworkConfigFile()));
		result.setOsgiFrameworkConfig(osgiFrameworkConfig);
	}
	
	protected void retrieveGeneralSettingsIfPossible(LauncherConfig result) {
		GeneralSettings bundleSettings = newGeneralSettings();
		if (new File(getLauncherSettings().getFiles().getGeneralSettingsFile()).exists()) {
			bundleSettings.load(new File(getLauncherSettings().getFiles().getGeneralSettingsFile()));
		}
		result.setGeneralSettings(bundleSettings);
	}
	
	protected void retrieveLoggingConfigIfPossible(LauncherConfig result) {
		LoggingConfig loggingConfig = newLoggingConfig();
		if (new File(getLauncherSettings().getFiles().getLoggingConfigFile()).exists()) {
			loggingConfig.load(new File(getLauncherSettings().getFiles().getLoggingConfigFile()));
		}
		result.setLoggingConfig(loggingConfig);
	}
	
	
	protected OsgiFrameworkConfig newOsgiFrameworkConfig() {
		return new OsgiFrameworkConfig();
	}
	
	protected GeneralSettings newGeneralSettings() {
		return new GeneralSettings();
	}
	
	protected LoggingConfig newLoggingConfig() {
		return new LoggingConfig();
	}
	
	
	public LauncherConfigLoader(LauncherSettings launcherSettings) {
		setLauncherSettings(launcherSettings);
	}
	
	private LauncherSettings launcherSettings;
	protected LauncherSettings getLauncherSettings() {
		return launcherSettings;
	}
	protected void setLauncherSettings(LauncherSettings launcherSettings) {
		this.launcherSettings = launcherSettings;
	}
	
}
