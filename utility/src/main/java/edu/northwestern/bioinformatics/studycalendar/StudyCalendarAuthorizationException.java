package edu.northwestern.bioinformatics.studycalendar;

/**
 * @author Rhett Sutphin
 */
public class StudyCalendarAuthorizationException extends StudyCalendarUserException {
    public StudyCalendarAuthorizationException(String message, Object... messageParameters) {
        super(message, messageParameters);
    }
}
