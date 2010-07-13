package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import org.restlet.Context;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.restlet.data.*;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

import java.util.Collection;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.DATA_READER;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_SUBJECT_CALENDAR_MANAGER;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_TEAM_ADMINISTRATOR;

/**
 * @author Jalpa Patel
 */
public class NotificationsResource extends AbstractCollectionResource<Notification> {
    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    private StudyCalendarXmlCollectionSerializer<Notification> xmlSerializer;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAuthorizedFor(Method.GET, Role.SUBJECT_COORDINATOR);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));

        StudySubjectAssignment assignment = getStudySubjectAssignment();
        Study study = null;
        Site site = null;
        if (assignment!= null) {
            StudySite ss = assignment.getStudySite();

            if (ss!=null) {
                study = ss.getStudy();
                site = ss.getSite();
            }
        }

        addAuthorizationsFor(Method.GET, site, study,
                STUDY_SUBJECT_CALENDAR_MANAGER,
                STUDY_TEAM_ADMINISTRATOR,
                DATA_READER);
        
    }

    public Collection<Notification> getAllObjects() throws ResourceException {
        String assignmentIdentifier = UriTemplateParameters.ASSIGNMENT_IDENTIFIER.extractFrom(getRequest());
        if (assignmentIdentifier == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No Assignment Identifier in request");
        }
        StudySubjectAssignment assignment = getStudySubjectAssignment();
        if(assignment == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No Assignment with given Id found");
        }
        return assignment.getNotifications();
    }

    protected StudySubjectAssignment getStudySubjectAssignment() {
        StudySubjectAssignment studySubjectAssignment = null;
        String assignmentIdentifier = UriTemplateParameters.ASSIGNMENT_IDENTIFIER.extractFrom(getRequest());
        if (assignmentIdentifier != null ) {
            return studySubjectAssignmentDao.getByGridId(assignmentIdentifier);
        }
        return studySubjectAssignment;
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (variant.getMediaType().includes(MediaType.TEXT_XML)) {
            return createXmlRepresentation(getAllObjects());
        }
        else if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
            return createJSONRepresentation(getAllObjects());
        }
        return null;
    }

    public Representation createJSONRepresentation(Collection<Notification> notifications) throws ResourceException  {
        try {
            JSONObject jsonData = new JSONObject();
            JSONArray jsonNotifications = new JSONArray();
            for (Notification notification: notifications) {
                jsonNotifications.put(createJSONNotification(notification));
            }
            jsonData.put("notifications", jsonNotifications);
            return new JsonRepresentation(jsonData);
        } catch (JSONException e) {
	        throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
	    }
    }

    public static JSONObject createJSONNotification(Notification notification) throws ResourceException {
        try {
            JSONObject jsonNotification = new JSONObject();
            jsonNotification.put("message", notification.getMessage());
            jsonNotification.put("title", notification.getTitle());
            jsonNotification.put("action-required", notification.isActionRequired());
            jsonNotification.put("dismissed", notification.isDismissed());
            jsonNotification.put("id", notification.getGridId());
            return jsonNotification;
        } catch (JSONException e) {
	        throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
	    }
    }

    public StudyCalendarXmlCollectionSerializer<Notification> getXmlSerializer() {
        return xmlSerializer;
    }

    public void setXmlSerializer(StudyCalendarXmlCollectionSerializer<Notification> xmlSerializer) {
        this.xmlSerializer = xmlSerializer;
    }

    public void setStudySubjectAssignmentDao(StudySubjectAssignmentDao studySubjectAssignmentDao) {
        this.studySubjectAssignmentDao = studySubjectAssignmentDao;
    }

}
