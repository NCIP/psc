package org.dynamicjava.api_bridge.utilities;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class ClassUtils {
	
	public static Class<?>[] getAllInterfaces(Class<?> clazz) {
		if (clazz.isInterface()) {
			return new Class[] { clazz };
		}
		
		Set<Class<?>> interfaces = new HashSet<Class<?>>();
		while (clazz != null) {
			for (int i = 0; i < clazz.getInterfaces().length; i++) {
				interfaces.add(clazz.getInterfaces()[i]);
			}
			clazz = clazz.getSuperclass();
		}
		return interfaces.toArray(new Class[interfaces.size()]);
	}
	
	public static String getPackageName(Class<?> clazz) {
		String className = clazz.getName();
		int lastDotIndex = className.lastIndexOf('.');
		return (lastDotIndex != -1 ? className.substring(0, lastDotIndex) : "");
	}
	
	public static Method findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
		int paramsLength = paramTypes.length;
		String[] requiredParamNames = new String[paramTypes.length];
		for (int i = 0; i < paramsLength; i++) {
			requiredParamNames[i] = paramTypes[i].getName();
		}
		
		for (Method method : clazz.getMethods()) {
			if (method.getName().equals(methodName)) {
				if (method.getParameterTypes().length == paramsLength) {
					Class<?>[] methodParamTypes = method.getParameterTypes();
					boolean allParamsMatch = true;
					for (int i = 0; i < methodParamTypes.length; i++) {
						if (!methodParamTypes[i].getName().equals(requiredParamNames[i])) {
							allParamsMatch = false;
							break;
						}
					}
					if (allParamsMatch) {
						return method;
					}
				}
			}
		}
		
		return null;
	}
	
}
