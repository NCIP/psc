package org.dynamicjava.osgi.da_launcher.internal.framework;

import java.util.Properties;

public class OsgiFrameworkSettings {
	
	private String profileDir;
	public String getProfileDir() {
		return profileDir;
	}
	public void setProfileDir(String profileDir) {
		this.profileDir = profileDir;
	}
	
	private Properties frameworkSpecificProperties = new Properties();
	public Properties getFrameworkSpecificProperties() {
		return frameworkSpecificProperties;
	}
	
}
