package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.domain.Registration;
import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import org.acegisecurity.Authentication;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Rhett Sutphin
 * @author John Dzak
 */
public class RegistrationsResource extends StudySiteCollectionResource<Registration> {
    private SubjectService subjectService;
    private SubjectDao subjectDao;
    private StudyCalendarXmlCollectionSerializer<StudySubjectAssignment> assignmentXmlSerializer;


    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAuthorizedFor(Method.GET, Role.SUBJECT_COORDINATOR);
        setAuthorizedFor(Method.POST, Role.SUBJECT_COORDINATOR);
    }

    @Override
    protected Representation createXmlRepresentation(StudySite studySite) throws ResourceException {
        return new StringRepresentation(
            assignmentXmlSerializer.createDocumentString(studySite.getStudySubjectAssignments()), MediaType.TEXT_XML);
    }

    @Override
    protected String acceptValue(Registration value) throws ResourceException {
        if (value.getSubjectCoordinator() == null) {
            Authentication auth = (Authentication) getRequest().getAttributes().get(PscGuard.AUTH_TOKEN_ATTRIBUTE_KEY);
            value.setSubjectCoordinator((User) auth.getPrincipal());
        }
        if(subjectDao.getAssignment(value.getSubject(),getStudy(),getSite())== null) {

        StudySubjectAssignment assigned = subjectService.assignSubject(
            value.getSubject(), getStudySite(), value.getFirstStudySegment(), value.getDate(),
            value.getDesiredStudySubjectAssignmentId(), null, value.getSubjectCoordinator());
        return String.format("studies/%s/schedules/%s",
            Reference.encode(getStudySite().getStudy().getAssignedIdentifier()),
            Reference.encode(assigned.getGridId()));
        }
        else {
            String message = String.format("Subject %s already assigned to the study %s",
                    value.getSubject().getPersonId(), getStudy().getAssignedIdentifier());
            log.error(message);
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    message);
        }
    }

    ////// CONFIGURATION

    @Required
    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @Required
    public void setSubjectDao(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }

    @Required
    public void setAssignmentXmlSerializer(StudyCalendarXmlCollectionSerializer<StudySubjectAssignment> assignmentXmlSerializer) {
        this.assignmentXmlSerializer = assignmentXmlSerializer;
    }
}
