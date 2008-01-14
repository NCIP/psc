package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.data.Reference;
import org.restlet.data.MediaType;
import org.springframework.beans.factory.annotation.Required;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.xml.domain.Registration;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Rhett Sutphin
 */
public class RegistrationsResource extends StudySiteCollectionResource<Registration> {
    private SubjectService subjectService;

    protected Representation createXmlRepresentation(StudySite studySite) throws ResourceException {
        List<Registration> registrations = new ArrayList<Registration>(studySite.getStudySubjectAssignments().size());
        for (StudySubjectAssignment assignment : studySite.getStudySubjectAssignments()) {
            registrations.add(Registration.create(assignment));
        }
        return new StringRepresentation(
            studyCalendarXmlFactory.createDocumentString(registrations, null), MediaType.TEXT_XML);
    }

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
}
