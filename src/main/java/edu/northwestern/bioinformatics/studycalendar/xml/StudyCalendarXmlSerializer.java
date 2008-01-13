package edu.northwestern.bioinformatics.studycalendar.xml;

import gov.nih.nci.cabig.ctms.domain.DomainObject;
import org.dom4j.Element;

/**
 * Serializes & deserializes some object to XML.  Where possible,
 * the objects will be {@link DomainObject}s.  However, some serialized
 * representations do not map directly on to the domain.  Those objects
 * should live in {@link edu.northwestern.bioinformatics.studycalendar.xml.domain}.
 *
 * @author Rhett Sutphin
 */
public interface StudyCalendarXmlSerializer<R> {
    /**
     * Create a new XML element for the given object, including any
     * child elements.  This method may delegate to other StudyCalendarXmlSerializer instances,
     * but it is not necessary that each serialized domain object have a separate serializer.
     */
    Element createElement(R object);

    /**
     * Read the given XML element and its children, creating objects as appropriate.
     */
    R readElement(Element element);
}
