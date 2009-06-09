package org.dynamicjava.osgi.da_launcher.internal.bundle.group;

import org.dynamicjava.osgi.da_launcher.internal.bundle.config.BundleGroupSettings;
import org.osgi.framework.Bundle;

public interface BundleGroup {
	
	void start(boolean installGroupBundles, boolean startGroupBundles);
	
	void stop();
	
	BundleGroupSettings getSettings();
	
	void startGroupBundles();
	
	Bundle[] getBundles();
	
	public String getName();
	
}
