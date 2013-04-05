/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import static org.easymock.classextension.EasyMock.expectLastCall;
import org.springframework.web.bind.MissingServletRequestParameterException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class ExceptionTranslationFilterTest extends WebTestCase {
    private ExceptionTranslationFilter filter;
    private FilterChain filterChain;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        filter = new ExceptionTranslationFilter();
        filterChain = registerMockFor(FilterChain.class);
    }

    public void testNoExceptionProceedsNormally() throws Exception {
        response.setStatus(200);
        filterChain.doFilter(request, response);

        doFilter();

        assertEquals(200, response.getStatus());
    }

    public void testMissingParameterBecomesBadRequest() throws Exception {
        filterChain.doFilter(request, response);
        expectLastCall().andThrow(new MissingServletRequestParameterException("hatSize", "int"));

        doFilter();

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    public void testMissingBoundPropertyBecomesBadRequest() throws Exception {
        filterChain.doFilter(request, response);
        expectLastCall().andThrow(new MissingRequiredBoundProperty("hatSize"));

        doFilter();

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    public void testUnhandledExceptionPassesUp() throws Exception {
        RuntimeException uhoh = new RuntimeException("Mr. Bill, no!");
        filterChain.doFilter(request, response);
        expectLastCall().andThrow(uhoh);

        try {
            doFilter();
            fail("Exception not rethrown");
        } catch (RuntimeException re) {
            assertEquals("Same exception not rethrown", uhoh, re);
        }
    }

    private void doFilter() throws IOException, ServletException {
        replayMocks();
        filter.doFilter(request, response, filterChain);
        verifyMocks();
    }

}
