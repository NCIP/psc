package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.test.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import org.springframework.mock.web.MockHttpServletRequest;

import java.beans.PropertyEditor;

/**
 * @author Rhett Sutphin
 */
public class ControllerToolsTest extends StudyCalendarTestCase {
    private MockHttpServletRequest request;
    private ControllerTools tools;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        request = new MockHttpServletRequest();
        tools = new ControllerTools();
    }

    public void testAjaxRequestWhenTrue() throws Exception {
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        assertTrue(tools.isAjaxRequest(request));
    }

    public void testAjaxRequestWithoutHeader() throws Exception {
        assertFalse(tools.isAjaxRequest(request));
    }
    
    public void testAjaxRequestWithOtherValue() throws Exception {
        request.addHeader("X-Requested-With", "Firefox");
        assertFalse(tools.isAjaxRequest(request));
    }

    public void testGetCurrentUser() throws Exception {
        assertNull(tools.getCurrentUser(request));
        User user = Fixtures.createUser("jimbo");
        request.setAttribute("currentUser", user);
        assertSame(user, tools.getCurrentUser(request));
    }



   	public void testCustomDateEditorWithExactDateLength() {
   		int maxLength = 10;
   		String validDate = "01/01/2005";
        String alsoValidDate = "1/1/2005";
        String invalidDate = "01/01/05";

   		assertTrue(validDate.length() == maxLength);
   		assertFalse(invalidDate.length() == maxLength);

           PropertyEditor dateEditor = tools.getDateEditor(false);
           //   CustomDateEditor editor = new CustomDateEditor(new SimpleDateFormat("MM/dd/yyyy"), true, maxLength);

   		try {
   		dateEditor.setAsText(validDate);
   		}
   		catch (IllegalArgumentException ex) {
   			fail("Exception shouldn't be thrown because this is a valid date");
   		}

   		try {
  			dateEditor.setAsText(invalidDate);
   			fail("Exception should be thrown because this is an invalid date");
   		}
  		catch (IllegalArgumentException ex) {
   			// expected to have the error message on failing year
   			assertTrue(ex.getMessage().indexOf("05") != -1);
   		}

   		try {
   		    dateEditor.setAsText(alsoValidDate);
   		}
   		catch (IllegalArgumentException ex) {
   			fail("Exception shouldn't be thrown because this is a valid date");
   		}
    }
    
}
