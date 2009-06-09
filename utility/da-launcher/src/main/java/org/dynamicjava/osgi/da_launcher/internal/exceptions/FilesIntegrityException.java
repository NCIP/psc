package org.dynamicjava.osgi.da_launcher.internal.exceptions;

public class FilesIntegrityException extends LauncherException {
	
	private static final long serialVersionUID = ("urn:" + FilesIntegrityException.class.getName()).hashCode();
	
	public FilesIntegrityException() {
		super();
	}
	
	public FilesIntegrityException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public FilesIntegrityException(String message) {
		super(message);
	}
	
	public FilesIntegrityException(Throwable cause) {
		super(cause);
	}
	
}
