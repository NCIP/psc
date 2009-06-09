package org.dynamicjava.osgi.da_launcher.internal.bundle.group;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dynamicjava.osgi.commons.utilities.Assert;
import org.dynamicjava.osgi.commons.utilities.BundleContextUtils;
import org.dynamicjava.osgi.commons.utilities.IoUtils;
import org.dynamicjava.osgi.commons.utilities.ShutdownableState;
import org.dynamicjava.osgi.da_launcher.internal.InternalLauncherConstants;
import org.dynamicjava.osgi.da_launcher.internal.bundle.config.BundleGroupSettings;
import org.dynamicjava.osgi.da_launcher.internal.bundle.processors.BundleProcessor;
import org.dynamicjava.osgi.da_launcher.internal.bundle.processors.DummyBundleProcessor;
import org.dynamicjava.osgi.da_launcher.internal.bundle.sources.BundleGroupSource;
import org.dynamicjava.osgi.da_launcher.internal.bundle.sources.BundleSourceListener;
import org.dynamicjava.osgi.da_launcher.internal.exceptions.BundleInstallationException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

public class DefaultBundleGroup implements BundleGroup {
	
	//@Override
	public synchronized void start(boolean installGroupBundles, boolean startGroupBundles) {
		getState().run();
		
		getSource().addListener(getBundleSourceListener());
		
		if (installGroupBundles) {
			installGroupBundles(startGroupBundles);
			
			if (startGroupBundles) {
				startGroupBundles();
			}
		}
	}
	
	//@Override
	public synchronized void stop() {
		getSource().removeListener(getBundleSourceListener());
		
		for (Bundle bundle : getBundleMap().values()) {
			try {
				bundle.uninstall();
			} catch (Exception ex) {
			}
		}
		
		getState().shutdown();
	}
	
	//@Override
	public synchronized void startGroupBundles() {
		for (Bundle bundle : getBundleMap().values()) {
			if (bundle.getState() != Bundle.ACTIVE && bundle.getState() != Bundle.STARTING) {
				startBundle(bundle);
			}
		}
	}
	
	//@Override
	public BundleGroupSettings getSettings() {
		return settings;
	}
	
	//@Override
	public Bundle[] getBundles() {
		return getBundleMap().values().toArray(new Bundle[0]);
	}
	
	
	protected synchronized Bundle[] installGroupBundles(boolean startBundles) {
		URL[] bundleArchiveUrls = getSource().getBundleUrls();
		URL[] processedBundleArchiveUrls = getProcessor().process(bundleArchiveUrls);
		
		List<Bundle> bundles = new ArrayList<Bundle>();
		for (URL bundleArchiveUrl : processedBundleArchiveUrls) {
			Bundle bundle = installBundle(bundleArchiveUrl);
			if (bundle != null) {
				bundles.add(bundle);
			}
		}
		
		if (startBundles) {
			for (Bundle bundle : bundles) {
				startBundle(bundle);
			}
		}
		
		return bundles.toArray(new Bundle[0]);
	}
	
	protected Bundle installBundle(URL bundleArchiveUrl) {
		String bundleLocation = formatBundleLocation(bundleArchiveUrl);
		try {
			return installBundle(bundleLocation, bundleArchiveUrl.openStream());
		} catch (BundleInstallationException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new BundleInstallationException(
					String.format("Failed to install Bundle[location = '%s']: %s",
							bundleLocation, ex.getMessage()), ex);
		}
	}
	
	protected Bundle installBundle(String bundleLocation, InputStream bundleArchiveIn) {
		try {
			try {
				Bundle bundle = getBundleContext().installBundle(bundleLocation, bundleArchiveIn);
				if (bundle.getSymbolicName() == null) {
					try {
						bundle.uninstall();
					} catch (Throwable ex) {
					}
					
					throw new BundleInstallationException(String.format(
							"Failed to install Bundle[location = '%s'] since it's an invalid OSGi bundle:" +
							" Bundle does not have a symbolic name",
							bundleLocation));
				}
				
				logger.info("Installed " + getBundleString(bundle));
				
				getBundleMap().put(bundleLocation, bundle);
				
				return bundle;
			} catch (BundleInstallationException ex) {
				throw ex;
			} catch (Throwable ex) {
				throw new BundleInstallationException(
						String.format("Failed to install Bundle[location = '%s']: %s",
								bundleLocation, ex.getMessage()), ex);
			} finally {
				IoUtils.closeIfPossible(bundleArchiveIn);
			}
		} catch (BundleInstallationException ex) {
			handleBundleInstallationException(bundleLocation, ex);
			return null;
		}
	}
	
	protected void handleBundleInstallationException(String bundleLocation,
			BundleInstallationException ex) {
		if (getSettings().isIgonreInstallErrors()) {
			logger.log(Level.WARNING, String.format("Failed to install Bundle[location = '%s']: %s",
					bundleLocation, ex.getMessage()), ex);
		} else {
			throw ex;
		}
	}
	
	protected boolean startBundle(Bundle bundle) {
		if (bundle.getHeaders().get(Constants.FRAGMENT_HOST) != null) {
			/// Fragment Host bundles can't be started.
			return false;
		}
		
		try {
			try {
				bundle.start();
				
				logger.info("Started " + getBundleString(bundle));
				
				return true;
			} catch (Throwable ex) {
				throw new BundleInstallationException(
						String.format("Failed to start %s: %s", getBundleString(bundle), ex.getMessage()), ex);
			}
		} catch (BundleInstallationException ex) {
			logger.log(Level.WARNING, "Failed to start " + getBundleString(bundle), ex);
			if (getSettings().isIgnoreStartErrors()) {
				return false;
			} else {
				throw ex;
			}
		}
	}
	
