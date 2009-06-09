package org.dynamicjava.osgi.da_launcher.internal.bundle.processors;

import java.net.URL;

public class DummyBundleProcessor implements BundleProcessor {
	
	//@Override
	public URL[] process(URL[] bundleUrls) {
		return bundleUrls;
	}
	
}
