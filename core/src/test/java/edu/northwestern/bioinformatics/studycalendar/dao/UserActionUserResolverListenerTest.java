package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.core.CsmUserCache;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.UserAction;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.hibernate.event.PostLoadEvent;

import static org.easymock.EasyMock.expect;

public class UserActionUserResolverListenerTest extends StudyCalendarTestCase {
    private UserActionUserResolverListener listener;

    private PostLoadEvent event;
    private UserAction action;
    private CsmUserCache csmUserCache;
    private User csmUser;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        action = new UserAction();
        csmUser = AuthorizationObjectFactory.createCsmUser(55L, "jo");

        event = new PostLoadEvent(null);
        event.setEntity(action);

        csmUserCache = registerMockFor(CsmUserCache.class);

        listener = new UserActionUserResolverListener();
        listener.setCsmUserCache(csmUserCache);
    }

    public void testManagerResolvedWhenResolveable() throws Exception {
        action.setCsmUserId(55);
        expect(csmUserCache.getCsmUser(55)).andReturn(csmUser);

        fire();
        assertSame(csmUser, action.getUser());
    }

    public void testNotResolvedWhenNoManagerCsmId() throws Exception {
        action.setCsmUserId(null);
        fire();
        assertNull(action.getUser());
    }

    public void testNotErrorWhenCsmReturnsNull() throws Exception {
        action.setCsmUserId(78);
        expect(csmUserCache.getCsmUser(78)).andReturn(null);
        fire();
        assertEquals("ID reset", (Object) 78, action.getCsmUserId());
    }

    public void testNothingHappensForNonAssignmentObjects() throws Exception {
        event.setEntity(new Study());
        fire();
    }

    private void fire() {
        replayMocks();
        listener.onPostLoad(event);
        verifyMocks();
    }
}
