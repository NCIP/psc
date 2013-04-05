/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.mock.web.MockFilterConfig;

import javax.servlet.Filter;
import javax.servlet.ServletException;

/**
 * @author Rhett Sutphin
 */
public abstract class ContextRetainingFilterTestCase extends WebTestCase {
    protected WebApplicationContext mockApplicationContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockApplicationContext = registerMockFor(WebApplicationContext.class);
        servletContext.setAttribute(
            WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, mockApplicationContext);
    }

    protected void initFilter(Filter testFilter) throws ServletException {
        testFilter.init(new MockFilterConfig(servletContext));
    }
}
