/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml;

import gov.nih.nci.cabig.ctms.domain.DomainObject;
import org.dom4j.Document;
import org.dom4j.Element;

import java.io.InputStream;

/**
 * Serializes & deserializes some object to XML.  Where possible,
 * the objects will be {@link DomainObject}s.  However, some serialized
 * representations do not map directly on to the domain.  Those objects
 * should live in {@link edu.northwestern.bioinformatics.studycalendar.xml.domain}.
 * <p>
 * Some domain objects may have more than one XML representation, so there might be
 * multiple serializers with the same type parameter.
 *
 * @author Rhett Sutphin
 * @author John Dzak
 */
public interface StudyCalendarXmlSerializer<R> {
    /**
     * Create a document for the given object.
     */
    Document createDocument(R root);

    /**
     * Create a document for the given object and return it as string of XML.
     */
    String createDocumentString(R root);

    /**
     * Create a new XML element for the given object, including any
     * child elements.  This method may delegate to other StudyCalendarXmlSerializer instances,
     * but it is not necessary that each serialized domain object have a separate serializer.
     */
    Element createElement(R object);

    /**
     * Parse the given document and return the object(s) it represents appropriately.
     * <p>
     * Serializers may not need to implement this method.  In that case, they should throw
     * {@link UnsupportedOperationException} with an appropriate message.
     */
    R readDocument(Document document);

    /**
     * Parse a document out of the given reader and return the object(s) it represents appropriately.
     * <p>
     * Serializers may not need to implement this method.  In that case, they should throw
     * {@link UnsupportedOperationException} with an appropriate message.
     * @param in
     */
    R readDocument(InputStream in);

    /**
     * Read the given XML element and its children, creating objects as appropriate.
     * <p>
     * Serializers may not need to implement this method.  In that case, they should throw
     * {@link UnsupportedOperationException} with an appropriate message.
     */
    R readElement(Element element);
}
