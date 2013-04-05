/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar;

/**
 * @author Rhett Sutphin
 */
public class StudyCalendarAuthorizationException extends StudyCalendarUserException {
    public StudyCalendarAuthorizationException(String message, Object... messageParameters) {
        super(message, messageParameters);
    }
}
