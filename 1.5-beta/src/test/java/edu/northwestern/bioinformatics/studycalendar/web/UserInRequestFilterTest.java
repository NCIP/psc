package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.SecurityContextHolderTestHelper;
import static org.easymock.classextension.EasyMock.expect;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.FilterChain;

/**
 * @author Rhett Sutphin
 */
public class UserInRequestFilterTest extends WebTestCase {
    private UserInRequestFilter filter;

    private FilterChain filterChain;
    private UserDao userDao;
    private WebApplicationContext mockApplicationContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockApplicationContext = registerMockFor(WebApplicationContext.class);
        filterChain = registerMockFor(FilterChain.class);
        userDao = registerDaoMockFor(UserDao.class);

        servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, mockApplicationContext);

        filter = new UserInRequestFilter();
        filter.init(new MockFilterConfig(servletContext));

        // should always continue
        filterChain.doFilter(request, response);
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
        ApplicationSecurityManager.removeUserSession();

        replayMocks();
        filter.doFilter(request, response, filterChain);
        verifyMocks();

        assertFalse(request.getAttributeNames().hasMoreElements());
    }
}
