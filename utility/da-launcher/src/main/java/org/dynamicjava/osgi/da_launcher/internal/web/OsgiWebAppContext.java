package org.dynamicjava.osgi.da_launcher.internal.web;

import javax.servlet.ServletContext;

import org.dynamicjava.osgi.da_launcher.web.DaLauncherWebConstants;
import org.osgi.framework.BundleContext;

public class OsgiWebAppContext {
	
	public static BundleContext getBundleContext(ServletContext context) {
		return (BundleContext)context.getAttribute(
				DaLauncherWebConstants.ServletContextAttributes.BUNDLE_CONTEXT_KEY);
	}
	
}
