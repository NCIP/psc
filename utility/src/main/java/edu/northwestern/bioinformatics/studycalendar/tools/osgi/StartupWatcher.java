/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools.osgi;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.startlevel.StartLevel;

/**
* @author Rhett Sutphin
*/
class StartupWatcher extends Thread implements FrameworkListener {
    private int expectedStartLevel, currentStartLevel;
    private Throwable error;

    StartupWatcher(int expectedStartLevel) {
        this.expectedStartLevel = expectedStartLevel;
        this.currentStartLevel = 0;
    }

    public synchronized void frameworkEvent(FrameworkEvent frameworkEvent) {
        if (isOfType(frameworkEvent, FrameworkEvent.ERROR)) {
            error = frameworkEvent.getThrowable();
        } else if (isOfType(frameworkEvent, FrameworkEvent.STARTLEVEL_CHANGED)) {
            currentStartLevel = getStartLevelService(frameworkEvent.getBundle().getBundleContext()).getStartLevel();
        }

        if (error != null || isAtExpectedStartLevel()) {
            this.notify();
        }
    }

    private boolean isOfType(FrameworkEvent frameworkEvent, int type) {
        return (frameworkEvent.getType() & type) != 0;
    }

    public synchronized void waitForStart(long timeoutMilliseconds) {
        if (!isAtExpectedStartLevel()) {
            try {
                this.wait(timeoutMilliseconds);
            } catch (InterruptedException e) {
                throw new StudyCalendarSystemException(
                    "Wait for embedded OSGi framework to start was interrupted", e);
            }
            if (error != null) {
                throw new StudyCalendarSystemException(
                    "Embedded OSGi framework startup failed", error);
            } else if (!isAtExpectedStartLevel()) {
                throw new StudyCalendarSystemException(
                    "Embedded OSGi framework did not start within %dms", timeoutMilliseconds);
            }
        }
    }

    public synchronized boolean isAtExpectedStartLevel() {
        return error == null && currentStartLevel >= expectedStartLevel;
    }

    private static StartLevel getStartLevelService(BundleContext bundleContext) {
        ServiceReference ref =
            bundleContext.getServiceReference(StartLevel.class.getName());
        if (ref == null) {
            throw new StudyCalendarSystemException(
                "Could not get a reference to an instance of the start level service");
        }
        return (StartLevel) bundleContext.getService(ref);
    }
}
