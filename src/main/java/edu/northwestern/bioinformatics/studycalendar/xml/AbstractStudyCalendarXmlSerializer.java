package edu.northwestern.bioinformatics.studycalendar.xml;


import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter;
import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.io.Reader;

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

    public static final Namespace DEFAULT_NAMESPACE = DocumentHelper.createNamespace("", PSC_NS);

    public Document createDocument(R root) {
        Document document = DocumentHelper.createDocument();
        Element element = createElement(root);
        
        element.add(DEFAULT_NAMESPACE);
        element.addNamespace(XML_SCHEMA_ATTRIBUTE, XSI_NS)
                .addAttribute("xsi:"+SCHEMA_LOCATION_ATTRIBUTE, PSC_NS + ' ' + SCHEMA_LOCATION);

        document.add(element);
        
        return document;
    }

    public String createDocumentString(R root) {
        return createDocument(root).asXML();
    }

    public R readDocument(Document document) {
        return readElement(document.getRootElement());
    }

    public R readDocument(Reader reader) {
        Document document;
        try {
            SAXReader saxReader = new SAXReader();
            document = saxReader.read(reader);
        } catch(DocumentException de) {
            throw new StudyCalendarSystemException("Could not read the XML for deserialization", de);
        }

        return readElement(document.getRootElement());
    }

    public abstract Element createElement(R object);

    public abstract R readElement(Element element);
}
