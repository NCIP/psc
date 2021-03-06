/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarAuthorizationException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.service.RegistrationService;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.Registration;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.representation.StringRepresentation;
import org.springframework.beans.factory.annotation.Required;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Rhett Sutphin
 * @author John Dzak
 */
public class RegistrationsResource extends StudySiteCollectionResource<Registration> {
    private SubjectService subjectService;
    private RegistrationService registrationService;
    private StudyCalendarXmlCollectionSerializer<StudySubjectAssignment> assignmentXmlSerializer;

    @Override
    public void doInit() {
        super.doInit();
        addAuthorizationsFor(Method.GET, getSite(), getStudy(),
                STUDY_TEAM_ADMINISTRATOR,
                STUDY_SUBJECT_CALENDAR_MANAGER,
                DATA_READER);
        addAuthorizationsFor(Method.POST, getSite(), getStudy(), STUDY_SUBJECT_CALENDAR_MANAGER);
    }

    @Override
    protected Representation createXmlRepresentation(StudySite studySite) throws ResourceException {
        return new StringRepresentation(
            assignmentXmlSerializer.createDocumentString(studySite.getStudySubjectAssignments()), MediaType.TEXT_XML);
    }

    @Override
    protected String acceptValue(Registration value) throws ResourceException {
        try {
            if (value.getStudySubjectCalendarManager() == null) {
                value.setStudySubjectCalendarManager(getCurrentUser());
            }
            registrationService.resolveRegistration(value, getStudySite());
            StudySubjectAssignment assigned = subjectService.assignSubject(getStudySite(), value);
            return String.format("studies/%s/schedules/%s",
                Reference.encode(getStudySite().getStudy().getAssignedIdentifier()),
                Reference.encode(assigned.getGridId()));
        } catch (StudyCalendarValidationException scve) {
            throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, scve.getMessage(), scve);
        } catch (StudyCalendarAuthorizationException scae) {
            throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, scae.getMessage(), scae);
        }
    }

    ////// CONFIGURATION

    @Required
    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @Required
    public void setRegistrationService(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @Required
    public void setAssignmentXmlSerializer(StudyCalendarXmlCollectionSerializer<StudySubjectAssignment> assignmentXmlSerializer) {
        this.assignmentXmlSerializer = assignmentXmlSerializer;
    }
}
