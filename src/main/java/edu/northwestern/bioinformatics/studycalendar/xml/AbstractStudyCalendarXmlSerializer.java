package edu.northwestern.bioinformatics.studycalendar.xml;

import org.w3c.dom.Document;
import org.dom4j.Element;

import java.io.Reader;

/**
 * @author Rhett Sutphin
 * @author John Dzak
 */
public abstract class AbstractStudyCalendarXmlSerializer<R> implements StudyCalendarXmlSerializer<R> {
    public Document createDocument(Object root) {
        throw new UnsupportedOperationException("TODO");
    }

    public String createDocumentString(Object root) {
        throw new UnsupportedOperationException("TODO");
    }

    public R readDocument(Document document) {
        throw new UnsupportedOperationException("TODO");
    }

    public R readDocument(Reader reader) {
        throw new UnsupportedOperationException("TODO");
    }

    public abstract Element createElement(Object object);

    public abstract R readElement(Element element);
}
