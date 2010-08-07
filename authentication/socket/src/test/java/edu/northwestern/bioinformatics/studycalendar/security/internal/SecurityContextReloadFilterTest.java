package edu.northwestern.bioinformatics.studycalendar.security.internal;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import junit.framework.TestCase;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.context.SecurityContextImpl;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import java.io.IOException;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.*;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class SecurityContextReloadFilterTest extends TestCase {
    private SecurityContextReloadFilter filter;

    private PscUserDetailsService userDetailsService;
    private FilterChain chain;

    private MockRegistry mocks;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private PscUser user;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mocks = new MockRegistry();
        chain = mocks.registerMockFor(FilterChain.class);
        userDetailsService = mocks.registerMockFor(PscUserDetailsService.class);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        user = createPscUser("P", PscRole.STUDY_QA_MANAGER);
        SecurityContextHolder.setContext(new SecurityContextImpl());
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(user, null));

        filter = new SecurityContextReloadFilter();
        filter.setPscUserDetailsService(userDetailsService);
    }

    @Override
    protected void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        super.tearDown();
    }

    public void testFilterSucceedsWithNoSecurityContext() throws Exception {
        /* expect */ chain.doFilter(request, response);

        SecurityContextHolder.clearContext();
        doFilter();
        // expect no exceptions
    }

    public void testFilterDoesNothingIfReloadFlagNotSet() throws Exception {
        /* expect */ chain.doFilter(request, response);

        user.setStale(false);
        doFilter();
    }

    public void testFilterReplacesTheAuthenticationIfReloadFlagSet() throws Exception {
        /* expect */ chain.doFilter(request, response);

        PscUser replacement = createPscUser("P", PscRole.STUDY_QA_MANAGER, PscRole.DATA_READER);
        expect(userDetailsService.loadUserByUsername("P")).andReturn(replacement);

        user.setStale(true);
        doFilter();

        Authentication actual = SecurityContextHolder.getContext().getAuthentication();
        assertSame("Not replaced", replacement, actual.getPrincipal());
        assertEquals("Authorities not correct", 2, actual.getAuthorities().length);
    }

    public void testReloadedUserReceivesArbitraryAttributes() throws Exception {
        /* expect */ chain.doFilter(request, response);

        PscUser replacement = createPscUser("P", PscRole.STUDY_QA_MANAGER);
        expect(userDetailsService.loadUserByUsername("P")).andReturn(replacement);

        user.setStale(true);
        user.setAttribute("foo", "quux");
        doFilter();

        assertEquals("quux", replacement.getAttribute("foo"));
    }

    private void doFilter() throws IOException, ServletException {
        mocks.replayMocks();
        filter.doFilter(request, response, chain);
        mocks.verifyMocks();
    }
}
