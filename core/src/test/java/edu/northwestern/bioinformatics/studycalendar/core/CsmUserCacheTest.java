/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.core;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.exceptions.CSObjectNotFoundException;

import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class CsmUserCacheTest extends StudyCalendarTestCase {
    private CsmUserCache cache;

    private AuthorizationManager authorizationManager;
    private User user16, user17;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        authorizationManager = registerMockFor(AuthorizationManager.class);

        cache = new CsmUserCache();
        cache.setCsmAuthorizationManager(authorizationManager);

        user16 = AuthorizationObjectFactory.createCsmUser(16, "jo");
        user17 = AuthorizationObjectFactory.createCsmUser(17, "joe");
    }

    public void testLooksUpAUserThatIsNotCached() throws Exception {
        expect(authorizationManager.getUserById("16")).andReturn(user16).once();
        replayMocks();

        assertSame("User not returned", user16, cache.getCsmUser(16));
        verifyMocks();
    }

    public void testCachesAUserThatIsLookedUp() throws Exception {
        expect(authorizationManager.getUserById("16")).andReturn(user16).once();
        replayMocks();

        cache.getCsmUser(16);
        assertSame("Same user not returned second time", user16, cache.getCsmUser(16));
        verifyMocks();
    }

    public void testReturnsNullForUserNotFound() throws Exception {
        expect(authorizationManager.getUserById("16")).andThrow(new CSObjectNotFoundException("Nope"));
        replayMocks();

        assertNull("User should not have been returned", cache.getCsmUser(16));
        verifyMocks();
    }

    public void testDoesNotCacheForUserNotFound() throws Exception {
        expect(authorizationManager.getUserById("16")).andThrow(new CSObjectNotFoundException("Nope")).times(2);
        replayMocks();

        assertNull("User should not have been returned", cache.getCsmUser(16));
        assertNull("User should still not have been returned", cache.getCsmUser(16));
        verifyMocks();
    }

    public void testReturnsNullForUserNull() throws Exception {
        expect(authorizationManager.getUserById("16")).andReturn(null);
        replayMocks();

        assertNull("User should not have been returned", cache.getCsmUser(16));
        verifyMocks();
    }

    public void testDoesNotCacheForUserNull() throws Exception {
        expect(authorizationManager.getUserById("16")).andReturn(null).times(2);
        replayMocks();

        assertNull("User should not have been returned", cache.getCsmUser(16));
        assertNull("User should still not have been returned", cache.getCsmUser(16));
        verifyMocks();
    }

    public void testCacheInvalidationForcesReload() throws Exception {
        expect(authorizationManager.getUserById("16")).andReturn(user16).andReturn(user17);
        replayMocks();

        assertSame("Correct user not returned for first call", user16, cache.getCsmUser(16));
        cache.invalidate(16);
        assertSame("Correct user not returned for first call", user17, cache.getCsmUser(16));

        verifyMocks();
    }

    public void testCacheInvalidationOnlyInvalidatesSpecifiedId() throws Exception {
        expect(authorizationManager.getUserById("16")).andReturn(user16).times(2);
        expect(authorizationManager.getUserById("17")).andReturn(user17).once();
        replayMocks();

        cache.getCsmUser(16);
        cache.getCsmUser(17);
        cache.invalidate(16);
        cache.getCsmUser(16);
        cache.getCsmUser(17);
        verifyMocks();
    }
}
