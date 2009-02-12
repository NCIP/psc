package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarUserException;

/**
 * @author Saurabh Agrawal
 */
public class StudyImportException extends StudyCalendarUserException {

    public StudyImportException(String message, Object... messageParameters) {
        super(message, messageParameters);
    }

    public StudyImportException(String message, Throwable cause, Object... messageParameters) {
        super(message, cause, messageParameters);
    }
}
