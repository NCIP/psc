/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;

import javax.servlet.FilterChain;

import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class UserInRequestFilterTest extends ContextRetainingFilterTestCase {
    private UserInRequestFilter filter;

    private FilterChain filterChain;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        filterChain = registerMockFor(FilterChain.class);

        filter = new UserInRequestFilter();
        initFilter(filter);

        // should always continue
        /* expect */ filterChain.doFilter(request, response);
        expect(mockApplicationContext.getBean("applicationSecurityManager")).
            andStubReturn(applicationSecurityManager);
    }

    public void testAttributeSetWhenLoggedIn() throws Exception {
        PscUser cab = AuthorizationObjectFactory.createPscUser("cab", new PscRole[0]);
        SecurityContextHolderTestHelper.setSecurityContext(cab, "pass");

        replayMocks();
        filter.doFilter(request, response, filterChain);
        verifyMocks();

        assertEquals("cab", request.getAttribute("user"));
        assertEquals(cab, request.getAttribute("currentUser"));
    }

    public void testAttributeNotSetWhenNotLoggedIn() throws Exception {
        applicationSecurityManager.removeUserSession();

        replayMocks();
        filter.doFilter(request, response, filterChain);
        verifyMocks();

        assertFalse(request.getAttributeNames().hasMoreElements());
    }
}
