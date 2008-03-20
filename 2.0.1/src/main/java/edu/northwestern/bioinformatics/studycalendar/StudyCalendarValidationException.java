package edu.northwestern.bioinformatics.studycalendar;

import org.springframework.validation.Errors;

import static java.lang.String.format;

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
