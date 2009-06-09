package org.dynamicjava.osgi.da_launcher;

import org.dynamicjava.osgi.da_launcher.internal.LauncherObjectsFactory;
import org.dynamicjava.osgi.da_launcher.internal.support.DefaultLauncher;

public class LauncherFactory {
	
	public Launcher createLauncher() {
		return new DefaultLauncher(getSettings(), new LauncherObjectsFactory(getSettings()));
	}
	
	
	public LauncherFactory(LauncherSettings settings) {
		this.settings = settings;
	}
	
	private final LauncherSettings settings;
	protected LauncherSettings getSettings() {
		return settings;
	}
	
}
