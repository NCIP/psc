package org.dynamicjava.osgi.da_launcher.internal.bundle.sources;

import java.net.URL;

public interface BundleGroupSource {
	
	URL[] getBundleUrls();
	
	void addListener(BundleSourceListener listener);
	
	void removeListener(BundleSourceListener listener);
	
}
