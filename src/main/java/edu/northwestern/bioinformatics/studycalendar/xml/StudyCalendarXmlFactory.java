package edu.northwestern.bioinformatics.studycalendar.xml;

import org.w3c.dom.Document;

import java.io.Reader;

/**
 * Object which reads and writes XML representations of various domain objects.  This class'
 * primary role is to select among the various implementations of {@link StudyCalendarXmlSerializer}
 * based on the document or domain object provided.
 *
 * @author Rhett Sutphin
 */
@Deprecated
public interface StudyCalendarXmlFactory {
    /**
     * Create a document for the given object using the specified serializer.  Directly
     * specifying the serializer should only rarely be required.
     */
    <R> Document createDocument(R root, StudyCalendarXmlSerializer<R> serializer);

    /**
     * Create a document for the given object using its default serializer and return it as string
     * of XML.
     */
    <R> String createDocumentString(R root, StudyCalendarXmlSerializer<R> serializer);

    /**
     * Parse the given document and return the object(s) it represents appropriately.
     */
    <R> R readDocument(Document document, StudyCalendarXmlSerializer<R> serializer);

    /**
     * Parse a document out of the given reader and return the object(s) it represents appropriately.
     */
    <R> R readDocument(Reader reader, StudyCalendarXmlSerializer<R> serializer);
}
