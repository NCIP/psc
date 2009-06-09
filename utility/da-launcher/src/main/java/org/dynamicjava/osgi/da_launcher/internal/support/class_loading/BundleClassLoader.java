package org.dynamicjava.osgi.da_launcher.internal.support.class_loading;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import org.osgi.framework.Bundle;

public class BundleClassLoader extends ClassLoader {
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			return getBundle().loadClass(name);
		}
		catch (ClassNotFoundException ex) {
			throw new ClassNotFoundException(String.format(
					"Class '%s' could not be load from Bundle[symbolic-name = '%s']",
					name, getBundle().getSymbolicName()));
		}
	}
	
	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class<?> clazz = findClass(name);
		if (resolve) {
			resolveClass(clazz);
		}
		return clazz;
	}
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return loadClass(name, false);
	}
	
	@Override
	protected URL findResource(String name) {
		return getBundle().getEntry(name);
	}
	
	@Override
	public URL getResource(String name) {
		return findResource(name);
	}
	
	@Override
	protected Enumeration<URL> findResources(String name) throws IOException {
		//return getBundle().findEntries(name, "*", false);
		Vector<URL> resources = new Vector<URL>();
		URL resource = getResource(name);
		if (resource != null) {
			resources.add(resource);
		}
		return resources.elements();
	}
	
	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		return findResources(name);
	}
	
	@Override
	public String toString() {
		return String.format("BundleClassLoader[bundle-symbolic-name = '%s']",
				getBundle().getSymbolicName());
	}
	
	
	public BundleClassLoader(Bundle bundle) {
		this.bundle = bundle;
	}
	
	
	private final Bundle bundle;
	protected Bundle getBundle() {
		return bundle;
	}
	
}
