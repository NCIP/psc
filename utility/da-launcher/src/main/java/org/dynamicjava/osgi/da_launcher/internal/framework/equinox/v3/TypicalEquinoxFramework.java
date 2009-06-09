package org.dynamicjava.osgi.da_launcher.internal.framework.equinox.v3;

import java.util.Map.Entry;

import org.dynamicjava.osgi.da_launcher.internal.framework.OsgiFrameworkSettings;
import org.dynamicjava.osgi.da_launcher.internal.framework.support.AbstractOsgiFramework;
import org.eclipse.core.runtime.adaptor.LocationManager;
import org.eclipse.osgi.baseadaptor.BaseAdaptor;
import org.eclipse.osgi.framework.internal.core.FilterImpl;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.eclipse.osgi.framework.internal.core.OSGi;
import org.osgi.framework.BundleContext;

public class TypicalEquinoxFramework extends AbstractOsgiFramework {
	
	@Override
	protected void startFramework() throws Exception {
		this.setEquinoxProperties();
		
		OSGi osgi = newOsgi();
		osgi.launch();
		
		this.setBundleContext(osgi.getBundleContext());
		this.setOsgi(osgi);
	}
	
	@Override
	protected void stopFramework() throws Exception {
		try {
			this.getOsgi().shutdown();
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
		return FilterImpl.class.getName();
	}
	
	
	protected void setEquinoxProperties() {
		setEquinoxProperty("eclipse.ignoreApp", "true");
		setEquinoxProperty("osgi.noShutdown", "true");
        setEquinoxProperty(CLEAN_STARTUP_PROPERTY, Boolean.toString(true));
		
		for (Entry<Object, Object> property : getSettings().getFrameworkSpecificProperties().entrySet()) {
			setEquinoxProperty(property.getKey().toString(), property.getValue().toString());
		}
		
        setEquinoxProperty(LocationManager.PROP_CONFIG_AREA, getSettings().getProfileDir());
        setEquinoxProperty(LocationManager.PROP_INSTALL_AREA, getSettings().getProfileDir());
	}
	
    protected void setEquinoxProperty(String name, String value) {
        FrameworkProperties.setProperty(name, value);
    }
	
	protected OSGi newOsgi() {
		return new OSGi(new BaseAdaptor(null));
	}
    
    
    public TypicalEquinoxFramework(OsgiFrameworkSettings settings) {
    	super(settings);
    }
	
	
	private OSGi osgi;
	protected OSGi getOsgi() {
		return osgi;
	}
	protected void setOsgi(OSGi osgi) {
		this.osgi = osgi;
	}

	
    protected static final String CLEAN_STARTUP_PROPERTY = "osgi.clean";
	
}
