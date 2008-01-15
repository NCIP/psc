package edu.northwestern.bioinformatics.studycalendar.xml;


import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.Reader;

/**
 * @author Rhett Sutphin
 * @author John Dzak
 */
public abstract class AbstractStudyCalendarXmlSerializer<R> implements StudyCalendarXmlSerializer<R> {

    protected Document createDocumentInstance() {
        return DocumentHelper.createDocument();
    }
    
    public Document createDocument(R root) {
        throw new UnsupportedOperationException("TODO");
    }

    public String createDocumentString(R root) {
        return createDocument(root).asXML();
    }

    public R readDocument(Document document) {
        throw new UnsupportedOperationException("TODO");
    }

    public R readDocument(Reader reader) {
        throw new UnsupportedOperationException("TODO");
    }

    public abstract Element createElement(R object);

    public abstract R readElement(Element element);
}
