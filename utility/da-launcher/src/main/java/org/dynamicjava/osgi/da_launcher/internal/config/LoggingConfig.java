package org.dynamicjava.osgi.da_launcher.internal.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.dynamicjava.osgi.commons.utilities.IoUtils;
import org.dynamicjava.osgi.da_launcher.internal.config.logging_settings.LoggerSettings;
import org.dynamicjava.osgi.da_launcher.internal.exceptions.ConfigurationException;
import org.dynamicjava.osgi.da_launcher.internal.utilities.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class LoggingConfig {
	
	public void load(File loggingConfigFile) {
		InputStream loggingConfigIn = null;
		try {
			loggingConfigIn = new FileInputStream(loggingConfigFile);
			load(loggingConfigIn);
		} catch (Exception ex) {
			throw new ConfigurationException(String.format(
					"Failed to load Logging Settings configuration file '%s': %s",
					loggingConfigFile, ex.getMessage()), ex);
		} finally {
			IoUtils.closeIfPossible(loggingConfigIn);
		}
	}
	
	public void load(InputStream osgiFrameworkConfigIn) {
		try {
			Document document = XmlUtils.parseDocument(osgiFrameworkConfigIn);
			XPath xpath = XPathFactory.newInstance().newXPath();
			
			readBundleLifecycleEventsLoggerElement(document, xpath);
			readSystemEventsLoggerElement(document, xpath);
		} catch (ConfigurationException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new ConfigurationException(String.format(
					"Failed to process Bundle Settings configuration file: %s", ex.getMessage()), ex);
		}
	}
	
	
	protected void readBundleLifecycleEventsLoggerElement(Document document, XPath xpath)
			throws XPathExpressionException {
		readLoggerElement(document, xpath, BUNDLE_LIFECYCLE_EVENTS_LOGGER_ELEMENT,
				getBundleLifecycleEventsLoggerSettings());
	}
	
	protected void readSystemEventsLoggerElement(Document document, XPath xpath)
			throws XPathExpressionException {
		readLoggerElement(document, xpath, SYSTEM_EVENTS_LOGGER_ELEMENT, getSystemEventsLoggerSettings());
	}
	
	protected void readLoggerElement(Document document, XPath xpath, String loggerElementName,
			LoggerSettings loggerSettings) throws XPathExpressionException {
		Element loggerElement = (Element)xpath.evaluate(
				String.format("/%s/%s", LOGGER_SETTINGS_ELEMENT, loggerElementName),
				document, XPathConstants.NODE);
		if (loggerElement != null) {
			readCommonLoggerSettings(loggerSettings, loggerElement);
		}
	}
	
	protected void readCommonLoggerSettings(LoggerSettings loggerSettings, Element loggerSettingsElement) {
		String enabledAttribute = loggerSettingsElement.getAttribute(ENABLED_ATTRIBUTE);
		if (enabledAttribute != null) {
			loggerSettings.setEnabled(Boolean.parseBoolean(enabledAttribute));
		}
	}
	
	
	private LoggerSettings bundleLifecycleEventsLoggerSettings = new LoggerSettings();
	public LoggerSettings getBundleLifecycleEventsLoggerSettings() {
		return bundleLifecycleEventsLoggerSettings;
	}
	public void setBundleLifecycleEventsLoggerSettings(LoggerSettings bundleLifecycleEventsLoggerSettings) {
		this.bundleLifecycleEventsLoggerSettings = bundleLifecycleEventsLoggerSettings;
	}
	
	private LoggerSettings systemEventsLoggerSettings = new LoggerSettings();
	public LoggerSettings getSystemEventsLoggerSettings() {
		return systemEventsLoggerSettings;
	}
	public void setSystemEventsLoggerSettings(LoggerSettings systemEventsLoggerSettings) {
		this.systemEventsLoggerSettings = systemEventsLoggerSettings;
	}
	
	
	protected static final String LOGGER_SETTINGS_ELEMENT = "logger-settings";
	
	protected static final String BUNDLE_LIFECYCLE_EVENTS_LOGGER_ELEMENT = "bundle-lifecycle-events-logger";
	
	protected static final String SYSTEM_EVENTS_LOGGER_ELEMENT = "system-events-logger";
	
	protected static final String ENABLED_ATTRIBUTE = "enabled";
	
}
