/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.direct;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarRuntimeException;

/**
 * @author Rhett Sutphin
 */
public class CasDirectException extends StudyCalendarRuntimeException {
    public CasDirectException(Throwable cause) {
        super(cause);
    }

    public CasDirectException(String message, Object... messageParameters) {
        super(message, messageParameters);
    }
}
