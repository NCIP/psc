package org.dynamicjava.osgi.da_launcher.internal.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.dynamicjava.osgi.commons.utilities.IoUtils;
import org.dynamicjava.osgi.commons.utilities.StringUtils;
import org.dynamicjava.osgi.da_launcher.internal.LauncherContext;
import org.dynamicjava.osgi.da_launcher.internal.bundle.config.BundleGroupSettings;
import org.dynamicjava.osgi.da_launcher.internal.bundle.group.BundleGroup;
import org.dynamicjava.osgi.da_launcher.internal.bundle.group.DefaultBundleGroup;
import org.dynamicjava.osgi.da_launcher.internal.bundle.processors.BundleProcessor;
import org.dynamicjava.osgi.da_launcher.internal.bundle.processors.BundleProcessorChain;
import org.dynamicjava.osgi.da_launcher.internal.bundle.processors.GenerationBundleProcessor;
import org.dynamicjava.osgi.da_launcher.internal.bundle.processors.GenerationBundleProcessor.HandlingType;
import org.dynamicjava.osgi.da_launcher.internal.bundle.sources.BundlesDirectoryBundleSource;
import org.dynamicjava.osgi.da_launcher.internal.exceptions.ConfigurationException;
import org.dynamicjava.osgi.da_launcher.internal.utilities.XPathUtils;
import org.dynamicjava.osgi.da_launcher.internal.utilities.XmlUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class BundleGroupsConfig {
	
	public void load(File bundleGroupsConfigFile) {
		InputStream bundleSettingsConfigIn = null;
		try {
			bundleSettingsConfigIn = new FileInputStream(bundleGroupsConfigFile);
			load(bundleSettingsConfigIn);
		} catch (Exception ex) {
			throw new ConfigurationException(String.format(
					"Failed to load Bundle Settings configuration file '%s': %s",
					bundleGroupsConfigFile, ex.getMessage()), ex);
		} finally {
			IoUtils.closeIfPossible(bundleSettingsConfigIn);
		}
	}
	
	public void load(InputStream bundleGroupsConfigInput) {
		try {
			Document document = XmlUtils.parseDocument(bundleGroupsConfigInput);
			XPath xpath = XPathFactory.newInstance().newXPath();
			
			loadBundleGroupElements(document, xpath);
		} catch (ConfigurationException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new ConfigurationException(String.format(
					"Failed to process Bundle Settings configuration file: %s", ex.getMessage()), ex);
		}
	}
	
	
	protected void loadBundleGroupElements(Document document, XPath xpath) throws XPathExpressionException {
		loadBundlesDirectoryGroups(document, xpath);
	}
	
	protected void loadBundlesDirectoryGroups(Document document, XPath xpath) throws XPathExpressionException {
		NodeList bundlesDirectoryElements = (NodeList)xpath.evaluate(
				String.format("/%s/%s", BUNDLE_GROUPS_ELEMENT, BUNDLES_DIRECTORY_ELEMENT),
				document, XPathConstants.NODESET);
		if (bundlesDirectoryElements != null) {
			for (int i = 0; i < bundlesDirectoryElements.getLength(); i++) {
				loadBundlesDirectoryGroup((Element)bundlesDirectoryElements.item(i), xpath);
			}
		}
	}
	
	protected void loadBundlesDirectoryGroup(Element bundlesDirectoryElement, XPath xpath) {
		Element directorySettingsElement = getRequiredElement(
				bundlesDirectoryElement, xpath, DIRECTORY_SETTINGS_ELEMENT);
		
		String bundleGroupName = readRequiredAttribute(bundlesDirectoryElement, NAME_ATTRIBUTE);
		BundlesDirectoryBundleSource bundleSource = new BundlesDirectoryBundleSource(
				bundleGroupName,
				new File(getLauncherContext().processProperty(
						readRequiredAttribute(directorySettingsElement, DIRECTORY_SETTINGS_DIR_ATTRIBUTE))));
		
		DefaultBundleGroup bundleGroup = new DefaultBundleGroup(
				getLauncherContext().getBundleContext(), bundleSource);
		bundleGroup.setName(bundleGroupName);
		bundleGroup.setSettings(readBundleGroupSettings(bundlesDirectoryElement, xpath));
		bundleGroup.setProcessor(readBundleProcessors(bundleGroupName, bundlesDirectoryElement, xpath));
		
		getBundleGroups().add(bundleGroup);
	}
	
	protected BundleProcessor readBundleProcessors(String bundleGroupName, Element bundleGroupElement, XPath xpath) {
		BundleProcessorChain bundleProcessorChain = new BundleProcessorChain();
		
		Element bundleGenerationElement = XPathUtils.getElement(
				xpath, bundleGroupElement, BUNDLE_GENERATION_ELEMENT);
		if (bundleGenerationElement != null) {
			bundleProcessorChain.add(readGenerationBundleProcessor(bundleGroupName, bundleGenerationElement));
		}
		
		return bundleProcessorChain;
	}
	
	protected BundleProcessor readGenerationBundleProcessor(String bundleGroupName, Element bundleGenerationElement) {
		GenerationBundleProcessor processor = new GenerationBundleProcessor(bundleGroupName, getLauncherContext());
		
		Attr typeAttribute = XmlUtils.findAttribute(bundleGenerationElement, BUNDLE_GENERATION_TYPE_ATTRIBUTE);
		if (typeAttribute != null && StringUtils.hasText(typeAttribute.getValue())) {
			processor.setHandlingType(HandlingType.valueOf(typeAttribute.getValue()));
		}
		
		Attr cacheDirAttribute = XmlUtils.findAttribute(
				bundleGenerationElement, BUNDLE_GENERATION_CACHE_DIR_ATTRIBUTE);
		if (cacheDirAttribute != null && StringUtils.hasText(cacheDirAttribute.getValue())) {
			processor.setCacheDir(new File(getLauncherContext().processProperty(cacheDirAttribute.getValue())));
		}
		
		return processor;
	}
	
	protected BundleGroupSettings readBundleGroupSettings(Element bundleGroupElement, XPath xpath) {
		BundleGroupSettings settings = new BundleGroupSettings();
		
		Element settingsElement = XPathUtils.getElement(xpath, bundleGroupElement, BUNDLE_GROUP_SETTINGS_ELEMENT);
		if (settingsElement != null) {
			Attr installOrderAttribute = XmlUtils.findAttribute(settingsElement, INSTALL_ORDER_ATTRIBUTE);
			if (installOrderAttribute != null && StringUtils.hasText(installOrderAttribute.getValue())) {
				settings.setInstallOrder(Integer.parseInt(installOrderAttribute.getValue()));
			}
			
			Attr startOrderAttribute = XmlUtils.findAttribute(settingsElement, START_ORDER_ATTRIBUTE);
			if (startOrderAttribute != null && StringUtils.hasText(startOrderAttribute.getValue())) {
				settings.setStartOrder(Integer.parseInt(startOrderAttribute.getValue()));
			}
			
			Attr autoStartAttribute = XmlUtils.findAttribute(settingsElement, AUTO_START_BUNDLES_ATTRIBUTE);
			if (autoStartAttribute != null && StringUtils.hasText(autoStartAttribute.getValue())) {
				settings.setAutoStartBundles(Boolean.parseBoolean(autoStartAttribute.getValue()));
			}
			
			Attr ignoreInstallErrorsAttribute = XmlUtils.findAttribute(settingsElement, IGNORE_INSTALL_ERRORS_ATTRIBUTE);
			if (ignoreInstallErrorsAttribute != null && StringUtils.hasText(ignoreInstallErrorsAttribute.getValue())) {
				settings.setIgnoreInstallErrors(Boolean.parseBoolean(ignoreInstallErrorsAttribute.getValue()));
			}
			
			Attr ignoreStartErrorsAttribute = XmlUtils.findAttribute(settingsElement, IGNORE_START_ERRORS_ATTRIBUTE);
			if (ignoreStartErrorsAttribute != null && StringUtils.hasText(ignoreStartErrorsAttribute.getValue())) {
				settings.setIgnoreStartErrors(Boolean.parseBoolean(ignoreStartErrorsAttribute.getValue()));
			}
		}
		
		return settings;
	}
	
	protected Element getRequiredElement(Element element, XPath xpath, String xpathExpression) {
		Element requiredElement = XPathUtils.getElement(xpath, element, xpathExpression);
		if (requiredElement == null) {
			throw new ConfigurationException(String.format(
					"Element '%s' must have the child element '%s' defined",
					element.getLocalName(), xpathExpression));
		}
		return requiredElement;
	}
	
	protected String readRequiredAttribute(Element element, String attrbiuteName) {
		Attr attribute =  XmlUtils.findAttribute(element, attrbiuteName);
		if (attribute == null || !StringUtils.hasText(attribute.getValue())) {
			throw new ConfigurationException(String.format(
					"Element '%s' must have the attribute '%s' set",
					element.getTagName(), attrbiuteName));
		}
		
		return attribute.getValue();
	}
	
	
	public BundleGroupsConfig(LauncherContext launcherContext) {
		this.launcherContext = launcherContext;
	}
	
	private final LauncherContext launcherContext;
	protected LauncherContext getLauncherContext() {
		return launcherContext;
	}
	
	private final List<BundleGroup> bundleGroups = new ArrayList<BundleGroup>();
	public List<BundleGroup> getBundleGroups() {
		return bundleGroups;
	}
	
	
	public static final String BUNDLE_GROUPS_ELEMENT = "bundle-groups";
	public static final String BUNDLES_DIRECTORY_ELEMENT = "bundles-directory";
	public static final String DIRECTORY_SETTINGS_ELEMENT = "directory-settings";
	public static final String DIRECTORY_SETTINGS_DIR_ATTRIBUTE = "dir";
	public static final String NAME_ATTRIBUTE = "name";
	public static final String BUNDLE_GROUP_SETTINGS_ELEMENT = "common-settings";
	public static final String INSTALL_ORDER_ATTRIBUTE = "install-order";
	public static final String START_ORDER_ATTRIBUTE = "start-order";
	public static final String AUTO_START_BUNDLES_ATTRIBUTE = "auto-start-bundles";
	public static final String IGNORE_INSTALL_ERRORS_ATTRIBUTE = "ignore-install-errors";
	public static final String IGNORE_START_ERRORS_ATTRIBUTE = "ignore-start-errors";
	protected static final String BUNDLE_GENERATION_ELEMENT = "bundle-generation";
	protected static final String BUNDLE_GENERATION_TYPE_ATTRIBUTE = "bundle-generation";
	protected static final String BUNDLE_GENERATION_CACHE_DIR_ATTRIBUTE = "cache-dir";
	
}
