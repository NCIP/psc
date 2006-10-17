package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Rhett Sutphin
 */
public class ControllerToolsTest extends StudyCalendarTestCase {
    private MockHttpServletRequest request = new MockHttpServletRequest();

    public void testAjaxRequestWhenTrue() throws Exception {
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        assertTrue(ControllerTools.isAjaxRequest(request));
    }

    public void testAjaxRequestWithoutHeader() throws Exception {
        assertFalse(ControllerTools.isAjaxRequest(request));
    }
    
    public void testAjaxRequestWithOtherValue() throws Exception {
        request.addHeader("X-Requested-With", "Firefox");
        assertFalse(ControllerTools.isAjaxRequest(request));
    }
}
