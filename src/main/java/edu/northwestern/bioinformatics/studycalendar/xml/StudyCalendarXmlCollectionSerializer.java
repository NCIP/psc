package edu.northwestern.bioinformatics.studycalendar.xml;

import org.dom4j.Document;
import org.dom4j.Element;

import java.util.Collection;
import java.io.InputStream;

/**
 * This interface is for serializers which support creating a document out of
 * a collection of their target representations.
 *
 * @author Rhett Sutphin
 */
public interface StudyCalendarXmlCollectionSerializer<R> extends StudyCalendarXmlSerializer<R> {
    Document createDocument(Collection<R> collection);

    String createDocumentString(Collection<R> collection);

    Collection<R> readCollectionDocument(InputStream in);

    Collection<R> readCollectionElement(Element element);
}
