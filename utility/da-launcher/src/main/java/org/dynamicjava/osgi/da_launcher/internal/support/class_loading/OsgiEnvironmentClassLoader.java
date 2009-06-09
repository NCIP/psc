package org.dynamicjava.osgi.da_launcher.internal.support.class_loading;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.service.packageadmin.PackageAdmin;

public class OsgiEnvironmentClassLoader extends ClassLoader implements BundleListener {
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		Map<Bundle, BundleClassLoader> bundleClassLoaders = getBundleClassLoaders();
		for (Bundle bundle : getBundles()) {
			if (canLoadResources(bundle)) {
				if (getPrioritizedBundles().contains(bundle) || containsClassPackage(bundle, name)) {
					try {
						return bundleClassLoaders.get(bundle).loadClass(name);
					} catch (ClassNotFoundException ex) {
						/// Continue searching in the next bundle.
					} catch (Throwable ex) {
						/// Catch any other exceptions which could be thrown by a bundle loading a class.
					}
				}
			}
		}
		
		if (getSupportingClassLoader() != null) {
			return getSupportingClassLoader().loadClass(name);
		} else {
			throw new ClassNotFoundException(String.format(
					"Class '%s' could not be load from %s", name, getClass().getSimpleName()));
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
		Map<Bundle, BundleClassLoader> bundleClassLoaders = getBundleClassLoaders();
		for (Bundle bundle : getBundles()) {
			if (canLoadResources(bundle)) {
				try {
					URL resource = bundleClassLoaders.get(bundle).getResource(name);
					if (resource != null) {
						return resource;
					}
				} catch (Throwable ex) {
					/// Catch any other exceptions which could be thrown by a bundle loading a resource.
				}
			}
		}
		
		if (getSupportingClassLoader() != null) {
			return getSupportingClassLoader().getResource(name);
		} else {
			return null;
		}
	}
	
	@Override
	public URL getResource(String name) {
		return findResource(name);
	}
	
	@Override
	protected Enumeration<URL> findResources(String name) throws IOException {
		Vector<URL> resources = new Vector<URL>();
		Map<Bundle, BundleClassLoader> bundleClassLoaders = getBundleClassLoaders();
		for (Bundle bundle : getBundles()) {
			if (canLoadResources(bundle)) {
				try {
					Enumeration<URL> bundleResources = bundleClassLoaders.get(bundle).getResources(name);
					if (bundleResources != null && bundleResources.hasMoreElements()) {
						while (bundleResources.hasMoreElements()) {
							resources.add(bundleResources.nextElement());
						}
					}
				}  catch (Throwable ex) {
					/// Catch any other exceptions which could be thrown by a bundle loading a resource.
				}
			}
		}
		
		if (resources.size() > 0) {
			return resources.elements();
		} else {
			if (getSupportingClassLoader() != null) {
				return getSupportingClassLoader().getResources(name);
			} else {
				return new Vector<URL>().elements();
			}
		}
	}
	
	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		return findResources(name);
	}
	
	//Override
	public void bundleChanged(BundleEvent bundleEvent) {
		if (bundleEvent.getType() == BundleEvent.INSTALLED) {
			addBundle(bundleEvent.getBundle());
		} else if (bundleEvent.getType() == BundleEvent.UNINSTALLED) {
			removeBundle(bundleEvent.getBundle());
		}
	}
	
	
	protected boolean canLoadResources(Bundle bundle) {
		return bundle.getState() != Bundle.UNINSTALLED;
	}
	
	protected boolean containsClassPackage(Bundle bundle, String className) {
		return (bundle.getEntry("/" + getPackageName(className)) != null
				|| bundle.getEntry("/" + className.replace('.', '/') + ".class") != null);
	}
	
	protected String getPackageName(String className) {
		int lastDotIndex = className.lastIndexOf('.');
		return lastDotIndex >= 0 ? className.substring(0, lastDotIndex).replace('.', '/') : "";
	}
	
	
	public OsgiEnvironmentClassLoader(BundleContext bundleContext,
			ClassLoader parent, Class<?>... prioritizedClasses) {
		this.bundleContext = bundleContext;
		this.supportingClassLoader = parent;
		
		initBundleClassLoaders(retrieveClassesBundles(prioritizedClasses));
		bundleContext.addBundleListener(this);
	}
	
	public OsgiEnvironmentClassLoader(BundleContext bundleContext,
			ClassLoader parent, Bundle... prioritizedBundles) {
		this.bundleContext = bundleContext;
		this.supportingClassLoader = parent;
		
		initBundleClassLoaders(prioritizedBundles);
		bundleContext.addBundleListener(this);
	}
	
	
	protected void initBundleClassLoaders(Bundle... priorityBundles) {
		getPrioritizedBundles().addAll(Arrays.asList(priorityBundles));
		addBundles(priorityBundles);
		addBundles(getBundleContext().getBundles());
	}
	
	protected void addBundles(Bundle... bundles) {
		Bundle systemBundle = null;
		for (Bundle bundle : bundles) {
			if (!getBundleClassLoaders().containsKey(bundle)) {
				/// If it's the System Bundle, then add it to the end of the list.
				if (bundle.getBundleId() != 0) {
					addBundle(bundle);
				} else {
					systemBundle = bundle;
				}
			}
		}
		
		if (systemBundle != null) {
			addBundle(systemBundle);
		}
	}
	
	protected synchronized void addBundle(Bundle bundle) {
		getBundles().add(bundle);
		getBundleClassLoaders().put(bundle, new BundleClassLoader(bundle));
	}
	
	protected synchronized void removeBundle(Bundle bundle) {
		getBundles().remove(bundle);
		getBundleClassLoaders().remove(bundle);
	}
	
	protected Bundle[] retrieveClassesBundles(Class<?>... classes) {
		PackageAdmin packageAdmin = (PackageAdmin)getBundleContext().getService(
				getBundleContext().getServiceReference(PackageAdmin.class.getName()));
		
		List<Bundle> priorityBundles = new ArrayList<Bundle>();
		for (Class<?> priorityClass : classes) {
			priorityBundles.add(packageAdmin.getBundle(priorityClass));
		}
		
		return priorityBundles.toArray(new Bundle[0]);
	}
	
	
	private final BundleContext bundleContext;
	protected BundleContext getBundleContext() {
		return bundleContext;
	}
	
	private final ClassLoader supportingClassLoader;
	protected ClassLoader getSupportingClassLoader() {
		return supportingClassLoader;
	}
	
	private final List<Bundle> bundles = new ArrayList<Bundle>();
	protected List<Bundle> getBundles() {
		return bundles;
	}
	
	private final List<Bundle> prioritizedBundles = new ArrayList<Bundle>();
	protected List<Bundle> getPrioritizedBundles() {
		return prioritizedBundles;
	}
	
	private final Map<Bundle, BundleClassLoader> bundleClassLoaders = new HashMap<Bundle, BundleClassLoader>();
	protected Map<Bundle, BundleClassLoader> getBundleClassLoaders() {
		return bundleClassLoaders;
	}
	
}
