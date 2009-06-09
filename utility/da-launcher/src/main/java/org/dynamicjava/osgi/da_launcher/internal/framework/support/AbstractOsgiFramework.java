package org.dynamicjava.osgi.da_launcher.internal.framework.support;

import org.dynamicjava.osgi.da_launcher.internal.exceptions.LauncherException;
import org.dynamicjava.osgi.da_launcher.internal.framework.OsgiFramework;
import org.dynamicjava.osgi.da_launcher.internal.framework.OsgiFrameworkSettings;
import org.dynamicjava.osgi.da_launcher.internal.framework.vendor.FrameworkUtil;
import org.osgi.framework.BundleContext;

public abstract class AbstractOsgiFramework implements OsgiFramework {
	
	protected abstract void startFramework() throws Exception;
	
	protected abstract void stopFramework() throws Exception;
	
	protected abstract BundleContext returnBundleContext();
	
	protected abstract String getFilterImplClassName();
	
	
	//@Override
	public synchronized void start() {
		if (isStarted()) {
			throw new IllegalArgumentException("OSGi Framework is alread running");
		}
		
		try {
			startFramework();
			updateOsgiFrameworkVendorPackage();
			setStarted(true);	
		} catch (Exception ex) {
			throw new LauncherException(String.format("Failed to start OSGi Framework: %s",
					ex.getMessage()), ex);
		}
	}
	
	//@Override
	public synchronized void stop() {
		if (!isStarted()) {
			throw new IllegalArgumentException("OSGi Framework is not running");
		}
		
		synchronized (this) {
			try {
				stopFramework();
				setStarted(false);
			} catch (Exception ex) {
				throw new LauncherException(String.format("Failed to stop OSGi Framework: %s",
						ex.getMessage()), ex);
			}
		}
	}
	
	//@Override
	public BundleContext getBundleContext() {
		if (!isStarted()) {
			throw new IllegalStateException("OSGi Framework is not running");
		}
		return returnBundleContext();
	}
	
	protected ClassLoader getFrameworkClassLoader() {
		return getClass().getClassLoader();
	}
	
	protected void updateOsgiFrameworkVendorPackage() {
		FrameworkUtil.setClassLoader(getFrameworkClassLoader(), getFilterImplClassName());
		System.setProperty("org.osgi.vendor.framework", getFrameworkVendorPackage());
	}
	
	protected String getFrameworkVendorPackage() {
		return FrameworkUtil.class.getPackage().getName();
	}
	
	protected boolean isStartOsgiConsole() {
		String consoleEnabled = getSettings().getFrameworkSpecificProperties().getProperty(
				OSGI_CONSOLE_ENABLED_PROPERTY);
		return consoleEnabled != null && Boolean.parseBoolean(consoleEnabled);
	}
	
	protected void startOsgiConsole() {
	}
	
	
	public AbstractOsgiFramework(OsgiFrameworkSettings settings) {
		setSettings(settings);
	}
	
	
	private boolean started = false;
	protected boolean isStarted() {
		return started;
	}
	protected void setStarted(boolean started) {
		this.started = started;
	}
	
	private OsgiFrameworkSettings settings;
	protected OsgiFrameworkSettings getSettings() {
		return settings;
	}
	protected void setSettings(OsgiFrameworkSettings settings) {
		this.settings = settings;
	}
	
	
	private static final String OSGI_CONSOLE_ENABLED_PROPERTY = "osgi-console.enabled";
	
}
