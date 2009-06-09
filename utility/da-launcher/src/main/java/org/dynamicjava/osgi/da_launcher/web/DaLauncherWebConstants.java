package org.dynamicjava.osgi.da_launcher.web;

public interface DaLauncherWebConstants {
	
	public static interface ServletContextAttributes {
		
		public static final String BUNDLE_CONTEXT_KEY = "osgi.bundle-context";
		
	}
	
	public static interface ContextListenerInitParamNames {
		
		public static final String PUT_BUNDLE_CONTEXT_IN_SERVLET_CONTEXT =
			"put-bundle-context-in-servlet-context";
		
		public static final String BUNDLE_CONTEXT_ATTRIBUTE_NAME = "bundle-context-attribute-name";
		
	}
	
}
