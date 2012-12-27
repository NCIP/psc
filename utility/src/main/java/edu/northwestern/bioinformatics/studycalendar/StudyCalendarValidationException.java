/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar;

/**
 * @author Rhett Sutphin
 */
public class StudyCalendarValidationException extends StudyCalendarUserException {
    public StudyCalendarValidationException(String message, Object... messageParameters) {
        super(message, messageParameters);
    }

    public StudyCalendarValidationException(String message, Throwable cause, Object... messageParameters) {
        super(message, cause, messageParameters);
    }
}
