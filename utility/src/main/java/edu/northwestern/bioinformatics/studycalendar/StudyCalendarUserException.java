package edu.northwestern.bioinformatics.studycalendar;

import org.springframework.validation.Errors;

/**
 * Base class for exceptions caused by misconfigurations or other user-induced and/or
 * user-recoverable problems.
 *
 * @author Rhett Sutphin
 */
public abstract class StudyCalendarUserException extends StudyCalendarRuntimeException {
    public StudyCalendarUserException(String message, Object... messageParameters) {
        super(message, messageParameters);
    }

    public void rejectInto(Errors errors) {
        errors.reject("error.literal", getMessage());
    }
}
