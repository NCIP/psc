package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.SUBJECT;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.SUBJECT_ASSIGNMENT;
import org.dom4j.Element;

/**
 * @author John Dzak
 */
public class StudySubjectAssignmentXmlSerializer extends AbstractStudyCalendarXmlCollectionSerializer<StudySubjectAssignment> {
    protected XsdElement rootElement() { return XsdElement.SUBJECT_ASSIGNMENT; }
    protected XsdElement collectionRootElement() { return XsdElement.SUBJECT_ASSIGNMENTS; }

    public Element createElement(StudySubjectAssignment assignment, boolean inCollection) {

        Element subjElt = SUBJECT.create();
        SUBJECT_FIRST_NM.addTo(subjElt, assignment.getSubject().getFirstName());
        SUBJECT_LAST_NM.addTo(subjElt, assignment.getSubject().getLastName());
        SUBJECT_PERSON_ID.addTo(subjElt, assignment.getSubject().getPersonId());
        SUBJECT_DATE_OF_BIRTH.addTo(subjElt, assignment.getSubject().getDateOfBirth());

        Element elt = SUBJECT_ASSIGNMENT.create();
        ASSIGNMENT_STUDY_NAME.addTo(elt,  assignment.getStudySite().getStudy().getName());
        ASSIGNMENT_SITE_NAME.addTo(elt,  assignment.getStudySite().getSite().getName());
        ASSIGNMENT_START_DATE.addTo(elt,  assignment.getStartDateEpoch());
        ASSIGNMENT_END_DATE.addTo(elt,  assignment.getEndDateEpoch());
        ASSIGNMENT_SUBJECT_COORD.addTo(elt,  assignment.getSubjectCoordinator().getName());
        ASSIGNMENT_CURRENT_AMENDMENT.addTo(elt, assignment.getCurrentAmendment().getNaturalKey());

        elt.add(subjElt);

        return elt;
    }

    public StudySubjectAssignment readElement(Element elt) {
        throw new UnsupportedOperationException("Reading Study Assignment elements not allowed");
    }
}