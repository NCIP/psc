package org.dynamicjava.osgi.da_launcher.internal.bundle.sources.generators;

import org.dynamicjava.osgi.da_launcher.internal.bundle.sources.BundleGroupSource;
import org.w3c.dom.Element;

public interface BundleGroupSourceGenerator {
	
	public BundleGroupSource generateBundleSource(Element bundleGroupElement);
	
}
