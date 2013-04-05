/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml;

import org.dom4j.Document;
import org.dom4j.Element;

import java.io.InputStream;
import java.util.Collection;

/**
 * @author John Dzak
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class CapturingStudyCalendarXmlFactoryStub<R> implements StudyCalendarXmlCollectionSerializer<R> {
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

    public R readDocument(Document document) {
        throw new UnsupportedOperationException("readDocument not implemented");
    }

    public R readDocument(InputStream in) {
        throw new UnsupportedOperationException("readDocument not implemented");
    }

    public R readElement(Element element) {
        throw new UnsupportedOperationException("readElement not implemented");
    }

    public Collection<R> readCollectionDocument(InputStream in) {
        throw new UnsupportedOperationException("readCollectionDocument not implemented");
    }

    public Collection<R> readCollectionElement(Element element) {
        throw new UnsupportedOperationException("readCollectionElement not implemented");
    }
}
