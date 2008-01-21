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
public abstract class AbstractStudyCalendarXmlSerializer<R> extends BaseAbstractStudyCalendarXmlSerializer implements StudyCalendarXmlSerializer<R> {
    public static final String XML_NS = StudyXMLWriter.XML_NS;
    public static final String XSI_NS = StudyXMLWriter.XSI_NS;
    public static final String SCHEMA_LOCATION  = StudyXMLWriter.SCHEMA_LOCATION;

    public static final String SCHEMA_LOCATION_ATTRIBUTE  = "schemaLocation";
    public static final String XML_SCHEMA_ATTRIBUTE       = "xsi";

    // Attributes
    public static final String ID = "id";
    public static final String NAME = "name";



    public Document createDocument(R root) {
        Document document = DocumentHelper.createDocument();
        Element element = createElement(root);
        
        element.add(DEFAULT_NAMESPACE);
        element.addNamespace(XML_SCHEMA_ATTRIBUTE, XSI_NS)
                .addAttribute("xsi:"+ SCHEMA_LOCATION_ATTRIBUTE, BaseAbstractStudyCalendarXmlSerializer.PSC_NS + ' ' + SCHEMA_LOCATION);

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
