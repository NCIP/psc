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

/**
 * @author Rhett Sutphin
 */
public class AuthenticationSystemSocketTest extends AuthenticationTestCase {
    private AuthenticationSystemSocket socket;

    private AuthenticationSystem authenticationSystem;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        authenticationSystem = registerMockFor(AuthenticationSystem.class);

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = registerMockFor(FilterChain.class);

        socket = new AuthenticationSystemSocket();
        socket.setConfiguration(new AuthenticationSystemConfiguration() {
            @Override
            public synchronized AuthenticationSystem getAuthenticationSystem() {
                return authenticationSystem;
            }
        });
    }
    
    public void testDoFilterWhenAuthSystemHasNone() throws Exception {
        expect(authenticationSystem.filter()).andReturn(null).anyTimes();
        filterChain.doFilter(request, response);
        replayMocks();

        socket.doFilter(request, response, filterChain);
        verifyMocks();
    }

    public void testDoFilterDelegatesWhenAuthSystemProvidesFilter() throws Exception {
        Filter systemFilter = registerMockFor(Filter.class);
        expect(authenticationSystem.filter()).andReturn(systemFilter).anyTimes();
        systemFilter.doFilter(request, response, filterChain);
        replayMocks();

        socket.doFilter(request, response, filterChain);
        verifyMocks();
    }
}
