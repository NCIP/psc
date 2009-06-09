package org.dynamicjava.osgi.da_launcher.internal.exceptions;


public class LaunchingException extends LauncherException {
	
	private static final long serialVersionUID = ("urn:" + LaunchingException.class.getName()).hashCode();
	
	public LaunchingException() {
		super();
	}
	
	public LaunchingException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public LaunchingException(String message) {
		super(message);
	}
	
	public LaunchingException(Throwable cause) {
		super(cause);
	}
	
}
