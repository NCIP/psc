package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.SUBJECT_ASSIGNMENT;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author John Dzak
 */
public class StudySubjectAssignmentXmlSerializer extends AbstractStudyCalendarXmlCollectionSerializer<StudySubjectAssignment> {
    private AbstractStudyCalendarXmlSerializer<Subject> subjectXmlSerializer;

    protected XsdElement rootElement() { return XsdElement.SUBJECT_ASSIGNMENT; }
    protected XsdElement collectionRootElement() { return XsdElement.SUBJECT_ASSIGNMENTS; }

    @Override
    public Element createElement(StudySubjectAssignment assignment, boolean inCollection) {
        Element elt = SUBJECT_ASSIGNMENT.create();
        ASSIGNMENT_STUDY_NAME.addTo(elt,  assignment.getStudySite().getStudy().getName());
        ASSIGNMENT_SITE_NAME.addTo(elt,  assignment.getStudySite().getSite().getName());
        ASSIGNMENT_START_DATE.addTo(elt,  assignment.getStartDateEpoch());
        ASSIGNMENT_END_DATE.addTo(elt,  assignment.getEndDateEpoch());
        ASSIGNMENT_SUBJECT_COORD.addTo(elt,  assignment.getSubjectCoordinator().getName());
        ASSIGNMENT_CURRENT_AMENDMENT.addTo(elt, assignment.getCurrentAmendment().getNaturalKey());

        elt.add(subjectXmlSerializer.createElement(assignment.getSubject()));

        return elt;
    }

    @Override
    public StudySubjectAssignment readElement(Element elt) {
        throw new UnsupportedOperationException("Reading Study Assignment elements not allowed");
    }

    ////// Bean Setters
    @Required
    public void setSubjectXmlSerializer(AbstractStudyCalendarXmlSerializer<Subject> subjectXmlSerializer) {
        this.subjectXmlSerializer = subjectXmlSerializer;
    }
}