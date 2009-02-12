package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.CurrentScheduledActivityStateXmlSerializer;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;

/**
 * @author Saurabh Agrawal
 */
public class ScheduledActivityResource extends AbstractDomainObjectResource<ScheduledActivity> {
    private ScheduledActivityDao scheduledActivityDao;
    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    private CurrentScheduledActivityStateXmlSerializer currentScheduledActivityStateXmlSerializer;

    private String badRequestBecause;

    @Override public boolean allowPost() { return true; }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAuthorizedFor(Method.GET, Role.SUBJECT_COORDINATOR);
        setAuthorizedFor(Method.POST, Role.SUBJECT_COORDINATOR);
    }

    @Override
    protected ScheduledActivity loadRequestedObject(Request request) {
        String scheduledActivityIdentifier = UriTemplateParameters.SCHEDULED_ACTIVITY_IDENTIFIER.extractFrom(request);
        String assignmentIdentifer = UriTemplateParameters.ASSIGNMENT_IDENTIFIER.extractFrom(request);
        String studyIdentifier = UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(request);

        ScheduledActivity scheduledActivity = scheduledActivityDao.getByGridId(scheduledActivityIdentifier);
        if (scheduledActivity == null) return null;

        StudySubjectAssignment assignment = scheduledActivity.getScheduledStudySegment().getScheduledCalendar().getAssignment();
        if (!assignment.getGridId().equals(assignmentIdentifer)) {
            log.debug("The requested schedule ({}) is not related to the requested scheduled activity ({})",
                assignment, scheduledActivity);
            return null;
        }

        Study study = assignment.getStudySite().getStudy();
        if (!study.getAssignedIdentifier().equals(studyIdentifier)) {
            log.debug("The requested schedule ({}) is not related to the requested study ({})",
                assignment, study);
            return null;
        }

        return scheduledActivity;
    }

    /**
     * Accepts a new scheduled activity state (date, status, and reason) to update the scheduled activity.
     *
     * @param entity
     * @throws ResourceException
     */
    @Override
    public void acceptRepresentation(final Representation entity) throws ResourceException {
        if (entity.getMediaType() == MediaType.TEXT_XML) {
            final ScheduledActivityState scheduledActivityState;
            try {
                scheduledActivityState = currentScheduledActivityStateXmlSerializer.readDocument(entity.getStream());
            } catch (IOException e) {
                log.warn("POST failed with IOException", e);
                throw new ResourceException(e);
            } catch (StudyCalendarValidationException exp) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, exp.getMessage());
            }

            if (scheduledActivityState == null) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not parse request entity");
            } else {
                getRequestedObject().changeState(scheduledActivityState);
                scheduledActivityDao.save(getRequestedObject());
                getResponse().setStatus(Status.SUCCESS_CREATED);
                getResponse().setLocationRef(getRequest().getOriginalRef());
            }
        } else {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unsupported content type");
        }
    }

    ////// CONFIGURATION

    @Required
    public void setStudySubjectAssignmentDao(final StudySubjectAssignmentDao studySubjectAssignmentDao) {
        this.studySubjectAssignmentDao = studySubjectAssignmentDao;
    }

    @Required
    public void setScheduledActivityDao(final ScheduledActivityDao scheduledActivityDao) {
        this.scheduledActivityDao = scheduledActivityDao;
    }

    @Required
    public void setCurrentScheduledActivityStateXmlSerializer(final CurrentScheduledActivityStateXmlSerializer currentScheduledActivityStateXmlSerializer) {
        this.currentScheduledActivityStateXmlSerializer = currentScheduledActivityStateXmlSerializer;
    }
}