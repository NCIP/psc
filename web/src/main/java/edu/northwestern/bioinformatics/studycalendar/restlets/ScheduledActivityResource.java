package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.CurrentScheduledActivityStateXmlSerializer;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.DATA_READER;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_SUBJECT_CALENDAR_MANAGER;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_TEAM_ADMINISTRATOR;

/**
 * @author Saurabh Agrawal
 */
public class ScheduledActivityResource extends AbstractDomainObjectResource<ScheduledActivity> {
    private ScheduledActivityDao scheduledActivityDao;
    private ScheduleService scheduleService;
    private CurrentScheduledActivityStateXmlSerializer currentScheduledActivityStateXmlSerializer;

    @Override public boolean allowPost() { return true; }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAuthorizedFor(Method.GET, Role.SUBJECT_COORDINATOR);
        setAuthorizedFor(Method.POST, Role.SUBJECT_COORDINATOR);

        Study study = null;
        Site site = null;
        ScheduledActivity sa = getRequestedObjectDuringInit();

        if (sa != null) {
            StudySite ss = sa.getScheduledStudySegment().getScheduledCalendar().getAssignment().getStudySite();
            study = ss.getStudy();
            site = ss.getSite();
        }
        addAuthorizationsFor(Method.GET, site, study,
                STUDY_SUBJECT_CALENDAR_MANAGER,
                STUDY_TEAM_ADMINISTRATOR,
                DATA_READER);

        addAuthorizationsFor(Method.POST, site, study, STUDY_SUBJECT_CALENDAR_MANAGER);
        
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
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
     * @param representation
     * @throws ResourceException
     */
    @Override
    public void acceptRepresentation(final Representation representation) throws ResourceException {
        ScheduledActivityState newState = null;
        if (representation.getMediaType() == MediaType.TEXT_XML) {
            try {
                newState = currentScheduledActivityStateXmlSerializer.readDocument(representation.getStream());
            } catch (IOException e) {
                log.warn("POST failed with IOException", e);
                throw new ResourceException(e);
            } catch (StudyCalendarValidationException exp) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, exp.getMessage());
            }
        } else if (representation.getMediaType().isCompatible(MediaType.APPLICATION_JSON)) {
            // TODO: this code is shared with the batch update resource
            try {
                JSONObject entity = new JSONObject(representation.getText());
                JSONObject activityState = (JSONObject)(entity.get(getRequestedObject().getGridId()));
                String state = activityState.get("state").toString();
                String reason = activityState.get("reason").toString();
                String dateString = activityState.get("date").toString();
                ScheduledActivityMode newMode = ScheduledActivityMode.getByName(state);
                if (newMode != null) {
                    Date date;
                    try {
                        date = getApiDateFormat().parse(dateString);
                    } catch (ParseException pe) {
                        throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Problem parsing date " + dateString);
                    }
                    newState = newMode.createStateInstance(date, reason);
                }
            } catch (JSONException e) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unparseable entity", e);
            } catch (IOException e) {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not read entity", e);
            }
        } else {
            throw new ResourceException(
                Status.CLIENT_ERROR_BAD_REQUEST, "Unsupported content type: " + representation.getMediaType());
        }

        if (newState == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not parse request entity");
        } else {
            getRequestedObject().changeState(newState);
            scheduledActivityDao.save(getRequestedObject());
            getResponse().setStatus(Status.SUCCESS_CREATED);
            getResponse().setLocationRef(getRequest().getOriginalRef());
        }
    }

    ////// CONFIGURATION

    @Required
    public void setStudySubjectAssignmentDao(final StudySubjectAssignmentDao studySubjectAssignmentDao) {
        StudySubjectAssignmentDao studySubjectAssignmentDao1 = studySubjectAssignmentDao;
    }

    @Required
    public void setScheduledActivityDao(final ScheduledActivityDao scheduledActivityDao) {
        this.scheduledActivityDao = scheduledActivityDao;
    }

    @Required
    public void setScheduleService(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @Required
    public void setCurrentScheduledActivityStateXmlSerializer(final CurrentScheduledActivityStateXmlSerializer currentScheduledActivityStateXmlSerializer) {
        this.currentScheduledActivityStateXmlSerializer = currentScheduledActivityStateXmlSerializer;
    }
}