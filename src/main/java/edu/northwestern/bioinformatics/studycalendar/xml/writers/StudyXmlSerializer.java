package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static org.apache.commons.lang.StringUtils.EMPTY;
import org.dom4j.Document;
import org.dom4j.Element;

public class StudyXmlSerializer extends AbstractStudyCalendarXmlSerializer<Study>
{
    public static final String XML_NS = StudyXMLWriter.XML_NS;
    public static final String XSI_NS = StudyXMLWriter.XSI_NS;
    public static final String PSC_NS = StudyXMLWriter.PSC_NS;
    public static final String SCHEMA_LOCATION  = StudyXMLWriter.SCHEMA_LOCATION;

    public static final String SCHEMA_LOCATION_ATTRIBUTE  = "schemaLocation";
    public static final String XML_SCHEMA_ATTRIBUTE       = "xsi";

    public static final String STUDY = "study";
    public static final String ASSIGNED_IDENTIFIER = "assigned-identifier";
    public static final String ID = "id";

    public Document createDocument(Study study) {
        Document document = createDocumentInstance();
        Element element = createElement(document, study);
        element.addNamespace(EMPTY, PSC_NS);
        element.addNamespace(XML_SCHEMA_ATTRIBUTE, XSI_NS);
        element.addNamespace(SCHEMA_LOCATION_ATTRIBUTE, PSC_NS + ' ' + SCHEMA_LOCATION);
        return document;
    }

    public Element createElement(Study study) {
        Document document = createDocumentInstance();
        return createElement(document, study);
    }

    public Study readElement(Element element) {
        Study study = new Study();
        study.setGridId(element.attributeValue(ID));
        study.setAssignedIdentifier(element.attributeValue(ASSIGNED_IDENTIFIER));
        return study;
    }

    // Create Element without namespace
    protected Element createElement(Document document, Study study) {
        return document.addElement(STUDY)
                .addAttribute(ID, study.getGridId())
                .addAttribute(ASSIGNED_IDENTIFIER, study.getAssignedIdentifier());
    }
}
