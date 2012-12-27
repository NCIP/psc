/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.DateFormat;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.CurrentScheduledActivityStateXmlSerializer;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.Request;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.representation.Variant;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Saurabh Agrawal
 */
public class ScheduledActivityResource extends AbstractDomainObjectResource<ScheduledActivity> {
    private ScheduledActivityDao scheduledActivityDao;
    private CurrentScheduledActivityStateXmlSerializer currentScheduledActivityStateXmlSerializer;

    @Override
    public void doInit() {
        super.doInit();

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
    public Representation post(final Representation representation, Variant variant) throws ResourceException {
        ScheduledActivityState newState = null;
        if (MediaType.TEXT_XML.includes(representation.getMediaType())) {
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
                    try {
                        Date date = getApiDateFormat().parse(dateString);

                        if (!activityState.isNull("time")) {
                            String time = activityState.get("time").toString();
                            try {
                                date = DateFormat.generateDateTime(date, time);
                                newState = newMode.createStateInstance(date, reason, true);
                            } catch (ParseException pe) {
                                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unparseable time:" + time);
                            }
                        } else {
                            newState = newMode.createStateInstance(date, reason);
                        }
                    } catch (ParseException pe) {
                        throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Problem parsing date " + dateString);
                    }
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

        return null;
    }

    ////// CONFIGURATION

    @Required
    public void setScheduledActivityDao(final ScheduledActivityDao scheduledActivityDao) {
        this.scheduledActivityDao = scheduledActivityDao;
    }

    @Required
    public void setCurrentScheduledActivityStateXmlSerializer(final CurrentScheduledActivityStateXmlSerializer currentScheduledActivityStateXmlSerializer) {
        this.currentScheduledActivityStateXmlSerializer = currentScheduledActivityStateXmlSerializer;
    }
}