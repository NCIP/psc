/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.internal;

import edu.northwestern.bioinformatics.studycalendar.security.AuthenticationSystemConfiguration;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationTestCase;
import static org.easymock.classextension.EasyMock.expect;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class AuthenticationSystemLogoutFilterTest extends AuthenticationTestCase {
    private AuthenticationSystemLogoutFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    private AuthenticationSystem authenticationSystem;
    private Filter defaultFilter;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = registerMockFor(FilterChain.class);
        defaultFilter = registerMockFor(Filter.class);

        AuthenticationSystemConfiguration configuration = registerMockFor(AuthenticationSystemConfiguration.class);
        authenticationSystem = registerMockFor(AuthenticationSystem.class);
        expect(configuration.getAuthenticationSystem()).andStubReturn(authenticationSystem);

        filter = new AuthenticationSystemLogoutFilter();
        filter.setAuthenticationSystemConfiguration(configuration);
        filter.setDefaultLogoutFilter(defaultFilter);
    }

    public void testPrefersExplicitLogoutFilter() throws Exception {
        Filter mockFilter = registerMockFor(Filter.class);
        expect(authenticationSystem.logoutFilter()).andReturn(mockFilter);
        mockFilter.doFilter(request, response, filterChain);

        doFilter();
    }

    public void testUsesDefaultFilterIfNoExplicitFilter() throws Exception {
        expect(authenticationSystem.logoutFilter()).andReturn(null);
        defaultFilter.doFilter(request, response, filterChain);

        doFilter();
    }

    private void doFilter() throws IOException, ServletException {
        replayMocks();
        filter.doFilter(request, response, filterChain);
        verifyMocks();
    }
}
