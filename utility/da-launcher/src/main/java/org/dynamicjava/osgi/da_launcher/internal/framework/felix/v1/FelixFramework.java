package org.dynamicjava.osgi.da_launcher.internal.framework.felix.v1;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map.Entry;

import org.dynamicjava.osgi.commons.utilities.ReflectionUtils;
import org.dynamicjava.osgi.da_launcher.internal.exceptions.LauncherException;
import org.dynamicjava.osgi.da_launcher.internal.framework.OsgiFrameworkSettings;
import org.dynamicjava.osgi.da_launcher.internal.framework.support.AbstractOsgiFramework;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class FelixFramework extends AbstractOsgiFramework {
	
	@Override
	protected void startFramework() throws Exception {
		/// This code was written to get rid of the message "Welcome to Felix." that is printed using System.out
		/// instead of being logged by some logging utility.
		PrintStream systemOut = System.out;
		PrintStream systemErr = System.err;
		try {
			System.setOut(new PrintStream(new ByteArrayOutputStream()));
			System.setErr(new PrintStream(new ByteArrayOutputStream()));
		} catch (Throwable ex) {
			/// We don't have the permission, it seems that we have to bear with the messages.
		}
		
		try {
			this.setFelixProperties();
			
			/// A call to the Main method of felix to run felix framework
			ReflectionHelper reflectionHelper = this.getReflectionHelper();
			reflectionHelper.getMainMethod().invoke(null, new Object[]{ new String[0] });
			
			Object felix = reflectionHelper.getFelixField().get(reflectionHelper.getMainClass());
			BundleContext bundleContext = (BundleContext)reflectionHelper.getBundleContextMethod().invoke(felix);
			
			setBundleContext(bundleContext);
			setFelix(felix);
		} finally {
			try {
				System.setOut(systemOut);
				System.setErr(systemErr);
			} catch (Throwable ex) {
			}
		}
	}

	@Override
	protected void stopFramework() throws Exception {
		try {
			((Bundle)this.getFelix()).stop();
		} finally {
			this.setBundleContext(null);
			this.setFelix(null);
		}
	}
	
	@Override
	protected BundleContext returnBundleContext() {
		return bundleContext;
	}
	protected void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}
	private BundleContext bundleContext;
	
	@Override
	protected String getFilterImplClassName() {
		return ApiMembersNames.Classes.FILTER_IMPL;
	}
	
	@Override
	protected ClassLoader getFrameworkClassLoader() {
		return getReflectionHelper().getFelixClassLoader();
	}
	
	
	protected void setFelixProperties() {
		setFelixProperty("felix.log.level", "0");
		setFelixProperty("felix.embedded.execution", "true");
		
		for (Entry<Object, Object> property : getSettings().getFrameworkSpecificProperties().entrySet()) {
			setFelixProperty(property.getKey().toString(), property.getValue().toString());
		}
		
		setFelixProperty(CACHE_PROFILE_DIR_PROPERTY, getSettings().getProfileDir());
	}
	
	protected void setFelixProperty(String name, String value) {
		System.getProperties().put(name, value);
	}
	
	
    public FelixFramework(OsgiFrameworkSettings settings) {
    	super(settings);
    	this.setReflectionHelper(new ReflectionHelper(getClass().getClassLoader()));
    }
    
    public FelixFramework(OsgiFrameworkSettings settings, ClassLoader felixClassLoader) {
    	super(settings);
    	this.setReflectionHelper(new ReflectionHelper(felixClassLoader));
    }
	
	
	private Object felix;
	protected Object getFelix() {
		return felix;
	}
	protected void setFelix(Object felix) {
		this.felix = felix;
	}
	
	
	private ReflectionHelper reflectionHelper;
	protected ReflectionHelper getReflectionHelper() {
		return reflectionHelper;
	}
	protected void setReflectionHelper(ReflectionHelper reflectionHelper) {
		this.reflectionHelper = reflectionHelper;
	}
	
	protected static class ReflectionHelper {
		
		private final Class<?> mainClass;
		public Class<?> getMainClass() {
			return mainClass;
		}
		
		private final Method mainMethod;
		public Method getMainMethod() {
			return mainMethod;
		}
		
		private final Class<?> felixClass;
		protected Class<?> getFelixClass() {
			return felixClass;
		}
		
		private final Field felixField;
		protected Field getFelixField() {
			return felixField;
		}
		
		private final Method bundleContextMethod;
		protected Method getBundleContextMethod() {
			return bundleContextMethod;
		}
		
		private final ClassLoader felixClassLoader;
		public ClassLoader getFelixClassLoader() {
			return felixClassLoader;
		}
		
		public ReflectionHelper(ClassLoader felixClassLoader) {
			try {
				this.felixClassLoader = felixClassLoader;
				mainClass = felixClassLoader.loadClass(ApiMembersNames.Classes.MAIN);
				mainMethod = mainClass.getMethod(ApiMembersNames.Methods.MAIN, String[].class);
				felixField = mainClass.getDeclaredField(ApiMembersNames.Fields.FELIX);
				
				felixClass = felixClassLoader.loadClass(ApiMembersNames.Classes.FELIX);
				bundleContextMethod = felixClass.getDeclaredMethod(ApiMembersNames.Methods.GET_BUNDLE_CONTEXT);
				
				ReflectionUtils.makeAccessible(felixField);
				ReflectionUtils.makeAccessible(bundleContextMethod);
			} catch (Exception ex) {
				throw new LauncherException(String.format(
						"Failed to load Felix members using Reflection API: %s", ex.getMessage()), ex);
			}
		}
		
	}
	
	
	protected static final String CACHE_PROFILE_DIR_PROPERTY = "felix.cache.profiledir";
	
}
