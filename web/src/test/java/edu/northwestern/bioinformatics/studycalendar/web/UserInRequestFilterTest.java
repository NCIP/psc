package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import static org.easymock.classextension.EasyMock.expect;

import javax.servlet.FilterChain;

/**
 * @author Rhett Sutphin
 */
public class UserInRequestFilterTest extends ContextRetainingFilterTestCase {
    private UserInRequestFilter filter;

    private FilterChain filterChain;
    private UserDao userDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        filterChain = registerMockFor(FilterChain.class);
        userDao = registerDaoMockFor(UserDao.class);

        filter = new UserInRequestFilter();
        initFilter(filter);

        // should always continue
        filterChain.doFilter(request, response);
        expect(mockApplicationContext.getBean("applicationSecurityManager")).
            andStubReturn(applicationSecurityManager);
    }

    public void testAttributeSetWhenLoggedIn() throws Exception {
        User cab = Fixtures.createUser("cab");
        SecurityContextHolderTestHelper.setSecurityContext("cab", "pass");

        expect(mockApplicationContext.getBean("userDao")).andReturn(userDao);
        expect(userDao.getByName("cab")).andReturn(cab);

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
