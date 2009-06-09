package org.dynamicjava.osgi.da_launcher.internal.exceptions;

public class BundleInstallationException extends LauncherException {
	
	private static final long serialVersionUID = ("urn:" + BundleInstallationException.class.getName()).hashCode();
	
	public BundleInstallationException() {
		super();
	}
	
	public BundleInstallationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public BundleInstallationException(String message) {
		super(message);
	}
	
	public BundleInstallationException(Throwable cause) {
		super(cause);
	}
	
}
