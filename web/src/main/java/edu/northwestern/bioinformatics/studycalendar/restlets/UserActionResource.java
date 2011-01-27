package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.UserActionDao;
import edu.northwestern.bioinformatics.studycalendar.domain.UserAction;
import edu.northwestern.bioinformatics.studycalendar.service.UserActionService;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_SUBJECT_CALENDAR_MANAGER;

/**
 * @author Jalpa Patel
 */
public class UserActionResource  extends AbstractPscResource {
    private UserActionDao userActionDao;
    private UserActionService userActionService;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        addAuthorizationsFor(Method.DELETE, STUDY_SUBJECT_CALENDAR_MANAGER);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }

    @Override
    protected Representation delete(Variant variant) throws ResourceException {
        UserAction userAction = getRequestedUserAction();
        verifyUndoable(userAction);
        userActionService.applyUndo(userAction);
        getResponse().setStatus(Status.SUCCESS_OK);
        return null;
    }

    private void verifyUndoable(UserAction ua) {
        if ( !getCurrentUser().getName().equals(ua.getUser().getName())) {
            throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN,
                    "Does not have proper credentials to undo the user action "+ ua.getGridId());
        } else if (ua.isUndone()) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "Already undone user action "+ ua.getGridId());
        }

        List<UserAction> undoableActions = userActionService.getUndoableActions(ua.getContext());
        if (undoableActions.isEmpty()) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "No undoable user action "+ ua.getGridId());
        } else if ( !ua.equals(undoableActions.get(0))) {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "Requested user action is out of order in undoable list");
        }
    }

    private UserAction getRequestedUserAction() throws ResourceException {
        String userActionId = UriTemplateParameters.USER_ACTION_IDENTIFIER.extractFrom(getRequest());
        if (userActionId == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,"No user action identifier in request");
        }
        UserAction userAction = userActionDao.getByGridId(userActionId);
        if (userAction == null) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,"User action doesn't exist with id " + userActionId);

        }
        return userAction;
    }

    @Required
    public void setUserActionDao(UserActionDao userActionDao) {
        this.userActionDao = userActionDao;
    }

    @Required
    public void setUserActionService(UserActionService userActionService) {
        this.userActionService = userActionService;
    }
}
