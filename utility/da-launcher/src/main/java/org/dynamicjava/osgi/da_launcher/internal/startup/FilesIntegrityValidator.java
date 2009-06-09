package org.dynamicjava.osgi.da_launcher.internal.startup;

import java.io.File;

import org.dynamicjava.osgi.da_launcher.LauncherSettings;
import org.dynamicjava.osgi.da_launcher.internal.exceptions.FilesIntegrityException;

public class FilesIntegrityValidator {
	
	public void validate() {
		validateDaLauncherHomeDir();
		validateLogsDir();
	}
	
	
	protected void validateDaLauncherHomeDir() {
		String daLauncherHomeDir = getSettings().getDirectories().getHomeDir();
		if (!new File(daLauncherHomeDir).exists()) {
			throw new FilesIntegrityException(String.format(
					"DA-Launcher directory '%s' does not exist", daLauncherHomeDir));
		}
	}
	
	protected void validateLogsDir() {
		String logsDir = getSettings().getDirectories().getLogsDir();
		if (!new File(logsDir).exists()) {
			new File(logsDir).mkdir();
		}
	}
	
	
	public FilesIntegrityValidator(LauncherSettings settings) {
		this.settings = settings;
	}
	
	private final LauncherSettings settings;
	protected LauncherSettings getSettings() {
		return settings;
	}
	
}
