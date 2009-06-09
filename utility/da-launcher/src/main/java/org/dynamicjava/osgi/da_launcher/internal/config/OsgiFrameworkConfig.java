package org.dynamicjava.osgi.da_launcher.internal.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.dynamicjava.osgi.commons.utilities.IoUtils;
import org.dynamicjava.osgi.commons.utilities.StringUtils;
import org.dynamicjava.osgi.da_launcher.internal.InternalLauncherConstants;
import org.dynamicjava.osgi.da_launcher.internal.exceptions.ConfigurationException;
import org.dynamicjava.osgi.da_launcher.internal.utilities.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class OsgiFrameworkConfig {
	
	public void load(File frameworkConfigFile) {
		InputStream osgiFrameworkConfigIn = null;
		try {
			osgiFrameworkConfigIn = new FileInputStream(frameworkConfigFile);
			load(osgiFrameworkConfigIn);
		} catch (Exception ex) {
			throw new ConfigurationException(String.format(
					"Failed to load OSGi Framework configuration file '%s': %s",
					frameworkConfigFile, ex.getMessage()), ex);
		} finally {
			IoUtils.closeIfPossible(osgiFrameworkConfigIn);
		}
	}
	
	public void load(InputStream osgiFrameworkConfigIn) {
		try {
			Document document = XmlUtils.parseDocument(osgiFrameworkConfigIn);
			XPath xpath = XPathFactory.newInstance().newXPath();
			
			readFrameworkElement(document, xpath);
			readFrameworkProperties(document, xpath);
		} catch (ConfigurationException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new ConfigurationException(String.format(
					"Failed to process OSGi Framework configuration file: %s", ex.getMessage()), ex);
		}
	}

	
	protected void readFrameworkElement(Document document, XPath xpath)
			throws XPathExpressionException {
		Element frameworkElement = getAndValidateFrameworkElement(document, xpath);
		
		setFrameworkName(frameworkElement.getAttribute(FRAMEWORK_NAME_ATTRIBUTE));
		
		if (frameworkElement.hasAttribute(FRAMEWORK_VERSION_ATTRIBUTE)) {
			setVersion(frameworkElement.getAttribute(FRAMEWORK_VERSION_ATTRIBUTE));
		} else {
			setVersion(FRAMEWORK_VERSION_LATEST);
		}
	}
	
	protected Element getAndValidateFrameworkElement(Document document, XPath xpath)
			throws XPathExpressionException {
		Element frameworkElement = (Element)xpath.evaluate("/osgi-framework-settings/framework",
				document, XPathConstants.NODE);
		
		if (frameworkElement == null) {
			throw new ConfigurationException("Required element 'framework' the child of the root is not defined");
		}
		
		if (!frameworkElement.hasAttribute(FRAMEWORK_NAME_ATTRIBUTE)) {
			throw new ConfigurationException("Attribute 'framework/@name' is not defined");
		}
		String frameworkName = frameworkElement.getAttribute(FRAMEWORK_NAME_ATTRIBUTE).toLowerCase();
		if (!InternalLauncherConstants.Frameworks.FELIX.equalsIgnoreCase(frameworkName)
				&& !InternalLauncherConstants.Frameworks.EQUINOX.equalsIgnoreCase(frameworkName)
				&& !InternalLauncherConstants.Frameworks.KNOPFLERFISH.equalsIgnoreCase(frameworkName)) {
			throw new ConfigurationException(String.format("Value '%s' is invalid for attribute 'framework/@name'", frameworkName));
		}
		
		return frameworkElement;
	}
	
	protected void readFrameworkProperties(Document document, XPath xpath)
			throws XPathExpressionException {
		Properties properties = new Properties();
		
		Element frameworkPropertiesElement = (Element)xpath.evaluate(
				"/osgi-framework-settings/framework-properties", document, XPathConstants.NODE);
		if (frameworkPropertiesElement != null) {
			if (frameworkPropertiesElement.hasAttribute(PROFILE_DIR_ATTRIBUTE)) {
				setProfileDir(frameworkPropertiesElement.getAttribute(PROFILE_DIR_ATTRIBUTE));
			}
		}
		
		NodeList frameworkPropertiesNodeList = (NodeList)xpath.evaluate(
				"/osgi-framework-settings/framework-properties/property", document, XPathConstants.NODESET);
		for (int propertyIndex = 0; propertyIndex < frameworkPropertiesNodeList.getLength(); propertyIndex++) {
			Element propertyElement = (Element)frameworkPropertiesNodeList.item(propertyIndex);
			readFrameworkProperty(propertyElement, properties);
		}
		
		setFrameworkProperties(properties);
	}
	
	protected void readFrameworkProperty(Element propertyElement, Properties properties) {
		if (!propertyElement.hasAttribute(PROPERTY_NAME_ATTRIBUTE)) {
			throw new ConfigurationException("Attribute 'framework-properties/property/@name' is not defined");
		}
		String propertyName = propertyElement.getAttribute(PROPERTY_NAME_ATTRIBUTE);
		
		String propertyValue = null;
		if (propertyElement.hasAttribute(PROPERTY_VALUE_ATTRIBUTE)) {
			propertyValue = propertyElement.getAttribute(PROPERTY_VALUE_ATTRIBUTE);
		} else {
			if (StringUtils.hasText(propertyElement.getTextContent())) {
				propertyValue = propertyElement.getTextContent();
			} else {
				throw new ConfigurationException(String.format("Framework property '%s' has no value specified", propertyName));
			}
		}
		
		properties.put(propertyName, propertyValue);
	}
	
	
	private String frameworkName;
	public String getFrameworkName() {
		return frameworkName;
	}
	public void setFrameworkName(String osgiFrameworkName) {
		this.frameworkName = osgiFrameworkName;
	}
	
	private String version;
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
	private String profileDir;
	public String getProfileDir() {
		return profileDir;
	}
	public void setProfileDir(String profileDir) {
		this.profileDir = profileDir;
	}
	
	private Properties frameworkProperties = new Properties();
	public Properties getFrameworkProperties() {
		return frameworkProperties;
	}
	public void setFrameworkProperties(Properties frameworkProperties) {
		this.frameworkProperties = frameworkProperties;
	}
	
	private ClassLoader frameworkClassLoader;
	public ClassLoader getFrameworkClassLoader() {
		return frameworkClassLoader;
	}
	public void setFrameworkClassLoader(ClassLoader frameworkClassLoader) {
		this.frameworkClassLoader = frameworkClassLoader;
	}
	
	
	protected static final String FRAMEWORK_ELEMENT = "framework";
	protected static final String FRAMEWORK_NAME_ATTRIBUTE = "name";
	protected static final String FRAMEWORK_VERSION_ATTRIBUTE = "version";
	protected static final String PROPERTY_NAME_ATTRIBUTE = "name";
	protected static final String PROPERTY_VALUE_ATTRIBUTE = "value";
	protected static final String FRAMEWORK_VERSION_LATEST = "latest";
	protected static final String PROFILE_DIR_ATTRIBUTE = "profile-dir";
	
}
