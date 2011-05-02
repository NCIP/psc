package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.NotificationDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Notification;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.Request;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.representation.Variant;

import java.io.IOException;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Jalpa Patel
 */
public class NotificationResource extends AbstractStorableDomainObjectResource<Notification> {
    public NotificationDao notificationDao;

    @Override
    public void doInit() {
        super.doInit();

        Study study = null;
        Site site = null;

        Notification notification = getRequestedObjectDuringInit();
        if (notification != null) {
            StudySubjectAssignment assignment = notification.getAssignment();

            if (assignment!= null) {
                StudySite ss = assignment.getStudySite();

                if (ss!=null) {
                    study = ss.getStudy();
                    site = ss.getSite();
                }
            }
        }
        addAuthorizationsFor(Method.GET, site, study,
                STUDY_SUBJECT_CALENDAR_MANAGER,
                STUDY_TEAM_ADMINISTRATOR,
                DATA_READER);

        addAuthorizationsFor(Method.PUT, site, study, STUDY_SUBJECT_CALENDAR_MANAGER);

        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }

    //todo need to check that notification_identifier corresponds to assignment_identifier
    @Override
    protected Notification loadRequestedObject(Request request) throws ResourceException {
        String notificationIdentifier = UriTemplateParameters.NOTIFICATION_IDENTIFIER.extractFrom(request);
        if (notificationIdentifier == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No Notification Identifier in request");
        }
        Notification notification = notificationDao.getByGridId(notificationIdentifier);
        if (notification == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No Notification found for given Id");
        }
        return notification;
    }

     @Override
    public Representation get(Variant variant) throws ResourceException {
        if (variant.getMediaType() == MediaType.TEXT_XML) {
            return createXmlRepresentation(getRequestedObject());
        } else if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
            return createJSONRepresentation(getRequestedObject());
        } else {
            return null;
        }
    }

    public Representation createJSONRepresentation(Notification notification) throws ResourceException  {
        JSONObject jsonData = NotificationsResource.createJSONNotification(notification);
        return new JsonRepresentation(jsonData);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Representation put(Representation representation, Variant variant) throws ResourceException {
        if (representation.getMediaType().isCompatible(MediaType.APPLICATION_JSON)) {
            try {
                JSONObject entity = new JSONObject(representation.getText());
                Notification notification = getRequestedObject();
                Boolean dismissed = (Boolean)entity.get("dismissed");
                notification.setDismissed(dismissed);
                store(notification);
            } catch (JSONException e) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unparseable entity", e);
            } catch (IOException e) {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not read entity", e);
            }
        } else {
            throw new ResourceException(
                Status.CLIENT_ERROR_BAD_REQUEST, "Unsupported content type: " + representation.getMediaType());
        }

        return null;
    }

    @Override
    public Notification store(Notification notification) throws ResourceException {
        notificationDao.save(notification);
        return notification;
    }

    public void setNotificationDao(NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }
}
