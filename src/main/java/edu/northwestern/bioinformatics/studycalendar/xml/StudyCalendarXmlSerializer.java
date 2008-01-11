package edu.northwestern.bioinformatics.studycalendar.xml;

import gov.nih.nci.cabig.ctms.domain.DomainObject;
import org.dom4j.Element;

/**
 * @author Rhett Sutphin
 */
public interface StudyCalendarXmlSerializer<R extends DomainObject> {
    /**
     * Create a new XML element for the given object, including any
     * child elements.  This method may delegate to other StudyCalendarXmlSerializer instances,
     * but it is not necessary that each serialized domain object have a separate serializer.
     */
    Element createElement(R object);

    /**
     * Read the given XML element and its children, creating and storing
     * domain objects as appropriate.
     */
    R readElement(Element element);
}
