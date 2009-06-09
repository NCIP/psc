package org.dynamicjava.osgi.da_launcher.internal.bundle.processors;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BundleProcessorChain implements BundleProcessor {
	
	//@Override
	public URL[] process(URL[] bundleUrls) {
		URL[] newBundleUrls = bundleUrls;
		for (BundleProcessor bundleProcessor : getBundleProcessors()) {
			newBundleUrls = bundleProcessor.process(newBundleUrls);
		}
		return newBundleUrls;
	}
	
	public void add(BundleProcessor bundleProcessor) {
		getBundleProcessors().add(bundleProcessor);
	}
	
	
	public BundleProcessorChain(BundleProcessor... bundleProcessors) {
		if (bundleProcessors != null && bundleProcessors.length > 0) {
			getBundleProcessors().addAll(Arrays.asList(bundleProcessors));
		}
	}
	
	private final List<BundleProcessor> bundleProcessors = new ArrayList<BundleProcessor>();
	protected List<BundleProcessor> getBundleProcessors() {
		return bundleProcessors;
	}
	
}
