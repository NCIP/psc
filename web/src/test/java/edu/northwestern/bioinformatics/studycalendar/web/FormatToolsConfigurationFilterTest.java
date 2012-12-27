/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;


import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.configuration.MockConfiguration;
import edu.northwestern.bioinformatics.studycalendar.tools.FormatTools;
import org.springframework.mock.web.MockFilterChain;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author Nataliya Shurupova
 */
public class FormatToolsConfigurationFilterTest extends WebTestCase {
    private FormatToolsConfigurationFilter filter;
    private FilterChain filterChain;
    private MockConfiguration configuration;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        filterChain = new MockFilterChain();
        configuration = new MockConfiguration();
        request.setAttribute("configuration", configuration);

        filter = new FormatToolsConfigurationFilter();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        FormatTools.clearLocalInstance();
    }

    public void testExceptionIfConfigurationNotAvailable() throws Exception {
        request.setAttribute("configuration", null);

        try {
            filter.doFilter(request, response, filterChain);
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException scse) {
            assertEquals("Wrong message", "configuration not present in request attributes", scse.getMessage());
        }
    }

    public void testFormatToolsIsSetDuringRequestAndClearedAfterward() throws Exception {
        configuration.set(Configuration.DISPLAY_DATE_FORMAT, "MM/DD/YYYY");
        assertCorrectFormatToolsSet("MM/dd/yyyy");
    }

    public void testFormatToolsForEuropeanFormat() throws Exception {
        configuration.set(Configuration.DISPLAY_DATE_FORMAT, "DD/MM/YYYY");
        assertCorrectFormatToolsSet("dd/MM/yyyy");
    }

    private void assertCorrectFormatToolsSet(final String expectedSimpleDateFormat) throws IOException, ServletException {
        final boolean[] executed = new boolean[] { false };

        filter.doFilter(request, response, new FilterChain() {
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
                executed[0] = true;
                assertTrue("Local instance not set", FormatTools.hasLocalInstance());
                assertEquals("Wrong instance set: " + FormatTools.getLocal().getDateFormatString(),
                        expectedSimpleDateFormat, FormatTools.getLocal().getDateFormatString());
            }
        });

        assertTrue("Filter chain should have been executed", executed[0]);
        assertFalse("Should not be set any more", FormatTools.hasLocalInstance());
    }
}
