package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlSerializer;
import org.dom4j.Element;

/**
 * @author Rhett Sutphin
 */
public interface ChangeXmlSerializer extends StudyCalendarXmlSerializer<Change> {
    StringBuffer validateElement(Change change, Element eChange);
}
