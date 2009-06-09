package org.dynamicjava.osgi.da_launcher.internal.startup;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.dynamicjava.osgi.commons.utilities.FileUtils;
import org.dynamicjava.osgi.commons.utilities.StringUtils;
import org.dynamicjava.osgi.da_launcher.LauncherConstants;
import org.dynamicjava.osgi.da_launcher.LauncherSettings;
import org.dynamicjava.osgi.da_launcher.internal.InternalLauncherConstants;
import org.dynamicjava.osgi.da_launcher.internal.config.LauncherConfig;
import org.dynamicjava.osgi.da_launcher.internal.config.OsgiFrameworkConfig;
import org.dynamicjava.osgi.da_launcher.internal.exceptions.ConfigurationException;
import org.dynamicjava.osgi.da_launcher.internal.exceptions.UnexpectedLauncherException;
import org.dynamicjava.osgi.da_launcher.internal.framework.OsgiFramework;
import org.dynamicjava.osgi.da_launcher.internal.framework.OsgiFrameworkSettings;
import org.dynamicjava.osgi.da_launcher.internal.framework.equinox.v3.EquinoxFramework;
import org.dynamicjava.osgi.da_launcher.internal.framework.felix.v1.FelixFramework;
import org.dynamicjava.osgi.da_launcher.internal.framework.knopflerfish.v2.KnopflerfishFramework;

public class OsgiFrameworkLoader {
	
	public OsgiFramework loadOsgiFramework() {
		OsgiFrameworkConfig frameworkConfig = getOsgiFrameworkConfig();
		
		frameworkConfig.getFrameworkProperties().put(LauncherConstants.BundleContextProperties.LOGS_DIR,
				getSettings().getDirectories().getLogsDir());
		
		ClassLoader frameworkClassLoader = getFrameworkClassLoader(frameworkConfig);
		
		return createOsgiFramework(frameworkConfig.getFrameworkName(),
				frameworkConfig.getVersion(), frameworkClassLoader, frameworkConfig);
	}
	
	
	protected ClassLoader getFrameworkClassLoader(OsgiFrameworkConfig frameworkConfig) {
		if (frameworkConfig.getFrameworkClassLoader() != null) {
			return frameworkConfig.getFrameworkClassLoader();
		} else {
			return createFrameworkClassLoader(frameworkConfig.getFrameworkName(), frameworkConfig.getVersion());
		}
	}
	
	protected ClassLoader createFrameworkClassLoader(String frameworkName, String version) {
		File frameworkDir = new File(String.format("%s/%s/%s",
				getSettings().getDirectories().getFrameworkDir(), frameworkName, version));
		if (!frameworkDir.exists()) {
			throw new ConfigurationException(String.format(
					"In the OSGi Framework Configuration file the OSGi framework with "
					+ "name = '%s' and version = '%s' is specified. But the directory '%s' does not exits. "
					+ "Please, make sure that this directory exists and contains JAR file(s) that contain "
					+ "the classes of the specified OSGi framework",
					frameworkName, version, frameworkDir.getAbsoluteFile()));
		}
		
		File[] files = frameworkDir.listFiles();
		List<URL> jarUrls = new ArrayList<URL>();
		for (File file : files) {
			if (file.isFile() && file.getName().endsWith(".jar")) {
				try {
					jarUrls.add(file.toURI().toURL());
				} catch (MalformedURLException ex) {
					throw new UnexpectedLauncherException(ex);
				}
			}
		}
		if (jarUrls.size() == 0) {
			throw new ConfigurationException(String.format(
					"The OSGi Framework directory '%s' does not contain any JAR files", frameworkDir));
		}
		
		return new URLClassLoader(jarUrls.toArray(new URL[0]),
				Thread.currentThread().getContextClassLoader());
	}
	
	protected OsgiFramework createOsgiFramework(String frameworkName, String version,
			ClassLoader frameworkClassLoader, OsgiFrameworkConfig frameworkConfig) {
		OsgiFrameworkSettings frameworkSettings = createFrameworkSettings(frameworkConfig);
		if (InternalLauncherConstants.Frameworks.FELIX.equalsIgnoreCase(frameworkName)) {
			return new FelixFramework(frameworkSettings, frameworkClassLoader);
		} else if (InternalLauncherConstants.Frameworks.EQUINOX.equalsIgnoreCase(frameworkName)) {
			return new EquinoxFramework(frameworkSettings, frameworkClassLoader);
		} else if (InternalLauncherConstants.Frameworks.KNOPFLERFISH.equalsIgnoreCase(frameworkName)) {
			return new KnopflerfishFramework(frameworkSettings, frameworkClassLoader);
		} else {
			/// This exception shouldn't occur, but just in case.
			throw new ConfigurationException(String.format(
					"OSGi Framework '%s' is not supported.", frameworkName));
		}
		// TODO Putting Framework Version into account when selecting an OsgiFramework class.
	}
	
	protected OsgiFrameworkSettings createFrameworkSettings(OsgiFrameworkConfig frameworkConfig) {
		OsgiFrameworkSettings settings = new OsgiFrameworkSettings();
		
		if (StringUtils.hasText(frameworkConfig.getProfileDir())) {
			settings.setProfileDir(frameworkConfig.getProfileDir());
			
			File profileDir = new File(frameworkConfig.getProfileDir());
			FileUtils.delete(new File(frameworkConfig.getProfileDir()));
			profileDir.mkdirs();
		} else {
			File profileDir = new File(getSettings().getDirectories().getRuntimeDir(), "profile");
			// TODO Handling non-clean launch
			if (profileDir.exists()) {
				FileUtils.delete(profileDir);
			}
			profileDir.mkdirs();
			settings.setProfileDir(profileDir.getAbsolutePath());
		}
		settings.getFrameworkSpecificProperties().putAll(frameworkConfig.getFrameworkProperties());
		
		return settings;
	}
	
	protected OsgiFrameworkConfig getOsgiFrameworkConfig() {
		return getLauncherConfig().getOsgiFrameworkConfig();
	}
	
	
	public OsgiFrameworkLoader(LauncherSettings settings, LauncherConfig launcherConfig) {
		this.settings = settings;
		this.launcherConfig = launcherConfig;
	}
	
	private final LauncherSettings settings;
	protected LauncherSettings getSettings() {
		return settings;
	}
	
	private final LauncherConfig launcherConfig;
	protected LauncherConfig getLauncherConfig() {
		return launcherConfig;
	}
	
}
