package org.dynamicjava.api_bridge.exceptions;

public class UntranslatableClassException extends ApiBridgeException {
	
	private static final long serialVersionUID = ("urn:" + UntranslatableClassException.class.getName()).hashCode();
	
	public UntranslatableClassException(Class<?> clazz) {
		super(String.format("Class '%s' can not be bridged since it has no super classes or"
				+ " interfaces that belong to the bridged API packages", clazz.getName()));
	}
	
}
