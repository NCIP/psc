package edu.northwestern.bioinformatics.studycalendar;

/**
 * PSC-specific error for fatal problems.  In general these will be caused by unrecoverable
 * configuration errors or incorrect assumptions.
 *
 * @author Rhett Sutphin
 */
public class StudyCalendarError extends Error {
    public StudyCalendarError(String message) {
        super(message);
    }

    public StudyCalendarError(String message, Throwable cause) {
        super(message, cause);
    }
}
