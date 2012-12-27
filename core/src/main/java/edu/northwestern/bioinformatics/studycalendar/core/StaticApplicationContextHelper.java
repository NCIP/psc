/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.core;

import org.springframework.context.ApplicationContext;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;

/**
 * Helper for loading a spring application context and statically caching it.  If loading fails,
 * it caches the error and does not retry loading to improve performance.
 *
 * @author Rhett Sutphin
 */
public abstract class StaticApplicationContextHelper {
    protected abstract ApplicationContext createApplicationContext();

    private ApplicationContext applicationContext = null;
    private Throwable acLoadingFailure = null;

    public synchronized ApplicationContext getApplicationContext() {
        if (applicationContext == null && acLoadingFailure == null) {
            try {
                applicationContext = createApplicationContext();
            } catch (RuntimeException e) {
                acLoadingFailure = e;
                throw e;
            } catch (Error e) {
                acLoadingFailure = e;
                throw e;
            }
        } else if (acLoadingFailure != null) {
            throw new StudyCalendarSystemException("Application context loading already failed; will not retry.", acLoadingFailure);
        }
        return applicationContext;
    }
}
