package org.dynamicjava.osgi.da_launcher.internal.utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class DirectoryWatcher {
	
	private final File dirToWatch;
	private final Timer timer;
	private final WatcherTimerTask timerTask = new WatcherTimerTask();
	private int watchIntervalInMillis;
	private boolean isCallbackAsync = false;
	private Map<File, Long> fileInfoMap;
	private final List<FileEventListener> listeners = new ArrayList<FileEventListener>();
	
	public DirectoryWatcher(File dirToWatch, int watchIntervalInMillis) {
		this.dirToWatch = dirToWatch;
		this.watchIntervalInMillis = watchIntervalInMillis;
		this.timer = new Timer();
	}
	
	public DirectoryWatcher(File dirToWatch, int watchIntervalInMillis, boolean isCallbackAsync) {
		this(dirToWatch, watchIntervalInMillis);
		this.isCallbackAsync = isCallbackAsync;
	}
	
	
	private boolean started = false;
	public synchronized void start() {
		if (started) {
			throw new IllegalStateException("Directory watcher is already started");
		}
		
		fileInfoMap = retrieveFileInfoMap(dirToWatch);
		
		timer.schedule(timerTask, 0, watchIntervalInMillis);
		
		started = true;
	}
	
	public synchronized void stop() {
		if (started) {
			timer.cancel();
			started = false;
		}
	}
	
	public synchronized void addListener(FileEventListener listener) {
		listeners.add(listener);
	}
	
	public synchronized void removeListener(FileEventListener listener) {
		listeners.remove(listener);
	}
	
	public void updateWatchInterval(int watchIntervalInMillis) {
		synchronized (this) {
			stop();
			this.watchIntervalInMillis = watchIntervalInMillis;
			start();
		}
	}
	
	
	protected Map<File, Long> retrieveFileInfoMap(File dir) {
		Map<File, Long> result = new HashMap<File, Long>();
		readFilesInfo(dir, result);
		return result;
	}
	
	protected void readFilesInfo(File dir, Map<File, Long> fileInfoMap) {
		File[] files = dir.listFiles();
		for (File file : files) {
			fileInfoMap.put(file, file.lastModified());
			if (file.isDirectory()) {
				readFilesInfo(file, fileInfoMap);
			}
		}
	}
	
	protected void notifyListeners(final File file, final FileOperation operation) {
		final FileEvent fileEvent = new FileEvent(file, operation);
		for (final FileEventListener listener : listeners) {
			if (isCallbackAsync) {
				new Thread(new Runnable() {
					public void run() {
						try {
							listener.onFileEvent(fileEvent);
						} catch (Throwable ex) {
						}
					}
				}).start();
			} else {
				try {
					listener.onFileEvent(fileEvent);
				} catch (Throwable ex) {
				}
			}
		}
	}
	
	
	private class WatcherTimerTask extends TimerTask {
		@Override
		public synchronized void run() {
			Map<File, FileOperation> changes = new HashMap<File, FileOperation>();
			
			Map<File, Long> currentFileInfoMap = retrieveFileInfoMap(dirToWatch);
			
			for (Map.Entry<File, Long> currentEntry : currentFileInfoMap.entrySet()) {
				File currentFile = currentEntry.getKey();
				Long lastModified = currentEntry.getValue();
				
				if (fileInfoMap.containsKey(currentFile)) {
					if (!lastModified.equals(fileInfoMap.get(currentFile))) {
						changes.put(currentFile, FileOperation.UPDATED);
					}
				} else {
					changes.put(currentFile, FileOperation.ADDED);
				}
			}

			for (Map.Entry<File, Long> pastEntry : fileInfoMap.entrySet()) {
				File pastFile = pastEntry.getKey();
				if (!currentFileInfoMap.containsKey(pastFile)) {
					changes.put(pastFile, FileOperation.REMOVED);
				}
			}
			
			for (Map.Entry<File, FileOperation> changeEntry : changes.entrySet()) {
				notifyListeners(changeEntry.getKey(), changeEntry.getValue());
			}
			
			fileInfoMap = currentFileInfoMap;
		}
	}
	
	
	public static interface FileEventListener {
		void onFileEvent(FileEvent fileEvent);
	}
	
	public class FileEvent {
		
		private final Object sender;
		public Object getSender() {
			return sender;
		}
		
		private final File file;
		public File getFile() {
			return file;
		}
		
		private final FileOperation operation;
		public FileOperation getOperation() {
			return operation;
		}
		
		private FileEvent(File file, FileOperation operation) {
			this.sender = DirectoryWatcher.this;
			this.file = file;
			this.operation = operation;
		}
		
	}
	
	
	public static enum FileOperation { ADDED, UPDATED, REMOVED }
	
}
