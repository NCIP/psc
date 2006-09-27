package edu.northwestern.bioinformatics.studycalendar;

/**
 * Exception for unexpected, unhandlable runtime conditions.  E.g, an InvocationTargetException
 * when it is not expected that the invoked method will throw an exception.
 *
 * @author Rhett Sutphin
 */
public class StudyCalendarSystemException extends RuntimeException {
    public StudyCalendarSystemException(String message) {
        super(message);
    }

    public StudyCalendarSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public StudyCalendarSystemException(Throwable cause) {
        super(cause);
    }
}
