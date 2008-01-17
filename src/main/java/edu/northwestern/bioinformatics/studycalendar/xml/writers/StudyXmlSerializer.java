package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;

public class StudyXmlSerializer extends AbstractStudyCalendarXmlSerializer<Study> {
    // Elements
    public static final String STUDY = "study";
    public static final String PLANNDED_CALENDAR = "planned-calendar";

    // Attributes
    public static final String ASSIGNED_IDENTIFIER = "assigned-identifier";
    public static final String ID = "id";

    private StudyDao studyDao;

    public Element createElement(Study study) {
        // Using QName is the only way to attach the namespace to the element
        QName qStudy = DocumentHelper.createQName(STUDY, DEFAULT_NAMESPACE);
        Element eStudy = DocumentHelper.createElement(qStudy)
                .addAttribute(ASSIGNED_IDENTIFIER, study.getAssignedIdentifier());

        QName qCalendar = DocumentHelper.createQName(PLANNDED_CALENDAR, DEFAULT_NAMESPACE);
        Element eCalendar = DocumentHelper.createElement(qCalendar)
                .addAttribute(ID, study.getPlannedCalendar().getGridId());

        eStudy.add(eCalendar);
                
        return eStudy;
    }

    public Study readElement(Element element) {
        String key = element.attributeValue(ASSIGNED_IDENTIFIER);
        Study study = studyDao.getByAssignedIdentifier(key);
        if (study == null) {
            study = new Study();
            study.setAssignedIdentifier(key);
            study.setPlannedCalendar(new PlannedCalendar());
            Element calendar = (Element) element.elements(PLANNDED_CALENDAR).get(0);
            study.getPlannedCalendar().setGridId(calendar.attributeValue(ID));
        }
        return study;
    }

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
}
