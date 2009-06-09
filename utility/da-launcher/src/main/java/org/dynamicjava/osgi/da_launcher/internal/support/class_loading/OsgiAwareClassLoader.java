package org.dynamicjava.osgi.da_launcher.internal.support.class_loading;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class OsgiAwareClassLoader extends ClassLoader {
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (getOsgiEnvironmentClassLoader() != null) {
			return getOsgiEnvironmentClassLoader().loadClass(name);
		} else {
			return super.loadClass(name);
		}
	}
	
	@Override
	public URL getResource(String name) {
		if (getOsgiEnvironmentClassLoader() != null) {
			return getOsgiEnvironmentClassLoader().getResource(name);
		} else {
			return super.getResource(name);
		}
	}
	
	@Override
	public InputStream getResourceAsStream(String name) {
		if (getOsgiEnvironmentClassLoader() != null) {
			return getOsgiEnvironmentClassLoader().getResourceAsStream(name);
		} else {
			return super.getResourceAsStream(name);
		}
	}
	
	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		if (getOsgiEnvironmentClassLoader() != null) {
			return getOsgiEnvironmentClassLoader().getResources(name);
		} else {
			return super.getResources(name);
		}
	}
	
	
	public void useBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
		
		setOsgiEnvironmentClassLoader(new OsgiEnvironmentClassLoader(
				bundleContext, new ClassLoader() { }, new Bundle[] { }));
	}
	
	
	private BundleContext bundleContext;
	protected BundleContext getBundleContext() {
		return bundleContext;
	}
	
	private OsgiEnvironmentClassLoader osgiEnvironmentClassLoader;
	protected OsgiEnvironmentClassLoader getOsgiEnvironmentClassLoader() {
		return osgiEnvironmentClassLoader;
	}
	protected void setOsgiEnvironmentClassLoader(OsgiEnvironmentClassLoader osgiEnvironmentClassLoader) {
		this.osgiEnvironmentClassLoader = osgiEnvironmentClassLoader;
	}
	
}
