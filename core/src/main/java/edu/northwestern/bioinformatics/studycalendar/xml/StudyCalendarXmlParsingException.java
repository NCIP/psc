package edu.northwestern.bioinformatics.studycalendar.xml;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarUserException;
import org.dom4j.DocumentException;

/**
 * @author Rhett Sutphin
 */
public class StudyCalendarXmlParsingException extends StudyCalendarUserException {
    public StudyCalendarXmlParsingException(DocumentException cause) {
        super("Could not parse the provided XML: %s", cause.getMessage(), cause);
    }
}
