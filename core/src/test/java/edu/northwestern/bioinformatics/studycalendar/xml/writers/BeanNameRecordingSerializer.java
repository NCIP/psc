package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlSerializer;
import org.dom4j.Document;
import org.dom4j.Element;

import java.io.InputStream;

/**
 * A fake serializer for testing.
 *
 * @author Rhett Sutphin
 */
class BeanNameRecordingSerializer<T> implements StudyCalendarXmlSerializer<T> {
    private String beanName;

    BeanNameRecordingSerializer(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }

    public Document createDocument(T root) {
        throw new UnsupportedOperationException("createDocument not implemented");
    }

    public String createDocumentString(T root) {
        throw new UnsupportedOperationException("createDocumentString not implemented");
    }

    public Element createElement(T object) {
        throw new UnsupportedOperationException("createElement not implemented");
    }

    public T readDocument(Document document) {
        throw new UnsupportedOperationException("readDocument not implemented");
    }

    public T readDocument(InputStream in) {
        throw new UnsupportedOperationException("readDocument not implemented");
    }

    public T readElement(Element element) {
        throw new UnsupportedOperationException("readElement not implemented");
    }
}
