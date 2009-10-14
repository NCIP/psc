package org.dynamicjava.osgi.da_launcher.internal.framework.felix.v1;

import org.apache.felix.framework.Felix;
import org.dynamicjava.osgi.da_launcher.internal.framework.OsgiFrameworkSettings;
import org.dynamicjava.osgi.da_launcher.internal.framework.support.AbstractOsgiFramework;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Properties;


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
            //starting felix directly instead of using main method of the felix Main class
            Properties configMap = getSettings().getFrameworkSpecificProperties();
            configMap.put("org.osgi.framework.storage", getSettings().getProfileDir());
            Felix m_felix = new Felix(configMap);
            m_felix.start();
		    BundleContext bundleContext = m_felix.getBundleContext();
		    setBundleContext(bundleContext);
		    setFelix(m_felix);
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
	protected void setFelixProperty(String name, String value) {
		System.getProperties().put(name, value);
	}


    public FelixFramework(OsgiFrameworkSettings settings) {
    	super(settings);
    }

    public FelixFramework(OsgiFrameworkSettings settings, ClassLoader felixClassLoader) {
    	super(settings);
    }


	private Object felix;
	protected Object getFelix() {
		return felix;
	}
	protected void setFelix(Object felix) {
		this.felix = felix;
	}
}
