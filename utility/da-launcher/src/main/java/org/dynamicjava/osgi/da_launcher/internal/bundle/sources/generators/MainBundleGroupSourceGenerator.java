package org.dynamicjava.osgi.da_launcher.internal.bundle.sources.generators;

import org.dynamicjava.osgi.da_launcher.internal.bundle.sources.BundleGroupSource;
import org.dynamicjava.osgi.da_launcher.internal.exceptions.ConfigurationException;
import org.w3c.dom.Element;

public class MainBundleGroupSourceGenerator implements BundleGroupSourceGenerator {
	
	public BundleGroupSource generateBundleSource(Element bundleGroupElement) {
		try {
			String bundleGroupName = bundleGroupElement.getLocalName();
			BundleGroupSourceGenerator bundleSourceGenerator = getBundleSourceGenerator(bundleGroupName);
			if (bundleSourceGenerator != null) {
				return bundleSourceGenerator.generateBundleSource(bundleGroupElement);
			} else {
				throw new ConfigurationException(String.format(
						"Failed to load Bundle Group configuration '%s': No Bundle Source Generator was found.",
						bundleGroupName));
			}
		} catch (Exception ex) {
			throw new ConfigurationException(String.format(
					"Failed to load Bundle Group configuration: %s",
					ex.getMessage()), ex);
		}
	}
	
	protected BundleGroupSourceGenerator getBundleSourceGenerator(String bundleGroupName) {
		if ("bundles-directory".equals(bundleGroupName)) {
			return new DirectoryBundleGroupSourceGenerator();
		} else {
			return null;
		}
	}
	
}
