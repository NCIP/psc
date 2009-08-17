package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Notification;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.dao.NotificationDao;

import org.restlet.resource.ResourceException;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.restlet.data.*;
import org.restlet.Context;
import org.restlet.ext.json.JsonRepresentation;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;

/**
 * @author Jalpa Patel
 */
public class NotificationResource extends AbstractStorableDomainObjectResource<Notification> {
    public NotificationDao notificationDao;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
        setAuthorizedFor(Method.PUT, Role.SUBJECT_COORDINATOR);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }
    protected Notification loadRequestedObject(Request request) throws ResourceException {
        String notificationIdentifier = UriTemplateParameters.NOTIFICATION_IDENTIFIER.extractFrom(request);
        if (notificationIdentifier == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No Notification Identifier in request");
        }
        Notification notification = notificationDao.getByGridId(notificationIdentifier);;
        if (notification == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No Notification found for given Id");
        }
        return notification;
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
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
    public void storeRepresentation(Representation representation) throws ResourceException {
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
    }

    @Override
    public void store(Notification notification) throws ResourceException {
        notificationDao.save(notification);
    }

    public void setNotificationDao(NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }
}
