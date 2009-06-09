package org.dynamicjava.osgi.da_launcher.internal.framework.equinox.v3;

public interface ApiMembersNames {
	
	public static interface Classes {
		
		public final static String BASE_ADAPTOR = "org.eclipse.osgi.baseadaptor.BaseAdaptor";
		
		public final static String FRAMEWORK_ADAPTOR = "org.eclipse.osgi.framework.adaptor.FrameworkAdaptor";
		
		public final static String OSGI = "org.eclipse.osgi.framework.internal.core.OSGi";
		
		public final static String FRAMEWORK_PROPERTIES = "org.eclipse.osgi.framework.internal.core.FrameworkProperties";
		
		public final static String FILTER_IMPL = "org.eclipse.osgi.framework.internal.core.FilterImpl";
	}
	
	public static interface Methods {
		
		public final static String LAUNCH = "launch";
		
		public final static String SHUTDOWN = "shutdown";
		
		public final static String GET_BUNDLE_CONTEXT = "getBundleContext";
		
		public final static String SET_PROPERTY = "setProperty";
		
	}
	
}
