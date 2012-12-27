/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar;

/**
 * Exception for unexpected, unhandlable runtime conditions.  E.g, an InvocationTargetException
 * when it is not expected that the invoked method will throw an exception.
 *
 * @author Rhett Sutphin
 */
public class StudyCalendarSystemException extends StudyCalendarRuntimeException {
    public StudyCalendarSystemException(String message, Object... messageParameters) {
        super(message, messageParameters);
    }

    public StudyCalendarSystemException(String message, Throwable cause, Object... messageParameters) {
        super(message, messageParameters, cause);
    }

    public StudyCalendarSystemException(Throwable cause) {
        super(cause);
    }
}
