package edu.northwestern.bioinformatics.studycalendar.xml;

import org.w3c.dom.Document;

import java.io.Reader;

/**
 * @author Rhett Sutphin
 */
public class CapturingStudyCalendarXmlFactoryStub implements StudyCalendarXmlFactory {
    public static final String XML_STRING = "<psc-fake/>";

    private Object lastObjectStringified, lastObjectDocumented;
    private StudyCalendarXmlSerializer lastSpecifiedSerializer;

    public Object getLastObjectStringified() {
        return lastObjectStringified;
    }

    public Object getLastObjectDocumented() {
        return lastObjectDocumented;
    }

    public StudyCalendarXmlSerializer getLastSpecifiedSerializer() {
        return lastSpecifiedSerializer;
    }

    ////// STUBS

    public <R> Document createDocument(R root, StudyCalendarXmlSerializer<R> serializer) {
        lastObjectDocumented = root;
        lastSpecifiedSerializer = serializer;
        return null;
    }

    public <R> String createDocumentString(R root, StudyCalendarXmlSerializer<R> serializer) {
        lastObjectStringified = root;
        return XML_STRING;
    }

    public <R> R readDocument(Document document, StudyCalendarXmlSerializer<R> serializer) {
        throw new UnsupportedOperationException("readDocument not implemented");
    }

    public <R> R readDocument(Reader reader, StudyCalendarXmlSerializer<R> serializer) {
        throw new UnsupportedOperationException("readDocument not implemented");
    }
}
