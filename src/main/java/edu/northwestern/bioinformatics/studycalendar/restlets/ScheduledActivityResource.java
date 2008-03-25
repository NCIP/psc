package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.CurrentScheduledActivityStateXmlSerializer;
import org.apache.commons.lang.StringUtils;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.util.Collection;

/**
 * @author Saurabh Agrawal
 */
public class ScheduledActivityResource extends AbstractDomainObjectResource<ScheduledActivity> {

    private ScheduledActivityDao scheduledActivityDao;

    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    private ScheduledCalendar scheduledCalendar;

    private CurrentScheduledActivityStateXmlSerializer currentScheduledActivityStateXmlSerializer;

    @Override
    public boolean allowPost() {
        return true;
    }


    @Override
    public boolean allowPut() {
        return false;
    }


    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
        setAuthorizedFor(Method.GET, Role.SUBJECT_COORDINATOR);

    }


    @Override
    protected ScheduledActivity loadRequestedObject(Request request) {
        String scheduledActivityIdentifier = UriTemplateParameters.SCHEDULED_ACTIVITY_IDENTIFIER.extractFrom(request);

        String assignmentId = UriTemplateParameters.ASSIGNMENT_IDENTIFIER.extractFrom(request);
        StudySubjectAssignment assignment = studySubjectAssignmentDao.getByGridId(assignmentId);
        if (assignment != null) {
            scheduledCalendar = assignment.getScheduledCalendar();


        }
        String studyIdentifier = UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(request);


        if (scheduledCalendar == null || !StringUtils.isNotEmpty(scheduledActivityIdentifier)
                || !StringUtils.isNumeric(scheduledActivityIdentifier)) {
            String message = "The scheduled calendar is null:" + scheduledCalendar + " or scheduled activity identifier is not numeric:" + scheduledActivityIdentifier;
            log.error(message);

            return null;
        } else if (scheduledCalendar.getAssignment().getStudyId() == null
                || !scheduledCalendar.getAssignment().getStudyId().equals(studyIdentifier)) {
            String message = "The assignment is associated to different study (id =" + scheduledCalendar.getAssignment().getStudyId() + "). " +
                    "Assignment must be associated with the same study which is provided in url parameter:" + studyIdentifier;
            log.error(message);
            return null;
//            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
//                    message);


        }

        Collection<ScheduledActivity> scheduledActivityCollection = scheduledActivityDao.getEventsByDate(scheduledCalendar, null, null);
        log.info("size of scheduled activity collection:" + scheduledActivityCollection.size());

        for (ScheduledActivity scheduledActivity : scheduledActivityCollection) {
            if (scheduledActivity.getId().equals(Integer.parseInt(scheduledActivityIdentifier))) {
                return scheduledActivity;

            }
        }
        log.error("no scheduled activity exists for given scheduled calendar:" + scheduledCalendar.getGridId());

        return null;
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
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not parse request");
            } else {
                final String target = store(scheduledActivityState);
                getResponse().redirectSeeOther(
                        new Reference(
                                new Reference(getRequest().getRootRef().toString() + '/'), target));
            }

        } else {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unsupported content type");
        }
    }

    public String store(final ScheduledActivityState scheduledActivityState) {

        // update the scheduled activity with new scheduled activity state (date, status, and reason).
        ScheduledActivity requestedScheduledActivity = getRequestedObject();
        requestedScheduledActivity.changeState(scheduledActivityState);
        scheduledActivityDao.save(requestedScheduledActivity);

        return String.format("studies/%s/schedules/%s/activities/%s",
                scheduledCalendar.getAssignment().getStudyId(), scheduledCalendar.getAssignment().getGridId(),
                scheduledActivityState.getId(), scheduledActivityState.getId());

    }

    

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