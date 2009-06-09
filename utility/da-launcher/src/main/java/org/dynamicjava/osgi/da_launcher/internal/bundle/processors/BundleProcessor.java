package org.dynamicjava.osgi.da_launcher.internal.bundle.processors;

import java.net.URL;

public interface BundleProcessor {
	
	URL[] process(URL[] bundleUrls);
	
}
