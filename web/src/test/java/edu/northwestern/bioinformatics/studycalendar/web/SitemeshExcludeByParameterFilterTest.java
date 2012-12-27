/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;


import com.opensymphony.module.sitemesh.RequestConstants;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.FilterChain;
import java.util.regex.Pattern;

/**
 * @author rsutphin
 */
public class SitemeshExcludeByParameterFilterTest extends StudyCalendarTestCase {
    private SitemeshExcludeByParameterFilter filter = new SitemeshExcludeByParameterFilter();
    private MockFilterConfig filterConfig;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;
    private MockServletContext servletContext;

    protected void setUp() throws Exception {
        super.setUp();
        servletContext = new MockServletContext();
        filterConfig = new MockFilterConfig(servletContext);
        request = new MockHttpServletRequest(servletContext);
        response = new MockHttpServletResponse();
        filterChain = registerMockFor(FilterChain.class);
        filterChain.doFilter(request, response);
        replayMocks();
    }

    public void testInit() throws Exception {
        filterConfig.addInitParameter("pattern", ".*");
        filter.init(filterConfig);

        assertEquals(Pattern.compile(".*").pattern(), filter.getPattern().pattern());
    }

    public void testInitWithoutPatternParam() throws Exception {
        try {
            filter.init(filterConfig);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertContains(iae.getMessage(), SitemeshExcludeByParameterFilter.class.getName());
            assertContains(iae.getMessage(), "pattern");
        }
    }

    public void testRequestWithParameter() throws Exception {
        filter.setPattern(Pattern.compile("d-\\d+-e"));
        request.addParameter("any", "thing");
        request.addParameter("d-16544-e", "2");
        request.addParameter("other", "foo");

        assertNull("FILTER_APPLIED initially set",
            request.getAttribute(RequestConstants.FILTER_APPLIED));

        filter.doFilter(request, response, filterChain);
        verifyMocks();

        assertEquals("FILTER_APPLIED not set",
            Boolean.TRUE, request.getAttribute(RequestConstants.FILTER_APPLIED));
    }

    public void testRequestWithoutParameter() throws Exception {
        filter.setPattern(Pattern.compile("d-\\d+-e"));
        request.addParameter("any", "thing");
        request.addParameter("other", "foo");

        assertNull("FILTER_APPLIED initially set",
            request.getAttribute(RequestConstants.FILTER_APPLIED));

        filter.doFilter(request, response, filterChain);
        verifyMocks();

        assertNull("FILTER_APPLIED set",
            request.getAttribute(RequestConstants.FILTER_APPLIED));
    }
}
