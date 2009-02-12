package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarUserException;

/**
 * Exception indicating that a requested {@link AuthenticationSystem} plugin could not be loaded
 * (i.e., the class was not found or did not implement the interface).
 *
 * @author Rhett Sutphin
 */
public class AuthenticationSystemLoadingFailure extends StudyCalendarUserException {

    public AuthenticationSystemLoadingFailure(String message, Object... messageParameters) {
        super(message, messageParameters);
    }

    public AuthenticationSystemLoadingFailure(String message, Throwable cause, Object... messageParameters) {
        super(message, cause, messageParameters);
    }
}
