package edu.northwestern.bioinformatics.studycalendar.xml;

import org.w3c.dom.Document;
import gov.nih.nci.cabig.ctms.domain.DomainObject;

import java.io.Reader;

/**
 * Object which reads and writes XML representations of various domain objects.  This class'
 * primary role is to select among the various implementations of {@link StudyCalendarXmlSerializer}
 * based on the document or domain object provided.
 *
 * @author Rhett Sutphin
 */
public interface StudyCalendarXmlFactory {
    /**
     * Create a document for the given object using the default serializer for its type.
     * Not all {@link DomainObject}s are supported; if the one passed in is not, this method
     * will throw {@link UnsupportedOperationException}.
     */
    Document createDocument(DomainObject root);

    /**
     * Create a document for the given object using the specified serializer.  Directly
     * specifying the serializer should only rarely be required.
     */
    <R extends DomainObject> Document createDocument(R root, StudyCalendarXmlSerializer<R> serializer);

    /**
     * Create a document for the given object using its default serializer and return it as string
     * of XML.
     */
    String createDocumentString(DomainObject root);

    /**
     * Parse the given document and store the object(s) it represents appropriately.
     */
    DomainObject readDocument(Document document);

    /**
     * Parse a document out of the given reader and store the object(s) it represents appropriately.
     */
    DomainObject readDocument(Reader reader);
}
