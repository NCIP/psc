/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.data.Form;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;

/**
 * @author Rhett Sutphin
 */
public class FormParametersTest extends RestletTestCase {
    private Form form;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        form = new Form();
    }

    public void testAttributeNameIsHyphenatedAndLowerCase() throws Exception {
        assertEquals("activity-code", FormParameters.ACTIVITY_CODE.attributeName());
    }

    public void testGetStringValueFromForm() throws Exception {
        form.add("day", "12");
        assertEquals("12", FormParameters.DAY.extractFirstFrom(form));
    }

    public void testGetStringValueFromFormForWeight() throws Exception {
        form.add("weight", "2");
        assertEquals("2", FormParameters.WEIGHT.extractFirstFrom(form));
    }

    public void testGetStringValueFromFormWhenNotSet() throws Exception {
        assertNull(FormParameters.DAY.extractFirstFrom(form));
    }

    public void testGetStringValueFromFormWhenNotSetForWeight() throws Exception {
        assertNull(FormParameters.WEIGHT.extractFirstFrom(form));
    }

    public void testGetIntegerValueFromForm() throws Exception {
        form.add("day", "12");
        assertEquals(12, (int) FormParameters.DAY.extractFirstAsIntegerFrom(form));    
    }

    public void testGetIntegerValueFromFormForWeight() throws Exception {
        form.add("weight", "2");
        assertEquals(2, (int) FormParameters.WEIGHT.extractFirstAsIntegerFrom(form));    
    }

    public void testGetIntegerValueFromFormWhenNotSet() throws Exception {
        assertNull(FormParameters.DAY.extractFirstAsIntegerFrom(form));
    }

    public void testGetIntegerValueFromFormWhenNotInteger() throws Exception {
        form.add("day", "twelve");
        try {
            FormParameters.DAY.extractFirstAsIntegerFrom(form);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException ve) {
            assertEquals("Expected day parameter value to be an integer, not 'twelve'",
                ve.getMessage());
        }
    }
}
