package org.dynamicjava.osgi.da_launcher.internal;

public interface InternalLauncherConstants {
	
	public static interface Directories {
		
		public static final String DA_LAUNCHER_HOME_DIR = "da-launcher";
		
		public static final String LOGS_DIR = "logs";
		
		public static final String CONFIGURATIONS_DIR = "config";
		
		public static final String FRAMEWORK_DIR = "osgi-framework";
		
		public static final String BUNDLES_DIR = "bundles";
		
		public static final String RUNTIME_DIR = "runtime";
		
		public static final String SYSTEM_BUNDLES_DIR = "system-bundles";
		
		public static final String APPLICATION_BUNDLES_DIR = "application-bundles";
		
		public static final String NON_BUNDLED_LIBRARIES_DIR = "non-bundled-libraries";
		
		public static final String LIB_DIR = "lib";
		
		public static final String GENERATED_BUNDLES_DIR = "generated-bundles";
		
		public static final String CACHE_DIR = "cache";
		
	}
	
	public static interface Files {
		
		public static final String FRAMEWORK_CONFIG = "osgi-framework.xml";
		
		public static final String GENERAL_SETTINGS = "general-settings.xml";

		public static final String LOGGING_SETTINGS_CONFIG = "logging-settings.xml";
		
		public static final String DA_LAUNCHER_EXT_CONFIG = "extensions.properties";
		
		public static final String GENERATED_BUNDLES_INFO = "generated-bundles-info.properties";
		
		public static final String BUNDLE_GROUPS_CONFIG = "bundle-groups.xml";
		
	}

	public static interface Frameworks {
		
		public static final String EQUINOX = "equinox";
		
		public static final String FELIX = "felix";
		
		public static final String KNOPFLERFISH = "knopflerfish";
		
	}
	
	public static interface Loggers {
		
		public static final String LOGGERS_PREFIX = "org.dynamicjava.osgi.da_launcher.";
		
		public static final String SYSTEM_EVENTS = LOGGERS_PREFIX + "system_events";
		
		public static final String BUNDLE_LIFECYCLE_EVENTS = LOGGERS_PREFIX + "bundle_lifecycle_events";
		
	}
	
	public static interface DaLauncherExt {
		
		public static final String LAUNCHER_FACTORY = "launcher-factory";
		
	}
	
}
