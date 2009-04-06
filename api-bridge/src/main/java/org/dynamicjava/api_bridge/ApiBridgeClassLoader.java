package org.dynamicjava.api_bridge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * When classes are loaded, the class loader will first look into the parent class loader, and if
 * the class was not fount it will look for it in the child class loader. As for classes in exceptional
 * packages, the class loader will look into the child first, then into the parent.
 * 
 * @author vladrin 
 */
public class ApiBridgeClassLoader extends ClassLoader {
	
	public static ClassLoader getClassLoader(ClassLoader parent,
			ClassLoader child, String... exceptionalPackages) {
		String classLoaderKey = formClassLoaderKey(parent, child, exceptionalPackages);
		ClassLoader result = getClassLoaderMap().get(classLoaderKey);
		if (result == null) {
			result = new ApiBridgeClassLoader(parent, child, exceptionalPackages);
			getClassLoaderMap().put(classLoaderKey, result);
		}
		return result;
	}
	
	private static String formClassLoaderKey(ClassLoader parent,
			ClassLoader child, String[] exceptionalPackages) {
		StringBuffer result = new StringBuffer();
		result.append(parent.hashCode());
        if (child != null) {
            result.append(':');
            result.append(child.hashCode());
        }
		result.append('-');
		for (String packageName : exceptionalPackages) {
			result.append(packageName.hashCode());
			result.append(':');
		}
		return result.toString();
	}
	
	private static final Map<String, ClassLoader> classLoaderMap = new HashMap<String, ClassLoader>();
	private static Map<String, ClassLoader> getClassLoaderMap() {
		return classLoaderMap;
	}
	
	
	protected ApiBridgeClassLoader(ClassLoader parent, ClassLoader child, String... exceptionalPackages) {
		super(parent);
		
		this.child = child;
		
		if (exceptionalPackages != null) {
			this.exceptoinalPackages = Arrays.asList(exceptionalPackages);
		} else {
			this.exceptoinalPackages = new ArrayList<String>();
		}
	}
	
	private final ClassLoader child;
	protected ClassLoader getChild() {
		return child;
	}
	
	private List<String> exceptoinalPackages;
	protected List<String> getExceptoinalPackages() {
		return exceptoinalPackages;
	}
	
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (isExceptionalClass(name)) {
			return loadClass(name, getChild(), getParent());
		} else {
			return loadClass(name, getParent(), getChild());
		}
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException {
		return loadClass(name);
	}
	
	
	protected Class<?> loadClass(String name, ClassLoader firstClassLoader, ClassLoader secondClassLoader)
			throws ClassNotFoundException {
		try {
			return firstClassLoader.loadClass(name);
		} catch (ClassNotFoundException ex){
			return secondClassLoader.loadClass(name);
		}
	}
	
	protected boolean isExceptionalClass(String className) {
		for (String exceptionalPackage : getExceptoinalPackages()) {
			if (className.startsWith(exceptionalPackage)) {
				return true;
			}
		}
		return false;
	}
	
}
