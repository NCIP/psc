package org.dynamicjava.osgi.da_launcher.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.dynamicjava.osgi.da_launcher.LauncherFactory;
import org.dynamicjava.osgi.da_launcher.LauncherSettings;
import org.dynamicjava.osgi.da_launcher.internal.InternalLauncherConstants;
import org.dynamicjava.osgi.da_launcher.internal.exceptions.LauncherException;

public class Main {

	public static void main(String[] args) {
		try {
			new Main().run(args);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}
	
	
	public void run(String[] args) {
		saveMainArgs(args);
		setDaLauncerHomeDir(locateDaLauncherHomeDir());
		retrieveDaLauncherFactory().createLauncher().launch();
	}
	
	
	protected LauncherFactory retrieveDaLauncherFactory() {
		try {
			String launcherFactoryClassName = retrieveLauncherFactoryClassName();
			
			if (LauncherFactory.class.equals(launcherFactoryClassName)) {
				return new LauncherFactory(getLauncherSettings());
			} else {
				Class<?> launcherFactoryClass = generateExtClassLoader().loadClass(launcherFactoryClassName);
				
				try {
					Constructor<?> constructor = launcherFactoryClass.getConstructor(
							LauncherSettings.class);
					return (LauncherFactory)constructor.newInstance(getLauncherSettings());
				} catch (Throwable ex) {
					return (LauncherFactory)launcherFactoryClass.newInstance();
				}
			}
		} catch (Throwable ex) {
			throw new LauncherException(String.format("Failed to load LauncherFactory: %s",
					ex.getMessage()), ex);
		}
	}
	
	protected String retrieveLauncherFactoryClassName() throws Exception {
		File daLauncherExtConfigFile = new File(new File(getDaLauncerHomeDir(),
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
	
	protected LauncherSettings getLauncherSettings() {
		return new LauncherSettings(getDaLauncerHomeDir());
	}
	
	protected ClassLoader generateExtClassLoader() {
		File[] files = new File(getDaLauncerHomeDir(), InternalLauncherConstants.Directories.LIB_DIR).listFiles();
		List<URL> jarUrls = new ArrayList<URL>();
		for (File file : files) {
			if (file.getName().endsWith(".jar")) {
				try {
					jarUrls.add(file.toURI().toURL());
				} catch (MalformedURLException ex) {
					// This exception shouldn't occur
					throw new RuntimeException(ex.getMessage(), ex);
				}
			}
		}
		return new URLClassLoader(jarUrls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());
	}
	
	
	protected String locateDaLauncherHomeDir() {
		String file = ".";
		String lastCheckedPath = "";
		List<String> files = null;
		while (true) {
			files = Arrays.asList(new File(file).list());
			if (files.contains("bundles") && files.contains("lib") && files.contains("config")) {
				break;
			}
			
			if (".".equals(file)) {
				file = "../";
			} else {
				file += "../";
			}
			
			String currentCheckedPath = getCanonicalPath(file);
			if (currentCheckedPath.equals(lastCheckedPath)) {
				throw new LauncherException("DA-Launcher home directory can not be located.");
			}
			lastCheckedPath = currentCheckedPath;
		}
		
		return getCanonicalPath(file);
	}
	
	protected String getCanonicalPath(String filePath) {
		try {
			return new File(filePath).getCanonicalPath();
		} catch (IOException ex) {
			// This exception shouldn't occur
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	protected void saveMainArgs(String[] args) {
		if (args == null || args.length == 0) {
			return;
		}
		
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < args.length; i++) {
			buffer.append(args[i]);
			if (i < args.length - 1) {
				buffer.append('\n');
			}
		}
		System.setProperty("main.args", buffer.toString());
	}
	
	
	private String daLauncerHomeDir;
	protected String getDaLauncerHomeDir() {
		return daLauncerHomeDir;
	}
	protected void setDaLauncerHomeDir(String daLauncerHomeDir) {
		this.daLauncerHomeDir = daLauncerHomeDir;
	}

}
