package edu.northwestern.bioinformatics.studycalendar;

import static java.lang.String.*;

/**
 * Base class for all PSC exceptions.  Cannot be used directly -- a typed exception
 * should be used instead. 
 *
 * @author Rhett Sutphin
 */
public abstract class StudyCalendarRuntimeException extends RuntimeException {
    public StudyCalendarRuntimeException() {
    }

    public StudyCalendarRuntimeException(Throwable cause) {
        super(cause);
    }

    public StudyCalendarRuntimeException(String message, Object... messageParameters) {
        super(format(message, messageParameters));
    }

    public StudyCalendarRuntimeException(String message, Throwable cause, Object... messageParameters) {
        super(format(message, messageParameters), cause);
    }
}
