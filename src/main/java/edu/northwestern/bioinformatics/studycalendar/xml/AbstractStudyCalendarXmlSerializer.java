package edu.northwestern.bioinformatics.studycalendar.xml;

import org.w3c.dom.Document;
import org.dom4j.Element;

import java.io.Reader;

public abstract class AbstractStudyCalendarXmlSerializer implements StudyCalendarXmlSerializer {
    public Document createDocument(Object root, StudyCalendarXmlSerializer studyCalendarXmlSerializer) {
        throw new UnsupportedOperationException("TODO");
    }

    public String createDocumentString(Object root, StudyCalendarXmlSerializer studyCalendarXmlSerializer) {
        throw new UnsupportedOperationException("TODO");
    }

    public Object readDocument(Document document, StudyCalendarXmlSerializer studyCalendarXmlSerializer) {
        throw new UnsupportedOperationException("TODO");
    }

    public Object readDocument(Reader reader, StudyCalendarXmlSerializer studyCalendarXmlSerializer) {
        throw new UnsupportedOperationException("TODO");
    }

    public abstract Element createElement(Object object);

    public abstract Object readElement(Element element);
}
