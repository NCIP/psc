package org.dynamicjava.osgi.da_launcher.internal.framework.equinox.v3;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map.Entry;

import org.dynamicjava.osgi.da_launcher.internal.exceptions.LauncherException;
import org.dynamicjava.osgi.da_launcher.internal.exceptions.LaunchingException;
import org.dynamicjava.osgi.da_launcher.internal.framework.OsgiFrameworkSettings;
import org.dynamicjava.osgi.da_launcher.internal.framework.support.AbstractOsgiFramework;
import org.osgi.framework.BundleContext;

public class EquinoxFramework extends AbstractOsgiFramework {
	
	@Override
	protected void startFramework() throws Exception {
		this.setEquinoxProperties();
		
		Object osgi = newOsgi();
		getReflectionHelper().getLaunchMethod().invoke(osgi);
		BundleContext bundleContext = (BundleContext)getReflectionHelper().getBundleContextMethod().invoke(osgi);
		
		this.setBundleContext(bundleContext);
		this.setOsgi(osgi);
		
		if (isStartOsgiConsole()) {
			startOsgiConsole();
		}
	}
	
	@Override
	protected void stopFramework() throws Exception {
		try {
			getReflectionHelper().getShutdownMethod().invoke(this.getOsgi());
		} finally {
			this.setBundleContext(null);
			this.setOsgi(null);
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
		return getReflectionHelper().getEquinoxClassLoader();
	}
	
	protected void startOsgiConsole() {
		try {
			String consoleClassName = "org.eclipse.osgi.framework.internal.core.FrameworkConsole";
			Class<?> consoleClass = getFrameworkClassLoader().loadClass(consoleClassName);
			Class<?>[] parameterTypes;
			Object[] parameters;
			parameterTypes = new Class[] { getOsgi().getClass(), String[].class};
			parameters = new Object[] { getOsgi(), new String[] { }};
			Constructor<?> constructor = consoleClass.getConstructor(parameterTypes);
			Runnable console = (Runnable)constructor.newInstance(parameters);
			Thread t = new Thread(console, "Equinox OSGi");
			t.setDaemon(false);
			t.start();
		} catch (Exception ex) {
			throw new LaunchingException("Failed to start Equinox console: " + ex.getMessage(), ex);
		}
	}
	
	protected void setEquinoxProperties() {
		setEquinoxProperty("eclipse.ignoreApp", "true");
		setEquinoxProperty("osgi.noShutdown", "true");
        setEquinoxProperty(CLEAN_STARTUP_PROPERTY, Boolean.toString(true));
		
		for (Entry<Object, Object> property : getSettings().getFrameworkSpecificProperties().entrySet()) {
			setEquinoxProperty(property.getKey().toString(), property.getValue().toString());
		}
		
        setEquinoxProperty("osgi.configuration.area", getSettings().getProfileDir());
        setEquinoxProperty("osgi.install.area", getSettings().getProfileDir());
	}
	
    protected void setEquinoxProperty(String name, String value) {
    	try {
    		getReflectionHelper().getSetPropertyMethod().invoke(null, name, value);
    	} catch (Throwable ex) {
    		throw new LauncherException(String.format("Failed to set Equinox property: %s",
    				ex.getMessage()), ex);
    	}
    }
	
	protected Object newOsgi() throws Exception {
		Object baseAdaptor = getReflectionHelper().getBaseAdaptorConstructor().newInstance(new Object[] { null });
		return getReflectionHelper().getOsgiConstructor().newInstance(baseAdaptor);
	}
	
	
    public EquinoxFramework(OsgiFrameworkSettings settings) {
    	super(settings);
    	this.setReflectionHelper(new ReflectionHelper(getClass().getClassLoader()));
    }
    
    public EquinoxFramework(OsgiFrameworkSettings settings, ClassLoader felixClassLoader) {
    	super(settings);
    	this.setReflectionHelper(new ReflectionHelper(felixClassLoader));
    }
	
	
	private Object osgi;
	protected Object getOsgi() {
		return osgi;
	}
	protected void setOsgi(Object osgi) {
		this.osgi = osgi;
	}
	
	
	private ReflectionHelper reflectionHelper;
	protected ReflectionHelper getReflectionHelper() {
		return reflectionHelper;
	}
	protected void setReflectionHelper(ReflectionHelper reflectionHelper) {
		this.reflectionHelper = reflectionHelper;
	}
	
	protected static class ReflectionHelper {
		
		private final Constructor<?> osgiConstructor;
		public Constructor<?> getOsgiConstructor() {
			return osgiConstructor;
		}
		
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
		
		private final Constructor<?> baseAdaptorConstructor;
		public Constructor<?> getBaseAdaptorConstructor() {
			return baseAdaptorConstructor;
		}
		
		private final Method setPropertyMethod;
		public Method getSetPropertyMethod() {
			return setPropertyMethod;
		}
		
		private final ClassLoader equinoxClassLoader;
		public ClassLoader getEquinoxClassLoader() {
			return equinoxClassLoader;
		}
		
		public ReflectionHelper(ClassLoader equinoxClassLoader) {
			try {
				this.equinoxClassLoader = equinoxClassLoader;
				Class<?> baseAdaptorClass = equinoxClassLoader.loadClass(ApiMembersNames.Classes.BASE_ADAPTOR);
				baseAdaptorConstructor = baseAdaptorClass.getConstructor(String[].class);
				
				Class<?> frameworkAdaptorClass = equinoxClassLoader.loadClass(
						ApiMembersNames.Classes.FRAMEWORK_ADAPTOR);
				Class<?> osgiClass = equinoxClassLoader.loadClass(ApiMembersNames.Classes.OSGI);
				osgiConstructor = osgiClass.getConstructor(frameworkAdaptorClass);
				launchMethod = osgiClass.getMethod(ApiMembersNames.Methods.LAUNCH);
				shutdownMethod = osgiClass.getMethod(ApiMembersNames.Methods.SHUTDOWN);
				bundleContextMethod = osgiClass.getDeclaredMethod(ApiMembersNames.Methods.GET_BUNDLE_CONTEXT);
				
				Class<?> frameworkPropertiesClass = equinoxClassLoader.loadClass(
						ApiMembersNames.Classes.FRAMEWORK_PROPERTIES);
				setPropertyMethod = frameworkPropertiesClass.getMethod(
						ApiMembersNames.Methods.SET_PROPERTY, String.class, String.class);
			} catch (Throwable ex) {
				throw new LauncherException(String.format(
						"Failed to load Equinox members using Reflection API: %s", ex.getMessage()), ex);
			}
		}
		
	}

	
    protected static final String CLEAN_STARTUP_PROPERTY = "osgi.clean";
	
}
