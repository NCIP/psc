package org.dynamicjava.osgi.da_launcher.internal.exceptions;

public class LauncherException extends RuntimeException {
	
	private static final long serialVersionUID = ("urn:" + LauncherException.class.getName()).hashCode();
	
	public LauncherException() {
		super();
	}
	
	public LauncherException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public LauncherException(String message) {
		super(message);
	}
	
	public LauncherException(Throwable cause) {
		super(cause);
	}
	
}
