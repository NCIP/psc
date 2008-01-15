package edu.northwestern.bioinformatics.studycalendar.xml;

import org.w3c.dom.Document;

import java.util.Collection;

/**
 * This interface is for serializers which support creating a document out of
 * a collection of their target representations.
 * <p>
 * These collections are typically used in a read-only way, so no deserialization
 * is required.
 *
 * @author Rhett Sutphin
 */
public interface StudyCalendarXmlCollectionSerializer<R> extends StudyCalendarXmlSerializer<R> {
    org.dom4j.Document createDocument(Collection<R> collection);

    String createDocumentString(Collection<R> collection);
}
