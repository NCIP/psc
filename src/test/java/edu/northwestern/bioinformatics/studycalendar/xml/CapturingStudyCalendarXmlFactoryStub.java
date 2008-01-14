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

    public Document createDocument(Object root) {
        lastObjectDocumented = root;
        return null;
    }

    public <R> Document createDocument(R root, StudyCalendarXmlSerializer<R> serializer) {
        lastObjectDocumented = root;
        lastSpecifiedSerializer = serializer;
        return null;
    }

    public String createDocumentString(Object root) {
        lastObjectStringified = root;
        return XML_STRING;
    }

    public Object readDocument(Document document) {
        throw new UnsupportedOperationException("readDocument not implemented");
    }

    public Object readDocument(Reader reader) {
        throw new UnsupportedOperationException("readDocument not implemented");
    }
}
