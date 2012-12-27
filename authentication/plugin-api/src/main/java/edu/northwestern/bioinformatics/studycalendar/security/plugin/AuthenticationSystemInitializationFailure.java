/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarUserException;

/**
 * Exception for a general expectation failure when an AuthenticationSystem plugin is
 * initializing.
 *
 * @author Rhett Sutphin
 */
public class AuthenticationSystemInitializationFailure extends StudyCalendarUserException {
    /**
     * @param message Error message, optionally using printf-style templating
     * @param messageParameters template values
     * @see String#format
     */
    public AuthenticationSystemInitializationFailure(String message, Object... messageParameters) {
        super(message, messageParameters);
    }

    /**
     * @param message Error message, optionally using printf-style templating
     * @param cause underlying cause for the problem
     * @param messageParameters template values
     * @see String#format
     */
    public AuthenticationSystemInitializationFailure(String message, Throwable cause, Object... messageParameters) {
        super(message, cause, messageParameters);
    }
}
