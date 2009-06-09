package org.dynamicjava.osgi.da_launcher;

import java.io.File;

import org.dynamicjava.osgi.da_launcher.internal.InternalLauncherConstants;

public class LauncherSettings {
	
	public LauncherSettings(String daLauncherHomeDir) {
		directories = newDirectories(daLauncherHomeDir);
		files = newFiles(directories);
	}
	
	
	private final Directories directories;
	public Directories getDirectories() {
		return directories;
	}
	
	private final Files files;
	public Files getFiles() {
		return files;
	}
	
	
	protected Directories newDirectories(String daLauncherHomeDir) {
		return new Directories(daLauncherHomeDir);
	}
	
	protected Files newFiles(Directories directories) {
		return new Files(directories);
	}
	
	
	public class Directories {
		
		private String homeDir;
		public String getHomeDir() {
			return homeDir;
		}
		
		private final String configurationsDir;
		public String getConfigurationsDir() {
			return configurationsDir;
		}
		
		private final String frameworkDir;
		public String getFrameworkDir() {
			return frameworkDir;
		}
		
		private final String logsDir;
		public String getLogsDir() {
			return logsDir;
		}
		
		private final String bundlesDir;
		public String getBundlesDir() {
			return bundlesDir;
		}
		
		private final String runtimeDir;
		public String getRuntimeDir() {
			return runtimeDir;
		}
		
		private final String cacheDir;
		public String getCacheDir() {
			return cacheDir;
		}
		
		private final String systemBundlesDir;
		public String getSystemBundlesDir() {
			return systemBundlesDir;
		}
		
		private final String applicationBundlesDir;
		public String getApplicationBundlesDir() {
			return applicationBundlesDir;
		}
		
		private final String nonBundledLibrariesDir;
		public String getNonBundledLibrariesDir() {
			return nonBundledLibrariesDir;
		}
		
		private final String generatedBundlesDir;
		public String getGeneratedBundlesDir() {
			return generatedBundlesDir;
		}
		
		public Directories(String daLauncherHomeDir) {
			homeDir = daLauncherHomeDir;
			
			configurationsDir = new File(daLauncherHomeDir,
					InternalLauncherConstants.Directories.CONFIGURATIONS_DIR).getAbsolutePath();

			frameworkDir = new File(daLauncherHomeDir,
					InternalLauncherConstants.Directories.FRAMEWORK_DIR).getAbsolutePath();

			logsDir = new File(daLauncherHomeDir,
					InternalLauncherConstants.Directories.LOGS_DIR).getAbsolutePath();

			bundlesDir = new File(daLauncherHomeDir,
					InternalLauncherConstants.Directories.BUNDLES_DIR).getAbsolutePath();
			
			runtimeDir = new File(daLauncherHomeDir,
					InternalLauncherConstants.Directories.RUNTIME_DIR).getAbsolutePath();
			
			cacheDir = new File(runtimeDir,
					InternalLauncherConstants.Directories.CACHE_DIR).getAbsolutePath();
			
			systemBundlesDir = new File(bundlesDir,
					InternalLauncherConstants.Directories.SYSTEM_BUNDLES_DIR).getAbsolutePath();
			
			applicationBundlesDir = new File(bundlesDir,
					InternalLauncherConstants.Directories.APPLICATION_BUNDLES_DIR).getAbsolutePath();
			
			nonBundledLibrariesDir = new File(bundlesDir,
					InternalLauncherConstants.Directories.NON_BUNDLED_LIBRARIES_DIR).getAbsolutePath();
			
			generatedBundlesDir = new File(cacheDir,
					InternalLauncherConstants.Directories.GENERATED_BUNDLES_DIR).getAbsolutePath();
		}
		
	}
	
	public class Files {
		
		private final String frameworkConfigFile;
		public String getFrameworkConfigFile() {
			return frameworkConfigFile;
		}
		
		private final String generalSettingsFile;
		public String getGeneralSettingsFile() {
			return generalSettingsFile;
		}
		
		private final String loggingConfigFile;
		public String getLoggingConfigFile() {
			return loggingConfigFile;
		}
		
		private final String bundleGroupsConfigFile;
		public String getBundleGroupsConfigFile() {
			return bundleGroupsConfigFile;
		}
		
		public Files(Directories directories) {
			frameworkConfigFile = new File(directories.getConfigurationsDir(),
					InternalLauncherConstants.Files.FRAMEWORK_CONFIG).getAbsolutePath();

			generalSettingsFile = new File(directories.getConfigurationsDir(),
					InternalLauncherConstants.Files.GENERAL_SETTINGS).getAbsolutePath();

			loggingConfigFile = new File(directories.getConfigurationsDir(),
					InternalLauncherConstants.Files.LOGGING_SETTINGS_CONFIG).getAbsolutePath();

			bundleGroupsConfigFile = new File(directories.getConfigurationsDir(),
					InternalLauncherConstants.Files.BUNDLE_GROUPS_CONFIG).getAbsolutePath();
		}
		
	}
	
}
