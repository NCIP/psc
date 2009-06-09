package org.dynamicjava.osgi.da_launcher.internal.framework;

import org.osgi.framework.BundleContext;

public interface OsgiFramework {
	
	void start();
	
	void stop();
	
	BundleContext getBundleContext();
	
}
