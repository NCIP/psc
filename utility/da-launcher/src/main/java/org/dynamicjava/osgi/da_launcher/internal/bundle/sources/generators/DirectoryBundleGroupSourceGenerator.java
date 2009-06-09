package org.dynamicjava.osgi.da_launcher.internal.bundle.sources.generators;

import java.io.File;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.dynamicjava.osgi.commons.utilities.StringUtils;
import org.dynamicjava.osgi.da_launcher.internal.bundle.sources.BundleGroupSource;
import org.dynamicjava.osgi.da_launcher.internal.bundle.sources.BundlesDirectoryBundleSource;
import org.dynamicjava.osgi.da_launcher.internal.exceptions.ConfigurationException;
import org.w3c.dom.Element;

public class DirectoryBundleGroupSourceGenerator implements BundleGroupSourceGenerator {
	
	//@Override
	public BundleGroupSource generateBundleSource(Element bundleGroupElement) {
		try {
			XPath xpath = XPathFactory.newInstance().newXPath();
			Element directorySetttingsElement = (Element)xpath.evaluate(
					String.format("directory-settings"), bundleGroupElement, XPathConstants.NODE);
			
			String bundleGroupName = bundleGroupElement.getAttribute("name");
			
			String dirAttribute = directorySetttingsElement.getAttribute("dir");
			if (!StringUtils.hasText(dirAttribute)) {
				throw new ConfigurationException("XML element 'directory-settings' missing attribute 'dir'");
			}
			
			return new BundlesDirectoryBundleSource(bundleGroupName, new File(dirAttribute));
		} catch (ConfigurationException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new ConfigurationException(String.format(": %s", ex.getMessage()), ex);
		}
	}
	
}
