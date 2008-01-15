package edu.northwestern.bioinformatics.studycalendar.xml;

import gov.nih.nci.cabig.ctms.domain.DomainObject;
import org.dom4j.Document;
import org.dom4j.Element;

import java.io.Reader;

/**
 * Serializes & deserializes some object to XML.  Where possible,
 * the objects will be {@link DomainObject}s.  However, some serialized
 * representations do not map directly on to the domain.  Those objects
 * should live in {@link edu.northwestern.bioinformatics.studycalendar.xml.domain}.
 *
 * @author Rhett Sutphin
 * @author John Dzak
 */
public interface StudyCalendarXmlSerializer<R> {
    /**
     * Create a document for the given object using the specified serializer.  Directly
     * specifying the serializer should only rarely be required.
     */
    org.dom4j.Document createDocument(R root);

    /**
     * Create a document for the given object using its default serializer and return it as string
     * of XML.
     */
    String createDocumentString(R root);

    /**
     * Create a new XML element for the given object, including any
     * child elements.  This method may delegate to other StudyCalendarXmlSerializer instances,
     * but it is not necessary that each serialized domain object have a separate serializer.
     */
    org.dom4j.Element createElement(R object);

    /**
     * Parse the given document and return the object(s) it represents appropriately.
     */
    R readDocument(Document document);

    /**
     * Parse a document out of the given reader and return the object(s) it represents appropriately.
     */
    R readDocument(Reader reader);

    /**
     * Read the given XML element and its children, creating objects as appropriate.
     */
    R readElement(Element element);
}
