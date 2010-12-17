package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dao.UserActionDao;
import edu.northwestern.bioinformatics.studycalendar.domain.UserAction;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;

import static gov.nih.nci.logging.api.util.StringUtils.isBlank;

public class UserActionsResource extends AbstractPscResource {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private UserActionDao userActionDao;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        setAllAuthorizedFor(Method.POST);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }

    @Override
    protected Representation post(Representation entity, Variant variant) throws ResourceException {
        if (entity.getMediaType().isCompatible(MediaType.APPLICATION_JSON)) {
            UserActionJSONRepresentation json = createJSONRepresentation(entity);

            String errorMessage = json.validate();
            if (errorMessage != null) {
                throw new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST, errorMessage);
            }

            UserAction action = json.createUserAction();

            userActionDao.save(action);

            getResponse().setStatus(Status.SUCCESS_CREATED);
            getResponse().setLocationRef(String.format(
                "user-actions/%s", Reference.encode(action.getGridId())));
        } else {
            throw new ResourceException(
                Status.CLIENT_ERROR_BAD_REQUEST, "Unsupported content type: " + entity.getMediaType());
        }
        return null;
    }

    private UserActionJSONRepresentation createJSONRepresentation(Representation representation) throws ResourceException {
        try {
            return new UserActionJSONRepresentation(representation.getText());
        } catch (JSONException e) {
            throw new StudyCalendarSystemException("Problem reading JSON", e);
        } catch (IOException e) {
            throw new StudyCalendarSystemException("Problem reading JSON", e);
        }
    }

    private class UserActionJSONRepresentation {
        private JSONObject wrapper;

        private UserActionJSONRepresentation(String json) throws JSONException {
            this.wrapper = new JSONObject(json);
        }

        public UserAction createUserAction() {
            UserAction action = new UserAction();
            try {
                action.setContext(wrapper.getString("context"));
                action.setActionType(wrapper.getString("actionType"));
                action.setDescription(wrapper.getString("description"));
                action.setUser(getCurrentUser().getCsmUser());
            } catch (JSONException e) {
                throw new StudyCalendarSystemException("Problem reading JSON", e);
            }
            return action;
        }

        private String[] REQUIRED_KEYS = {"description", "context", "actionType"};
        public String validate() {
            for (String key : REQUIRED_KEYS) {
                try {
                    if (!wrapper.has(key)) {
                        return "Missing attribute: " + key;
                    } else if (wrapper.isNull(key) || isBlank(wrapper.getString(key))) {
                        return "Blank attribute: " + key;
                    }
                } catch (JSONException e) {
                    return "Missing attribute: " + key;
                }
            }
            return null;
        }
    }
    
    @Required
    public void setUserActionDao(UserActionDao userActionDao) {
        this.userActionDao = userActionDao;
    }
}
