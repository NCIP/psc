package org.dynamicjava.osgi.da_launcher.internal.support;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.dynamicjava.osgi.da_launcher.Launcher;
import org.dynamicjava.osgi.da_launcher.LauncherSettings;
import org.dynamicjava.osgi.da_launcher.internal.InternalLauncherConstants;
import org.dynamicjava.osgi.da_launcher.internal.LauncherContext;
import org.dynamicjava.osgi.da_launcher.internal.LauncherObjectsFactory;
import org.dynamicjava.osgi.da_launcher.internal.bundle.BundleManager;
import org.dynamicjava.osgi.da_launcher.internal.config.LauncherConfig;
import org.dynamicjava.osgi.da_launcher.internal.exceptions.LaunchingException;
import org.dynamicjava.osgi.da_launcher.internal.framework.OsgiFramework;
import org.dynamicjava.osgi.da_launcher.internal.startup.LoggersInitializer;
import org.dynamicjava.osgi.da_launcher.internal.support.class_loading.OsgiAwareClassLoader;

public class DefaultLauncher implements Launcher {
	
	//@Override
	public synchronized void launch(LauncherConfig launcherConfig) {
		try {
			setLauncherConfig(launcherConfig);
			
			LauncherObjectsFactory factory = getFactory();
			
			LoggersInitializer loggersInitializer = factory.createLoggersInitializer(
					launcherConfig.getLoggingConfig());
			loggersInitializer.removeConsoleHandlerFromRootLogger();
			loggersInitializer.initSystemEventsLogger();
			
			logger.info("Launching Dynamic Application.");
			
			OsgiAwareClassLoader osgiAwareClassLoader = null;
			ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
			if (launcherConfig.getGeneralSettings().getThreadClassLoaderSettings().isOsgiAware()) {
				osgiAwareClassLoader = newOsgiAwareClassLoader();
				Thread.currentThread().setContextClassLoader(osgiAwareClassLoader);
			}
			
			OsgiFramework osgiFrameowrk = factory.createOsgiFrameworkLoader(launcherConfig).loadOsgiFramework();
			setOsgiFramework(osgiFrameowrk);
			
			getOsgiFramework().start();
			
			if (osgiAwareClassLoader != null) {
				osgiAwareClassLoader.useBundleContext(osgiFrameowrk.getBundleContext());
			}
			
			Thread.currentThread().setContextClassLoader(oldClassLoader);
			
			/// This is called twice because for some reason after staring Equinox in Tomcat
			/// the root logger uses the ConsoleHandler. Such problem doesn't occur with Felix
			/// or when using Equinox in a standalone application.
			loggersInitializer.removeConsoleHandlerFromRootLogger();
			loggersInitializer.initOtherLoggers();
			
			LauncherContext context = new DefaultLauncherContext(getSettings(),
					launcherConfig, osgiFrameowrk.getBundleContext());
			
			BundleManager bundleManager = factory.createBundleManager(context);
			setBundleManager(bundleManager);
			
			if (osgiAwareClassLoader != null) {
				Thread.currentThread().setContextClassLoader(osgiAwareClassLoader);
			}
			
			bundleManager.run();
			
			logger.info("Dynamic Application is being launched successfully.");
		} catch (Throwable ex) {
			LaunchingException launchingException = new LaunchingException(
					String.format("Failed to launch Dynamic Application Launcher: %s", ex.getMessage()), ex);
			logger.log(Level.SEVERE, launchingException.getMessage(), ex);
			throw launchingException;
		}
	}
	
	//@Override
	public synchronized void launch() {
		try {
			LauncherObjectsFactory factory = getFactory();
			
			factory.createFilesIntegrityValidator().validate();
			
			launch(factory.createLauncherConfigLoader().loadConfig());
		} catch (LaunchingException ex) {
			throw ex;
		} catch (Throwable ex) {
			LaunchingException launchingException = new LaunchingException(
					String.format("Failed to launch Dynamic Application Launcher: %s", ex.getMessage()), ex);
			logger.log(Level.SEVERE, launchingException.getMessage(), ex);
			throw launchingException;
		}
	}
	
	//@Override
	public synchronized void shutdown() {
		try {
			logger.info("Shutting down Dynamic Application.");
			
			getOsgiFramework().stop();
			
			logger.info("Dynamic Application Launcher was shutdown sucessfully.");
		} catch (Throwable ex) {
			logger.log(Level.SEVERE, String.format("An error occured while stopping Dynamic Application Launcher: %s",
					ex.getMessage()), ex);
		}
		
		getBundleManager().shutdown();
		
		setOsgiFramework(null);
		setBundleManager(null);
	}
	
	//@Override
	public OsgiFramework getOsgiFramework() {
		return osgiFramework;
	}
	
	
	protected OsgiAwareClassLoader newOsgiAwareClassLoader() {
		return new OsgiAwareClassLoader();
	}
	
	
	public DefaultLauncher(LauncherSettings settings, LauncherObjectsFactory daLauncherFactory) {
		this.settings = settings;
		this.factory = daLauncherFactory;
	}
	
	
	private final LauncherSettings settings;
	protected LauncherSettings getSettings() {
		return settings;
	}

	private final LauncherObjectsFactory factory;
	protected LauncherObjectsFactory getFactory() {
		return factory;
	}
	
	private OsgiFramework osgiFramework;
	protected void setOsgiFramework(OsgiFramework osgiFramework) {
		this.osgiFramework = osgiFramework;
	}
	
	private BundleManager bundleManager;
	protected BundleManager getBundleManager() {
		return bundleManager;
	}
	protected void setBundleManager(BundleManager bundleManager) {
		this.bundleManager = bundleManager;
	}
	
	private LauncherConfig launcherConfig;
	protected LauncherConfig getLauncherConfig() {
		return launcherConfig;
	}
	protected void setLauncherConfig(LauncherConfig launcherConfig) {
		this.launcherConfig = launcherConfig;
	}
	
	
	protected static final Logger logger = Logger.getLogger(InternalLauncherConstants.Loggers.SYSTEM_EVENTS);
	
}
