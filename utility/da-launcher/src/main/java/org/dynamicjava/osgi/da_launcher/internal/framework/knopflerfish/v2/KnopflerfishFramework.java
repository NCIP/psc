package org.dynamicjava.osgi.da_launcher.internal.framework.knopflerfish.v2;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.Map.Entry;

import org.dynamicjava.osgi.da_launcher.internal.exceptions.LauncherException;
import org.dynamicjava.osgi.da_launcher.internal.framework.OsgiFrameworkSettings;
import org.dynamicjava.osgi.da_launcher.internal.framework.support.AbstractOsgiFramework;
import org.osgi.framework.BundleContext;

public class KnopflerfishFramework extends AbstractOsgiFramework {
	
	@Override
	protected void startFramework() throws Exception {
		this.setKnopflerfishProperties();
		
		Object framework = newFramework();
		getReflectionHelper().getLaunchMethod().invoke(framework, 0);
		BundleContext bundleContext =
			(BundleContext)getReflectionHelper().getBundleContextMethod().invoke(framework);
		
		this.setFramework(framework);
		this.setBundleContext(bundleContext);
	}
	
	protected Object newFramework() throws Exception {
		return getReflectionHelper().getFrameworkConstructor().newInstance(this);
	}
	
	@Override
	protected void stopFramework() throws Exception {
		try {
			getReflectionHelper().getShutdownMethod().invoke(this.getFramework());
		} finally {
			this.setBundleContext(null);
			this.setFramework(null);
		}
	}
	
	@Override
	protected String getFilterImplClassName() {
		return ApiMembersNames.Classes.FILTER_IMPL;
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
	protected ClassLoader getFrameworkClassLoader() {
		return getReflectionHelper().getKnopflerfishClassLoader();
	}
	
	
	protected void setKnopflerfishProperties() {
		Properties knopflerfishProperties = new Properties();
		
		knopflerfishProperties.setProperty("org.osgi.framework.dir", getSettings().getProfileDir());
		knopflerfishProperties.setProperty("org.knopflerfish.framework.bundlestorage", "file");
		knopflerfishProperties.setProperty("org.knopflerfish.framework.system.export.all", "true");
		knopflerfishProperties.setProperty("org.knopflerfish.framework.bundlestorage.file.reference", "true");
		knopflerfishProperties.setProperty("org.knopflerfish.framework.bundlestorage.file.unpack", "false");
		knopflerfishProperties.setProperty("org.knopflerfish.startlevel.use", "true");
		knopflerfishProperties.setProperty("org.knopflerfish.framework.exitonshutdown", "false");
		
		/// Commented to make the behavior consistent with other OSGi Frameworks and since I'm 
		/// not sure how this will affect applications embedded in non-osgi environments.
		/// Note: If you need to set the value of this property you can do this in 'osgi-framework.xml' file.
		//knopflerfishProperties.setProperty("org.knopflerfish.osgi.setcontextclassloader", "true");
		
		for (Entry<Object, Object> property : getSettings().getFrameworkSpecificProperties().entrySet()) {
			knopflerfishProperties.put(property.getKey().toString(), property.getValue().toString());
		}
		
		System.getProperties().putAll(knopflerfishProperties);
	}
	
	
    public KnopflerfishFramework(OsgiFrameworkSettings settings) {
    	super(settings);
    	this.setReflectionHelper(new ReflectionHelper(getClass().getClassLoader()));
    }
    
    public KnopflerfishFramework(OsgiFrameworkSettings settings, ClassLoader felixClassLoader) {
    	super(settings);
    	this.setReflectionHelper(new ReflectionHelper(felixClassLoader));
    }
	
    
    private Object framework;
	protected Object getFramework() {
		return framework;
	}
	protected void setFramework(Object framework) {
		this.framework = framework;
	}
    
	private ReflectionHelper reflectionHelper;
	protected ReflectionHelper getReflectionHelper() {
		return reflectionHelper;
	}
	protected void setReflectionHelper(ReflectionHelper reflectionHelper) {
		this.reflectionHelper = reflectionHelper;
	}
	
	
	protected static class ReflectionHelper {
		
		private final Method launchMethod;
		public Method getLaunchMethod() {
			return launchMethod;
		}
		
		private final Method shutdownMethod;
		public Method getShutdownMethod() {
			return shutdownMethod;
		}
		
		private final Method bundleContextMethod;
		protected Method getBundleContextMethod() {
			return bundleContextMethod;
		}
		
		private final Constructor<?> frameworkConstructor;
		public Constructor<?> getFrameworkConstructor() {
			return frameworkConstructor;
		}
		
		private final ClassLoader knopflerfishClassLoader;
		public ClassLoader getKnopflerfishClassLoader() {
			return knopflerfishClassLoader;
		}
		
		public ReflectionHelper(ClassLoader knopflerfishClassLoader) {
			try {
				this.knopflerfishClassLoader = knopflerfishClassLoader;
				Class<?> frameworkClass = knopflerfishClassLoader.loadClass(ApiMembersNames.Classes.FRAMEWORK);
				frameworkConstructor = frameworkClass.getConstructor(Object.class);
				
				launchMethod = frameworkClass.getMethod(ApiMembersNames.Methods.LAUNCH, long.class);
				shutdownMethod = frameworkClass.getMethod(ApiMembersNames.Methods.SHUTDOWN);
				bundleContextMethod = frameworkClass.getDeclaredMethod(
						ApiMembersNames.Methods.GET_SYSTEM_BUNDLE_CONTEXT);
			} catch (Throwable ex) {
				throw new LauncherException(String.format(
						"Failed to load Knopflerfish members using Reflection API: %s", ex.getMessage()), ex);
			}
		}
		
	}
	
}
