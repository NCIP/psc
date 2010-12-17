package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dao.UserActionDao;
import edu.northwestern.bioinformatics.studycalendar.domain.UserAction;
import gov.nih.nci.logging.api.util.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;

import static gov.nih.nci.logging.api.util.StringUtils.isBlank;

public class UserActionsResource extends AbstractPscResource {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private UserActionDao userActionDao;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.POST);
        setReadable(false);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }

    @Override public boolean allowPost() { return true; }

    @Override
    public void acceptRepresentation(Representation representation) throws ResourceException {
        if (representation.getMediaType().isCompatible(MediaType.APPLICATION_JSON)) {
            UserActionJSONRepresentation json = createJSONRepresentation(representation);

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
                Status.CLIENT_ERROR_BAD_REQUEST, "Unsupported content type: " + representation.getMediaType());
        }
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

    //    private class UserActionPost {
//        private JSONObject json;
//
//        private UserActionPost(JSONObject json) {
//            this.json = json;
//        }
//
//        public UserAction apply() {
//
//        }
//    }
    
    @Required
    public void setUserActionDao(UserActionDao userActionDao) {
        this.userActionDao = userActionDao;
    }
}
