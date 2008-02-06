package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.domain.Registration;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Rhett Sutphin
 */
public class RegistrationsResource extends StudySiteCollectionResource<Registration> {
    private SubjectService subjectService;
    private StudyCalendarXmlCollectionSerializer<StudySubjectAssignment> assignmentXmlSerializer;

    @Override
    protected Representation createXmlRepresentation(StudySite studySite) throws ResourceException {
        return new StringRepresentation(
            assignmentXmlSerializer.createDocumentString(studySite.getStudySubjectAssignments()), MediaType.TEXT_XML);
    }

    @Override
    protected String acceptValue(Registration value) throws ResourceException {
        StudySubjectAssignment assigned = subjectService.assignSubject(
            value.getSubject(), getStudySite(), value.getFirstStudySegment(), value.getDate(),
            value.getDesiredStudySubjectAssignmentId(), value.getSubjectCoordinator());
        return String.format("studies/%s/schedules/%s",
            Reference.encode(getStudySite().getStudy().getAssignedIdentifier()),
            Reference.encode(assigned.getGridId()));
    }

    ////// CONFIGURATION

    @Required
    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @Required
    public void setAssignmentXmlSerializer(StudyCalendarXmlCollectionSerializer<StudySubjectAssignment> assignmentXmlSerializer) {
        this.assignmentXmlSerializer = assignmentXmlSerializer;
    }
}
