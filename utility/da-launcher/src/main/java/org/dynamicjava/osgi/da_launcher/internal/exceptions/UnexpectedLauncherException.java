package org.dynamicjava.osgi.da_launcher.internal.exceptions;

public class UnexpectedLauncherException extends LauncherException {
	
	private static final long serialVersionUID = ("urn:" + UnexpectedLauncherException.class.getName()).hashCode();
	
	public UnexpectedLauncherException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public UnexpectedLauncherException(Throwable cause) {
		super(String.format("An Unexcpected Exception was thrown: <%s> - %s", cause.getMessage()), cause);
	}
	
}
