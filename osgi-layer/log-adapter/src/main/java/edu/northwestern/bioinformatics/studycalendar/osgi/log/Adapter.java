/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.osgi.log;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rhett Sutphin
 */
public class Adapter implements LogListener, ServiceListener, BundleActivator {
    private static final String LOG_READER = LogReaderService.class.getName();
    private BundleContext bundleContext;

    public void start(BundleContext context) throws Exception {
        this.bundleContext = context;
        ServiceReference sr = context.getServiceReference(LOG_READER);
        if (sr != null) {
            registerSelf(sr);
        }
        context.addServiceListener(this, "(objectClass=" + LOG_READER + ')');
    }

    public void stop(BundleContext context) throws Exception {
    }

    public void serviceChanged(ServiceEvent evt) {
        if (evt.getType() == ServiceEvent.REGISTERED) {
            registerSelf(evt.getServiceReference());
        }
    }

    public void logged(LogEntry logEntry) {
        Logger logger = LoggerFactory.getLogger(buildLoggerName(logEntry));
        switch (logEntry.getLevel()) {
            case LogService.LOG_DEBUG:   logger.debug(logEntry.getMessage(), logEntry.getException()); break;
            case LogService.LOG_INFO:    logger.info (logEntry.getMessage(), logEntry.getException()); break;
            case LogService.LOG_WARNING: logger.warn (logEntry.getMessage(), logEntry.getException()); break;
            case LogService.LOG_ERROR:   logger.error(logEntry.getMessage(), logEntry.getException()); break;
        }
    }

    private String buildLoggerName(LogEntry logEntry) {
        StringBuilder name = new StringBuilder();
        if (logEntry.getServiceReference() != null) {
            name.append("service: ");
            String pid = (String) logEntry.getServiceReference().getProperty(Constants.SERVICE_PID);
            if (pid != null) {
                name.append(pid);
            } else {
                name.append("anonymous");
            }
            name.append('#').append(logEntry.getServiceReference().getProperty(Constants.SERVICE_ID));
        } else {
            name.append(" bundle: ").append(logEntry.getBundle().getSymbolicName()).
                append('#').append(logEntry.getBundle().getBundleId());
        }
        return name.toString();
    }

    private void registerSelf(ServiceReference reference) {
        LogReaderService service = (LogReaderService) bundleContext.getService(reference);
        if (service != null) {
            service.addLogListener(this);
        }
    }
}
