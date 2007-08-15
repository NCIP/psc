package edu.northwestern.bioinformatics.studycalendar;

import static java.lang.String.*;

/**
 * PSC-specific error for fatal problems.  In general these will be caused by unrecoverable
 * configuration errors or incorrect assumptions.
 *
 * @author Rhett Sutphin
 */
public class StudyCalendarError extends Error {
    public StudyCalendarError(String message, Object... messageParameters) {
        super(format(message, messageParameters));
    }

    public StudyCalendarError(String message, Throwable cause, Object... messageParameters) {
        super(format(message, messageParameters), cause);
    }
}
