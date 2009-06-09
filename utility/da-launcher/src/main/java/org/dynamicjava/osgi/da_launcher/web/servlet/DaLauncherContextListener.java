package org.dynamicjava.osgi.da_launcher.web.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.dynamicjava.osgi.da_launcher.Launcher;
import org.dynamicjava.osgi.da_launcher.LauncherFactory;
import org.dynamicjava.osgi.da_launcher.LauncherSettings;
import org.dynamicjava.osgi.da_launcher.internal.InternalLauncherConstants;
import org.dynamicjava.osgi.da_launcher.internal.exceptions.LauncherException;
import org.dynamicjava.osgi.da_launcher.internal.exceptions.LaunchingException;
import org.dynamicjava.osgi.da_launcher.web.DaLauncherWebConstants;

public class DaLauncherContextListener implements ServletContextListener {
	
	//@Override
	public void contextInitialized(ServletContextEvent contextEvent) {
		try {
			ServletContext context = contextEvent.getServletContext();
			
			Launcher launcher = newDaLauncherFactory(context).createLauncher();
			launcher.launch();
			
			setLauncher(launcher);
			putServletContextAttributes(context);
		} catch (LaunchingException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new LaunchingException(
					String.format("Failed to launch DA-Launcher: %s", ex.getMessage()), ex);
		}
	}
	
	//@Override
	public void contextDestroyed(ServletContextEvent contextEvent) {
		if (getLauncher() != null) {
			getLauncher().shutdown();
		}
	}
	
	
	protected void putServletContextAttributes(ServletContext context) {
		String putContextValue = context.getInitParameter(DaLauncherWebConstants.ContextListenerInitParamNames
				.PUT_BUNDLE_CONTEXT_IN_SERVLET_CONTEXT);
		if (putContextValue == null || Boolean.parseBoolean(putContextValue)) {
			String bundleContextAttributeName = context.getInitParameter(
					DaLauncherWebConstants.ContextListenerInitParamNames.BUNDLE_CONTEXT_ATTRIBUTE_NAME);
			if (bundleContextAttributeName == null) {
				bundleContextAttributeName = DaLauncherWebConstants.ServletContextAttributes.BUNDLE_CONTEXT_KEY;
			}
			context.setAttribute(bundleContextAttributeName, getLauncher().getOsgiFramework().getBundleContext());
		}
	}
	
	
	protected LauncherFactory newDaLauncherFactory(ServletContext context) {
		String daLauncherHomeDir = getDaLauncherHomeDir(context);
		try {
			String launcherFactoryClassName = retrieveLauncherFactoryClassName(daLauncherHomeDir);
			
			if (LauncherFactory.class.equals(launcherFactoryClassName)) {
				return new LauncherFactory(getLauncherSettings(context));
			} else {
				Class<?> launcherFactoryClass = Thread.currentThread().getContextClassLoader()
						.loadClass(launcherFactoryClassName);
				
				try {
					Constructor<?> constructor = launcherFactoryClass.getConstructor(
							LauncherSettings.class);
					return (LauncherFactory)constructor.newInstance(getLauncherSettings(context));
				} catch (Throwable ex) {
					return (LauncherFactory)launcherFactoryClass.newInstance();
				}
			}
		} catch (Throwable ex) {
			throw new LauncherException(String.format("Failed to load LauncherFactory: %s",
					ex.getMessage()), ex);
		}
	}
	
	protected String retrieveLauncherFactoryClassName(String daLauncerHomeDir) throws Exception {
		File daLauncherExtConfigFile = new File(new File(daLauncerHomeDir,
				InternalLauncherConstants.Directories.CONFIGURATIONS_DIR).getAbsolutePath(),
				InternalLauncherConstants.Files.DA_LAUNCHER_EXT_CONFIG);
		
		if (daLauncherExtConfigFile.exists()) {
			Properties daLauncherExtProeprties = new Properties();
			daLauncherExtProeprties.load(new FileInputStream(daLauncherExtConfigFile));
			
			String launcherFactoryClassName = daLauncherExtProeprties.getProperty(
					InternalLauncherConstants.DaLauncherExt.LAUNCHER_FACTORY);
			
			if (launcherFactoryClassName != null) {
				return launcherFactoryClassName;
			}
		}
		
		return LauncherFactory.class.getName();
	}
	
	protected LauncherSettings getLauncherSettings(ServletContext context) {
		return new LauncherSettings(getDaLauncherHomeDir(context));
	}
	
	protected String getDaLauncherHomeDir(ServletContext context) {
		return context.getRealPath(DA_LAUNCHER_APP_RELATIVE_HOME_DIR);
	}
	
	
	private Launcher launcher;
	protected Launcher getLauncher() {
		return launcher;
	}
	protected void setLauncher(Launcher launcher) {
		this.launcher = launcher;
	}
	
	
	protected static final String DA_LAUNCHER_APP_RELATIVE_HOME_DIR =
			"/WEB-INF/" + InternalLauncherConstants.Directories.DA_LAUNCHER_HOME_DIR;
	
}
