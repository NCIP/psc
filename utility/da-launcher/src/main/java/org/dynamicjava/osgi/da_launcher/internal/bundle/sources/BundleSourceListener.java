package org.dynamicjava.osgi.da_launcher.internal.bundle.sources;

import java.net.URL;

public interface BundleSourceListener {
	
	void added(URL bundleArchiveUrl);
	
	void updated(URL bundleArchiveUrl);
	
	void removed(URL bundleArchiveUrl);
	
}
