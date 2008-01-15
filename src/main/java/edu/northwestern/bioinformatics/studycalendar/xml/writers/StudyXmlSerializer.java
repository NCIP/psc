package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

public class StudyXmlSerializer extends AbstractStudyCalendarXmlSerializer<Study> {
    public static final String STUDY = "study";
    public static final String ASSIGNED_IDENTIFIER = "assigned-identifier";
    public static final String ID = "id";

    public Element createElement(Study study) {
        Element element = DocumentFactory.getInstance().createElement(STUDY);

        element.addAttribute(ID, study.getGridId())
               .addAttribute(ASSIGNED_IDENTIFIER, study.getAssignedIdentifier());

        return element;
    }

    public Study readElement(Element element) {
        Study study = new Study();
        study.setGridId(element.attributeValue(ID));
        study.setAssignedIdentifier(element.attributeValue(ASSIGNED_IDENTIFIER));
        return study;
    }
}
