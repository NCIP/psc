package org.dynamicjava.osgi.da_launcher.internal.framework.vendor;

import java.lang.reflect.Constructor;

import org.dynamicjava.osgi.da_launcher.internal.exceptions.LauncherException;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;

public class FrameworkUtil {
	
	public static Filter createFilter(String filter) throws InvalidSyntaxException {
		try {
			return (Filter)getFilterImplConstructor().newInstance(filter);
		} catch (Throwable ex) {
			throw new LauncherException("Failed to create Filter from the OSGi Framework", ex);
		}
	}
	
	
	public static void setClassLoader(ClassLoader classLoader, String filterImplClassName) {
		try {
			Class<?> filterImplClass = classLoader.loadClass(filterImplClassName);
			filterImplConstructor = filterImplClass.getConstructor(String.class);
		} catch (Throwable ex) {
			throw new LauncherException(ex.getMessage(), ex);
		}
	}
	
	
	private static Constructor<?> filterImplConstructor;
	private static Constructor<?> getFilterImplConstructor() {
		return filterImplConstructor;
	}
	
}
