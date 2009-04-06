package org.dynamicjava.api_bridge;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ApiBridgeCache {
	
	private final Map<Method, Method> methodMap = new HashMap<Method, Method>();
	public Map<Method, Method> getMethodMap() {
		return methodMap;
	}
	
	private final Map<Class<?>, Class<?>> superClassMap = new HashMap<Class<?>, Class<?>>();
	protected Map<Class<?>, Class<?>> getSuperClassMap() {
		return superClassMap;
	}
	
	private final Map<Class<?>, Set<Class<?>>> interfaceMap = new HashMap<Class<?>, Set<Class<?>>>();
	protected Map<Class<?>, Set<Class<?>>> getInterfaceMap() {
		return interfaceMap;
	}
	
}
