/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Required;

import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.SUBJECT_ASSIGNMENT;

/**
 * @author John Dzak
 */
public class StudySubjectAssignmentXmlSerializer extends AbstractStudyCalendarXmlCollectionSerializer<StudySubjectAssignment> {
    private AbstractStudyCalendarXmlSerializer<Subject> subjectXmlSerializer;
    private ScheduledCalendarXmlSerializer scheduledCalendarXmlSerializer;
    private Boolean subjectCentric = false;
    private Boolean includeScheduledCalendar = false;
    protected XsdElement rootElement() { return XsdElement.SUBJECT_ASSIGNMENT; }
    protected XsdElement collectionRootElement() { return XsdElement.SUBJECT_ASSIGNMENTS; }

    @Override
    public Element createElement(StudySubjectAssignment assignment, boolean inCollection) {
        Element elt = SUBJECT_ASSIGNMENT.create();
        ASSIGNMENT_STUDY_NAME.addTo(elt,  assignment.getStudySite().getStudy().getName());
        ASSIGNMENT_SITE_NAME.addTo(elt,  assignment.getStudySite().getSite().getName());
        ASSIGNMENT_START_DATE.addTo(elt,  assignment.getStartDate());
        ASSIGNMENT_END_DATE.addTo(elt,  assignment.getEndDate());
        ASSIGNMENT_SUBJECT_COORD.addTo(elt,  assignment.getStudySubjectCalendarManager().getName());
        ASSIGNMENT_CURRENT_AMENDMENT.addTo(elt, assignment.getCurrentAmendment().getNaturalKey());
        ASSIGNMENT_ID.addTo(elt, assignment.getGridId());
        if (!subjectCentric) {
            elt.add(subjectXmlSerializer.createElement(assignment.getSubject()));
        }
        if (subjectCentric || includeScheduledCalendar) {
            elt.add(scheduledCalendarXmlSerializer.createElement(assignment.getScheduledCalendar()));
        }
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

    public void setSubjectCentric(Boolean subjectCentric) {
        this.subjectCentric = subjectCentric;
    }

    public void setIncludeScheduledCalendar(Boolean includeScheduledCalendar) {
        this.includeScheduledCalendar = includeScheduledCalendar;
    }

    public void setScheduledCalendarXmlSerializer(ScheduledCalendarXmlSerializer scheduledCalendarXmlSerializer) {
        this.scheduledCalendarXmlSerializer = scheduledCalendarXmlSerializer;
    }
}