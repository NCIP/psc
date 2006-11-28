package edu.northwestern.bioinformatics.studycalendar;

/**
 * @author Rhett Sutphin
 */
public class StudyCalendarValidationException extends RuntimeException {
    public StudyCalendarValidationException(String message) {
        super(message);
    }

    public StudyCalendarValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
