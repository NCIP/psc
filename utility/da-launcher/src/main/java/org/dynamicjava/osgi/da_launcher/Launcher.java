package org.dynamicjava.osgi.da_launcher;

import org.dynamicjava.osgi.da_launcher.internal.config.LauncherConfig;
import org.dynamicjava.osgi.da_launcher.internal.framework.OsgiFramework;

public interface Launcher {
	
	void launch();
	
	void launch(LauncherConfig launcherConfig);
	
	void shutdown();
	
	OsgiFramework getOsgiFramework();
	
}
