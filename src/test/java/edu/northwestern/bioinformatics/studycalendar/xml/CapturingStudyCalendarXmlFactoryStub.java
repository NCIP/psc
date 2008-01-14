package edu.northwestern.bioinformatics.studycalendar.xml;

import org.w3c.dom.Document;
import org.dom4j.Element;

import java.io.Reader;
import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class CapturingStudyCalendarXmlFactoryStub implements StudyCalendarXmlCollectionSerializer {
    public static final String XML_STRING = "<psc-fake/>";

    private Object lastObjectStringified, lastObjectDocumented, lastObjectElementified;

    ////// CAPTURED DATA ACCESSORS

    public Object getLastObjectStringified() {
        return lastObjectStringified;
    }

    public Object getLastObjectDocumented() {
        return lastObjectDocumented;
    }

    public Object getLastObjectElementified() {
        return lastObjectElementified;
    }

    ////// STUBS

    public Document createDocument(Object root) {
        lastObjectDocumented = root;
        return null;
    }

    public String createDocumentString(Object root) {
        lastObjectStringified = root;
        return XML_STRING;
    }

    public Element createElement(Object object) {
        lastObjectElementified = object;
        return null;
    }

    public Document createDocument(Collection collection) {
        lastObjectDocumented = collection;
        return null;
    }

    public String createDocumentString(Collection collection) {
        lastObjectStringified = collection;
        return XML_STRING;
    }

    public Object readDocument(Document document) {
        throw new UnsupportedOperationException("readDocument not implemented");
    }

    public Object readDocument(Reader reader) {
        throw new UnsupportedOperationException("readDocument not implemented");
    }

    public Object readElement(Element element) {
        throw new UnsupportedOperationException("readElement not implemented");
    }
}
