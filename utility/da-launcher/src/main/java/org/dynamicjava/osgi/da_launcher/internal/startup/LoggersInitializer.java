package org.dynamicjava.osgi.da_launcher.internal.startup;

import java.io.File;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.dynamicjava.osgi.da_launcher.LauncherSettings;
import org.dynamicjava.osgi.da_launcher.internal.InternalLauncherConstants;
import org.dynamicjava.osgi.da_launcher.internal.config.LoggingConfig;
import org.dynamicjava.osgi.da_launcher.internal.exceptions.LauncherException;

public class LoggersInitializer {
	
	public void initSystemEventsLogger() {
		if (getLoggingConfig().getSystemEventsLoggerSettings().isEnabled()) {
			initLogger(Logger.getLogger(InternalLauncherConstants.Loggers.SYSTEM_EVENTS), "system-events.log");
		}
	}
	
	public void initOtherLoggers() {
		initBundleLifecycleEventsLogger();
	}
	
	public void removeConsoleHandlerFromRootLogger() {
		Logger rootLogger = Logger.getLogger("");
		for (Handler handler : rootLogger.getHandlers()) {
			if (handler instanceof ConsoleHandler) {
				rootLogger.removeHandler(handler);
			}
		}
	}
	
	
	protected void initBundleLifecycleEventsLogger() {
		if (getLoggingConfig().getBundleLifecycleEventsLoggerSettings().isEnabled()) {
			initLogger(Logger.getLogger(InternalLauncherConstants.Loggers.BUNDLE_LIFECYCLE_EVENTS),
					"bundle-lifecycle-events.log");
		}
	}
	
	protected void initLogger(Logger logger, String logFileName) {
		try {
			FileHandler eventsFileHandler = new FileHandler(getLogFileAbsolutePath(logFileName), true);
			eventsFileHandler.setFormatter(new SimpleFormatter());
			logger.addHandler(eventsFileHandler);
		} catch (Throwable ex) {
			throw new LauncherException(String.format("Failed to initialize JDK Logger '%s': %s",
					logger != null ? logger.getName() : "(null)", ex.getMessage()), ex);
		}
	}
	
	protected String getLogFileAbsolutePath(String logFileName) {
		return new File(getSettings().getDirectories().getLogsDir(), logFileName).getAbsolutePath();
	}
	
	
	public LoggersInitializer(LauncherSettings settings, LoggingConfig loggingConfig) {
		this.settings = settings;
		this.loggingConfig = loggingConfig;
	}
	
	private final LauncherSettings settings;
	protected LauncherSettings getSettings() {
		return settings;
	}
	
	private final LoggingConfig loggingConfig;
	protected LoggingConfig getLoggingConfig() {
		return loggingConfig;
	}
	
}
