package org.dynamicjava.osgi.da_launcher.internal.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.dynamicjava.osgi.commons.utilities.IoUtils;
import org.dynamicjava.osgi.commons.utilities.StringUtils;
import org.dynamicjava.osgi.da_launcher.internal.exceptions.ConfigurationException;
import org.dynamicjava.osgi.da_launcher.internal.utilities.XPathUtils;
import org.dynamicjava.osgi.da_launcher.internal.utilities.XmlUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class GeneralSettings {
	
	private ThreadContextClassLoaderSettings threadClassLoaderSettings = new ThreadContextClassLoaderSettings();
	public ThreadContextClassLoaderSettings getThreadClassLoaderSettings() {
		return threadClassLoaderSettings;
	}
	public void setThreadClassLoaderSettings(ThreadContextClassLoaderSettings threadClassLoaderSettings) {
		this.threadClassLoaderSettings = threadClassLoaderSettings;
	}
	
	
	public class ThreadContextClassLoaderSettings {
		
		private boolean osgiAware = false;
		public boolean isOsgiAware() {
			return osgiAware;
		}
		public void setOsgiAware(boolean osgiAware) {
			this.osgiAware = osgiAware;
		}
		
	}
	
	
	public void load(File generalSettingsFile) {
		InputStream bundleSettingsConfigIn = null;
		try {
			bundleSettingsConfigIn = new FileInputStream(generalSettingsFile);
			load(bundleSettingsConfigIn);
		} catch (Exception ex) {
			throw new ConfigurationException(String.format(
					"Failed to load General Settings from '%s': %s",
					generalSettingsFile, ex.getMessage()), ex);
		} finally {
			IoUtils.closeIfPossible(bundleSettingsConfigIn);
		}
	}
	
	public void load(InputStream bundleGroupsConfigInput) {
		try {
			Document document = XmlUtils.parseDocument(bundleGroupsConfigInput);
			XPath xpath = XPathFactory.newInstance().newXPath();
			
			loadSettings(document, xpath);
		} catch (ConfigurationException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new ConfigurationException(String.format(
					"Failed to process Bundle Settings configuration file: %s", ex.getMessage()), ex);
		}
	}
	
	protected void loadSettings(Document document, XPath xpath) {
		Element threadClassLoaderElement = XPathUtils.getElement(xpath, document.getDocumentElement(),
				String.format("/%s/%s", GENERAL_SETTINGS_ELEMENT, THREAD_CLASS_LOADER_ELEMENT));
		if (threadClassLoaderElement != null) {
			Attr osgiAwareAttribute =  XmlUtils.findAttribute(threadClassLoaderElement, OSGI_AWARE_ATTRIBUTE);
			if (osgiAwareAttribute != null && StringUtils.hasText(osgiAwareAttribute.getValue())) {
				getThreadClassLoaderSettings().setOsgiAware(Boolean.parseBoolean(osgiAwareAttribute.getValue()));
			}
		}
	}
	
	
	public static final String GENERAL_SETTINGS_ELEMENT = "general-settings";
	public static final String THREAD_CLASS_LOADER_ELEMENT = "thread-context-class-loader";
	public static final String OSGI_AWARE_ATTRIBUTE = "osgi-aware";
	
}
