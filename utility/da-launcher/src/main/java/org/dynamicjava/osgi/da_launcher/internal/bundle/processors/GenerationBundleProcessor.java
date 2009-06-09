package org.dynamicjava.osgi.da_launcher.internal.bundle.processors;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.dynamicjava.osgi.bundler.BundleGenerator;
import org.dynamicjava.osgi.bundler.BundleGeneratorFactory;
import org.dynamicjava.osgi.bundler.support.BundleCollectionGenerationParams;
import org.dynamicjava.osgi.commons.utilities.FileUtils;
import org.dynamicjava.osgi.da_launcher.internal.LauncherContext;
import org.dynamicjava.osgi.da_launcher.internal.exceptions.LauncherException;

public class GenerationBundleProcessor implements BundleProcessor {
	
	public static enum HandlingType { ALL /*, NON_BUNDLED_ONLY*/ }
	
	
	//@Override
	public URL[] process(URL[] bundleUrls) {
		try {
			File[] bundleFiles = convertToFiles(bundleUrls);
			getCacheDir().mkdirs();
			
			if (isFirstTimeProcessing()) {
				synchronized (this) {
					setFirstTimeProcessing(false);
					
					File[] generatedBundleFiles = getBundleGenerator().generate(
							new BundleCollectionGenerationParams(bundleFiles, getCacheDir(), true, false));
					return convertToUrls(generatedBundleFiles);
				}
			} else {
				File[] generatedBundleFiles = getBundleGenerator().generate(
						new BundleCollectionGenerationParams(bundleFiles, getCacheDir(), true, true));
				return convertToUrls(generatedBundleFiles);
			}
		} catch (Throwable ex) {
			throw new LauncherException(String.format(
					"Bundle Processor of Bundle Group '%s' failed to process bundles: %s",
					getBundleGroupName(), ex.getMessage()), ex);
		}
	}
	
	protected File[] convertToFiles(URL[] urls) throws URISyntaxException {
		File[] files = new File[urls.length];
		for (int i = 0; i < urls.length; i++) {
			files[i] = new File(urls[i].toURI());
		}
		return files;
	}
	
	protected URL[] convertToUrls(File[] files) throws MalformedURLException {
		URL[] urls = new URL[files.length];
		for (int i = 0; i < files.length; i++) {
			urls[i] = files[i].toURI().toURL();
		}
		return urls;
	}
	
	
	public GenerationBundleProcessor(String bundleGroupName, LauncherContext launcherContext) {
		this.bundleGroupName = bundleGroupName;
		this.launcherContext = launcherContext;
		this.bundleGenerator = BundleGeneratorFactory.getDefaultFactory().create(launcherContext.getBundleContext());
	}
	
	
	private final String bundleGroupName;
	protected String getBundleGroupName() {
		return bundleGroupName;
	}
	
	private final LauncherContext launcherContext;
	protected LauncherContext getLauncherContext() {
		return launcherContext;
	}
	
	private File cacheDir;
	public File getCacheDir() {
		if (cacheDir == null) {
			cacheDir = FileUtils.createTempDir();
		}
		return cacheDir;
	}
	public void setCacheDir(File cacheDir) {
		this.cacheDir = cacheDir;
	}
	
	private HandlingType handlingType = HandlingType.ALL;
	public HandlingType getHandlingType() {
		return handlingType;
	}
	public void setHandlingType(HandlingType handlingType) {
		this.handlingType = handlingType;
	}
	
	private boolean firstTimeProcessing = true;
	protected boolean isFirstTimeProcessing() {
		return firstTimeProcessing;
	}
	protected void setFirstTimeProcessing(boolean firstTimeProcessing) {
		this.firstTimeProcessing = firstTimeProcessing;
	}
	
	private final BundleGenerator bundleGenerator;
	protected BundleGenerator getBundleGenerator() {
		return bundleGenerator;
	}
	
	
	protected static final String BUNDLE_GENERATION_SETTINGS_FILE_SUFFIX = ".properties";
	
}
