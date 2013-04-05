/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

/**
 * @author Rhett Sutphin
 */
public class ValidatingFormTest extends StudyCalendarTestCase {
    public void testErrorAddedForMissingRequiredField() throws Exception {
        ValidatingForm form = new ValidatingForm("").
            validatePresenceOf(FormParameters.ACTIVITY_CODE);
        assertErrorCount(1, form);
        assertEquals("Missing required parameter activity-code", form.getErrors().get(0));
    }
    
    public void testErrorAddedForBlankPresenceOfField() throws Exception {
        ValidatingForm form = new ValidatingForm("activity-code=+").
            validatePresenceOf(FormParameters.ACTIVITY_CODE);
        assertErrorCount(1, form);
        assertEquals("Missing required parameter activity-code", form.getErrors().get(0));
    }
    
    public void testNoErrorAddedForPresentPresenceOfField() throws Exception {
        ValidatingForm form = new ValidatingForm("activity-code=412").
            validatePresenceOf(FormParameters.ACTIVITY_CODE);
        assertErrorCount(0, form);
    }

    public void testErrorAddedForFloatInIntegralField() throws Exception {
        ValidatingForm form = new ValidatingForm("day=4.3").
            validateIntegralityOf(FormParameters.DAY);
        assertErrorCount(1, form);
        assertEquals("Parameter day must be an integer ('4.3' isn't)", form.getErrors().get(0));
    }

    public void testErrorAddedForTextInIntegralField() throws Exception {
        ValidatingForm form = new ValidatingForm("day=eleven").
            validateIntegralityOf(FormParameters.DAY);
        assertErrorCount(1, form);
        assertEquals("Parameter day must be an integer ('eleven' isn't)", form.getErrors().get(0));
    }

    public void testNoErrorAddedForIntegerInIntegralField() throws Exception {
        ValidatingForm form = new ValidatingForm("day=11").
            validateIntegralityOf(FormParameters.DAY);
        assertErrorCount(0, form);
    }

    public void testNoErrorAddedForBlankInIntegralField() throws Exception {
        ValidatingForm form = new ValidatingForm("day=+").
            validateIntegralityOf(FormParameters.DAY);
        assertErrorCount(0, form);
    }

    public void testDoesNotThrowExceptionWhenNoErrors() throws Exception {
        ValidatingForm form = new ValidatingForm("foo=bar");
        form.throwForValidationFailureIfNecessary();
    }

    public void testThrowsExceptionWhenRequestedIfThereAreErrors() throws Exception {
        ValidatingForm form = new ValidatingForm("").
            addError("Bad things are afoot").
            addError("Very bad things");
        try {
            form.throwForValidationFailureIfNecessary();
            fail("Exception not thrown");
        } catch (ResourceException actual) {
            assertEquals("Wrong status",
                Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY.getCode(),
                actual.getStatus().getCode());
            assertEquals("Wrong description", "Bad things are afoot\nVery bad things",
                actual.getStatus().getDescription());
        }
    }

    private static void assertErrorCount(int expectedCount, ValidatingForm form) {
        assertEquals("Expected " + expectedCount + " errors", expectedCount, 
            form.getErrors().size());
    }
}
