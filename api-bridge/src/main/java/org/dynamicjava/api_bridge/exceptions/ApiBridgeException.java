package org.dynamicjava.api_bridge.exceptions;

public class ApiBridgeException extends RuntimeException {
	
	private static final long serialVersionUID = ("urn:" + ApiBridgeException.class.getName()).hashCode();
	
	public ApiBridgeException() {
		super();
	}
	
	public ApiBridgeException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ApiBridgeException(String message) {
		super(message);
	}
	
	public ApiBridgeException(Throwable cause) {
		super(cause);
	}
	
}
