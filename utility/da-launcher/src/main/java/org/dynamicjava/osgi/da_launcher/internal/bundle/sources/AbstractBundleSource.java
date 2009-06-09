package org.dynamicjava.osgi.da_launcher.internal.bundle.sources;

import java.util.ArrayList;
import java.util.List;

public class AbstractBundleSource {
	
	public void addListener(BundleSourceListener listener) {
		getListeners().add(listener);
	}
	
	public void removeListener(BundleSourceListener listener) {
		getListeners().remove(listener);
	}
	
	
	private final List<BundleSourceListener> listeners = new ArrayList<BundleSourceListener>();
	protected List<BundleSourceListener> getListeners() {
		return listeners;
	}
	
}
