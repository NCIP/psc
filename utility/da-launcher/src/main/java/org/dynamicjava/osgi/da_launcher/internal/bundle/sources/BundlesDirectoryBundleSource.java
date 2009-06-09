package org.dynamicjava.osgi.da_launcher.internal.bundle.sources;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dynamicjava.osgi.commons.utilities.FileUtils;
import org.dynamicjava.osgi.da_launcher.internal.InternalLauncherConstants;
import org.dynamicjava.osgi.da_launcher.internal.exceptions.UnexpectedLauncherException;
import org.dynamicjava.osgi.da_launcher.internal.utilities.DirectoryWatcher;
import org.dynamicjava.osgi.da_launcher.internal.utilities.DirectoryWatcher.FileEvent;
import org.dynamicjava.osgi.da_launcher.internal.utilities.DirectoryWatcher.FileEventListener;

public class BundlesDirectoryBundleSource extends AbstractBundleSource implements BundleGroupSource {
	
	//@Override
	public URL[] getBundleUrls() {
		File[] files = FileUtils.listAllChildFiles(getBundlesDir());
		List<URL> urls = new ArrayList<URL>();
		
		for (File file : files) {
			if (isBnudleArchive(file)) {
				try {
					urls.add(file.toURI().toURL());
				} catch (MalformedURLException ex) {
					/// This exception shouldn't occur
					throw new UnexpectedLauncherException(ex);
				}
			}
		}
		
		return urls.toArray(new URL[0]);
	}
	
	
	protected boolean isBnudleArchive(File file) {
		int lastDotIndex = file.getName().lastIndexOf('.');
		if (lastDotIndex == -1) {
			return false;
		} else {
			String extension = file.getName().substring(lastDotIndex);
			/// return true if the extension is '.*ar', e.g., jar, war, ear, aar, etc.
			return extension.length() == 4 && extension.endsWith("ar");
		}
	}
	
	
	public BundlesDirectoryBundleSource(String bundleGroupName, File bundlesDir) {
		this.bundlesDir = bundlesDir;
		this.bundleGroupName = bundleGroupName;
		
		init();
	}
	
	protected void init() {
		DirectoryWatcher bundlesDirWatcher = new DirectoryWatcher(getBundlesDir(), 500);
		bundlesDirWatcher.addListener(getDirListener());
		bundlesDirWatcher.start();
		
		setBundlesDirWatcher(bundlesDirWatcher);
	}
	
	private DirectoryWatcher bundlesDirWatcher;
	protected DirectoryWatcher getBundlesDirWatcher() {
		return bundlesDirWatcher;
	}
	protected void setBundlesDirWatcher(DirectoryWatcher bundlesDirWatcher) {
		this.bundlesDirWatcher = bundlesDirWatcher;
	}
	
	private final File bundlesDir;
	public File getBundlesDir() {
		return bundlesDir;
	}
	
	private final String bundleGroupName;
	protected String getBundleGroupName() {
		return bundleGroupName;
	}
	
	
	private final FileEventListener dirListener = new DirListener();
	protected FileEventListener getDirListener() {
		return dirListener;
	}
	protected class DirListener implements FileEventListener {
		
		//@Override
		public synchronized void onFileEvent(FileEvent fileEvent) {
			try {
				if (!isBnudleArchive(fileEvent.getFile())) {
					return;
				}
				
				URL fileUrl = fileEvent.getFile().toURI().toURL();
				
				for (BundleSourceListener bundleSourceListener : getListeners()) {
					switch (fileEvent.getOperation()) {
					case ADDED:
						bundleSourceListener.added(fileUrl);
						break;
						
					case UPDATED:
						bundleSourceListener.updated(fileUrl);
						break;
						
					case REMOVED:
						bundleSourceListener.removed(fileUrl);
						break;
					}
				}
			} catch (Throwable ex) {
				logger.log(Level.WARNING, String.format(
						"Directory Bundle source of Bundle Group '%s' failed to handle file '%s': %s",
						getBundleGroupName(), fileEvent.getFile().getName(), ex.getMessage()), ex);
			}
		}
		
	}
	
	
	protected static final Logger logger = Logger.getLogger(InternalLauncherConstants.Loggers.BUNDLE_LIFECYCLE_EVENTS);
	
}
