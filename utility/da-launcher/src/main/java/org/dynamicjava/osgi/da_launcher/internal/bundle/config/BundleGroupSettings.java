package org.dynamicjava.osgi.da_launcher.internal.bundle.config;

public class BundleGroupSettings {
	
	private int installOrder = 3;
	public int getInstallOrder() {
		return installOrder;
	}
	public void setInstallOrder(int installOrder) {
		this.installOrder = installOrder;
	}
	
	private int startOrder = 3;
	public int getStartOrder() {
		return startOrder;
	}
	public void setStartOrder(int startOrder) {
		this.startOrder = startOrder;
	}
	
	private boolean autoStartBundles = true;
	public boolean isAutoStartBundles() {
		return autoStartBundles;
	}
	public void setAutoStartBundles(boolean autoStartBundles) {
		this.autoStartBundles = autoStartBundles;
	}
	
	private boolean ignoreInstallErrors = false;
	public boolean isIgonreInstallErrors() {
		return ignoreInstallErrors;
	}
	public void setIgnoreInstallErrors(boolean ignoreInstallErrors) {
		this.ignoreInstallErrors = ignoreInstallErrors;
	}
	
	private boolean ignoreStartErrors = false;
	public boolean isIgnoreStartErrors() {
		return ignoreStartErrors;
	}
	public void setIgnoreStartErrors(boolean ignoreStartErrors) {
		this.ignoreStartErrors = ignoreStartErrors;
	}
	
}
