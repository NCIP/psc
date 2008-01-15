package edu.northwestern.bioinformatics.studycalendar.xml;


import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import static org.apache.commons.lang.StringUtils.EMPTY;

import java.io.Reader;

import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter;

/**
 * @author Rhett Sutphin
 * @author John Dzak
 */
public abstract class AbstractStudyCalendarXmlSerializer<R> implements StudyCalendarXmlSerializer<R> {
    public static final String XML_NS = StudyXMLWriter.XML_NS;
    public static final String XSI_NS = StudyXMLWriter.XSI_NS;
    public static final String PSC_NS = StudyXMLWriter.PSC_NS;
    public static final String SCHEMA_LOCATION  = StudyXMLWriter.SCHEMA_LOCATION;

    public static final String SCHEMA_LOCATION_ATTRIBUTE  = "schemaLocation";
    public static final String XML_SCHEMA_ATTRIBUTE       = "xsi";

    public Document createDocument(R root) {
        Document document = DocumentHelper.createDocument();
        Element element = createElement(root);

        element.addNamespace(EMPTY, PSC_NS)
                .addNamespace(XML_SCHEMA_ATTRIBUTE, XSI_NS)
                .addNamespace(SCHEMA_LOCATION_ATTRIBUTE, PSC_NS + ' ' + SCHEMA_LOCATION);

        document.add(element);
        
        return document;
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
