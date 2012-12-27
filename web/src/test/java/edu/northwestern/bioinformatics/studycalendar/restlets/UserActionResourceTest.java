/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.UserActionDao;
import edu.northwestern.bioinformatics.studycalendar.domain.UserAction;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.UserActionService;
import org.restlet.data.Method;
import org.restlet.data.Status;

import java.util.ArrayList;
import java.util.Arrays;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_SUBJECT_CALENDAR_MANAGER;
import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class UserActionResourceTest extends AuthorizedResourceTestCase<UserActionResource> {
    private static final String USER_ACTION_IDENTIFIER = "1111";
    private static final String CONTEXT = "context";
    private UserActionDao userActionDao;
    private UserActionService userActionService;

    public void setUp() throws Exception {
        super.setUp();
        userActionDao = registerDaoMockFor(UserActionDao.class);
        userActionService = registerMockFor(UserActionService.class);
        request.getAttributes().put(UriTemplateParameters.USER_ACTION_IDENTIFIER.attributeName(), USER_ACTION_IDENTIFIER);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected UserActionResource createAuthorizedResource() {
        UserActionResource resource = new UserActionResource();
        resource.setUserActionDao(userActionDao);
        resource.setUserActionService(userActionService);
        return resource;
    }

    public void testAllowedMethods() throws Exception {
        assertAllowedMethods("DELETE");
    }

    public void testDeleteWithAuthorizedRoles() {
        assertRolesAllowedForMethod(Method.DELETE, STUDY_SUBJECT_CALENDAR_MANAGER);
    }

    public void test400WhenNoUserActionIdentifierInRequest() throws Exception {
        UriTemplateParameters.USER_ACTION_IDENTIFIER.removeFrom(request);

        doDelete();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    public void test404WhenUnknownUserAction() throws Exception {
        expect(userActionDao.getByGridId(USER_ACTION_IDENTIFIER)).andReturn(null);

        doDelete();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void test403WhenUserCannotUndoUserAction() throws Exception {
        setCurrentUser(AuthorizationObjectFactory.createPscUser("bad", PscRole.SYSTEM_ADMINISTRATOR));
        doDelete();
        assertResponseStatus(Status.CLIENT_ERROR_FORBIDDEN);
    }

    public void test403WhenCurrentUserIsNotUserActionUser() throws Exception {
        expect(userActionDao.getByGridId(USER_ACTION_IDENTIFIER)).andReturn(setGridId("gridId",
                new UserAction("Delayed for 4 days", "context", "delay",
                        false, AuthorizationObjectFactory.createCsmUser(11, "perry"))));
        doDelete();
        assertResponseStatus(Status.CLIENT_ERROR_FORBIDDEN,
                "Does not have proper credentials to undo the user action gridId");
    }

    public void test400WhenUserActionIsAlreadyUndone() throws Exception {
        UserAction ua = createUserAction("gridId", "Delayed for 4 days", "delay", true);
        expect(userActionDao.getByGridId(USER_ACTION_IDENTIFIER)).andReturn(ua);
        doDelete();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Already undone user action gridId");
    }

    public void test400WhenUndoableActionsListIsEmpty() throws Exception {
        UserAction ua = createUserAction("gridId", "Delayed for 4 days", "delay", false);
        expect(userActionDao.getByGridId(USER_ACTION_IDENTIFIER)).andReturn(ua);
        expect(userActionService.getUndoableActions("context")).andReturn(new ArrayList<UserAction>());
        doDelete();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST,"No undoable user action gridId");
    }

    public void test400WhenUserActionIsOutOfOrderOfUndoableActions() throws Exception {
        UserAction ua1 = createUserAction("gridId1", "Delayed for 4 days", "delay", false);
        UserAction ua2 = createUserAction("gridId2", "Advance for 2 days", "advance", false);
        expect(userActionDao.getByGridId(USER_ACTION_IDENTIFIER)).andReturn(ua1);
        expect(userActionService.getUndoableActions("context")).andReturn(Arrays.asList(ua2, ua1));
        doDelete();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST,
                "Requested user action is out of order in undoable list");
    }

    public void testApplyUndoOnUserAction() throws Exception {
        UserAction ua1 = createUserAction("gridId1", "Delayed for 4 days", "delay", false);
        UserAction ua2 = createUserAction("gridId2", "Advance for 2 days", "advance", false);
        expect(userActionDao.getByGridId(USER_ACTION_IDENTIFIER)).andReturn(ua1);
        expect(userActionService.getUndoableActions("context")).andReturn(Arrays.asList(ua1, ua2));
        expect(userActionService.applyUndo(ua1)).andReturn(ua1);
        doDelete();
        assertResponseStatus(Status.SUCCESS_OK);
    }

    private UserAction createUserAction(String gridId, String description, String actionType, Boolean undone) {
        getCurrentUser().getCsmUser().setUserId(12L);
        return setGridId(gridId, new UserAction(description, CONTEXT, actionType,
                        undone, getCurrentUser().getCsmUser()));
    }
}