	protected boolean updateBundle(URL bundleArchiveUrl) {
		String bundleLocation = formatBundleLocation(bundleArchiveUrl);
		try {
			Bundle bundleToUpdate = BundleContextUtils.findBundleByLocation(bundleLocation, getBundleContext());
			if (bundleToUpdate == null) {
				throw new BundleInstallationException(String.format(
						"Failed to update bundle from location = '%s' since no bundle with such location exist",
						bundleArchiveUrl));
			}
			
			updateBundle(bundleToUpdate, bundleArchiveUrl.openStream());
			
			return true;
		} catch (BundleInstallationException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new BundleInstallationException(String.format(
					"Failed to update bundle from location = '%s': %s", bundleLocation, ex.getMessage()), ex);
		}
	}
	
	protected boolean updateBundle(Bundle bundle, InputStream bundleArchiveIn) {
		try {
			try {
				bundle.update(bundleArchiveIn);
				
				logger.info("Updated " + getBundleString(bundle));
				
				return true;
			} catch (Throwable ex) {
				throw new BundleInstallationException(String.format("Failed to update %s: %s",
						getBundleString(bundle), ex.getMessage()), ex);
			}
		} catch (BundleInstallationException ex) {
			if (getSettings().isIgonreInstallErrors()) {
				logger.log(Level.WARNING, "Failed to update " + getBundleString(bundle), ex);
				return false;
			} else {
				throw ex;
			}
		}
	}
	
	protected boolean uninstallBundle(Bundle bundle) {
		try {
			String bundleLocation = bundle.getLocation();
			
			bundle.uninstall();
			
			logger.info("Uninstalled " + getBundleString(bundle));
			
			getBundleMap().remove(bundleLocation);
			
			return true;
		} catch (Throwable ex) {
			throw new BundleInstallationException(
					String.format("Failed to uninstall %s: %s", bundle, ex.getMessage()), ex);
		}
	}
	
	protected String formatBundleLocation(URL bundleArchiveUrl) {
		return bundleArchiveUrl.toString();
	}
	
	protected String getBundleString(Bundle bundle) {
		return String.format("Bundle[symbolic-name = '%s', location = '%s']",
				bundle.getSymbolicName(), bundle.getLocation());
	}
	
	
	public DefaultBundleGroup(BundleContext bundleContext, BundleGroupSource bundleGroupSource) {
		this.bundleContext = bundleContext;
		setSource(bundleGroupSource);
	}
	
	
	private final BundleContext bundleContext;
	protected BundleContext getBundleContext() {
		return bundleContext;
	}
	
	private String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	private BundleGroupSource source;
	public BundleGroupSource getSource() {
		return source;
	}
	public void setSource(BundleGroupSource bundleGroupSource) {
		Assert.notNull(bundleGroupSource, "Argument 'bundleGroupSource' can not be null.");
		this.source = bundleGroupSource;
	}
	
	private BundleProcessor processor;
	public BundleProcessor getProcessor() {
		if (processor == null) {
			processor = new DummyBundleProcessor();
		}
		return processor;
	}
	public void setProcessor(BundleProcessor bundleGroupProcessor) {
		this.processor = bundleGroupProcessor;
	}
	
	private BundleGroupSettings settings = new BundleGroupSettings();
	public void setSettings(BundleGroupSettings settings) {
		this.settings = settings;
	}
	
	private final ShutdownableState state = new ShutdownableState("DefaultBundleGroup");
	protected ShutdownableState getState() {
		return state;
	}
	
	private final BundleSourceListener bundleSourceListener = new InternalBundleSourceListener();
	protected BundleSourceListener getBundleSourceListener() {
		return bundleSourceListener;
	}
	
	private final Map<String, Bundle> bundleMap = new HashMap<String, Bundle>();
	protected Map<String, Bundle> getBundleMap() {
		return bundleMap;
	}
	
	
	protected class InternalBundleSourceListener implements BundleSourceListener {
		//@Override
		public synchronized void added(URL bundleArchiveUrl) {
			Bundle bundle = getBundleMap().get(bundleArchiveUrl.toString());
			if (bundle == null) {
				bundle = installBundle(bundleArchiveUrl);
				if (bundle != null && getSettings().isAutoStartBundles()) {
					startBundle(bundle);
				}
			} else {
				updateBundle(bundleArchiveUrl);
			}
		}
		
		//@Override
		public synchronized void removed(URL bundleArchiveUrl) {
			Bundle bundle = getBundleMap().get(bundleArchiveUrl.toString());
			if (bundle != null) {
				uninstallBundle(bundle);
			}
		}
		
		//@Override
		public synchronized void updated(URL bundleArchiveUrl) {
			Bundle bundle = getBundleMap().get(bundleArchiveUrl.toString());
			if (bundle != null) {
				updateBundle(bundleArchiveUrl);
			}
		}
		
	}
	
	
	protected static final Logger logger = Logger.getLogger(InternalLauncherConstants.Loggers.BUNDLE_LIFECYCLE_EVENTS);
	
}
