package org.dynamicjava.osgi.da_launcher.internal.framework.felix.v1;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map.Entry;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.FilterImpl;
import org.apache.felix.framework.cache.BundleCache;
import org.apache.felix.main.Main;
import org.dynamicjava.osgi.commons.utilities.FileUtils;
import org.dynamicjava.osgi.commons.utilities.ReflectionUtils;
import org.dynamicjava.osgi.da_launcher.internal.framework.OsgiFrameworkSettings;
import org.dynamicjava.osgi.da_launcher.internal.framework.support.AbstractOsgiFramework;
import org.osgi.framework.BundleContext;

public class TypicalFelixFramework  extends AbstractOsgiFramework {
	
	@Override
	protected void startFramework() throws Exception {
		this.setFelixProperties();
		
		/// A call to the Main method of felix to run felix framework
		Main.main(new String[0]);
		
		/// We use reflection to acquire a reference to Bundle Context since it's not visible.
		Field felixField = Main.class.getDeclaredField(FELIX_FIELD);
		Method getBundleContextMethod = Felix.class.getDeclaredMethod(GET_BUNDLE_CONTEXT_METHOD);
		ReflectionUtils.makeAccessible(felixField);
		ReflectionUtils.makeAccessible(getBundleContextMethod);
		
		Felix felix = (Felix)felixField.get(Main.class);
		BundleContext bundleContext = (BundleContext)getBundleContextMethod.invoke(felix);
		
		setBundleContext(bundleContext);
		setFelix(felix);
	}
	
	@Override
	protected void stopFramework() throws Exception {
		try {
			this.getFelix().stop();
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
		return FilterImpl.class.getName();
	}
	
	
	protected void setFelixProperties() {
		getSettings().setProfileDir(FileUtils.createTempDir().getAbsolutePath());
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
    
    
    public TypicalFelixFramework(OsgiFrameworkSettings settings) {
    	super(settings);
    }
	
	
	private Felix felix;
	protected Felix getFelix() {
		return felix;
	}
	protected void setFelix(Felix felix) {
		this.felix = felix;
	}
	
	
	protected static final String FELIX_FIELD = "m_felix";
	
	protected static final String GET_BUNDLE_CONTEXT_METHOD = "getBundleContext";
	
	protected static final String CACHE_PROFILE_DIR_PROPERTY = "felix.cache.profiledir";
	
}
