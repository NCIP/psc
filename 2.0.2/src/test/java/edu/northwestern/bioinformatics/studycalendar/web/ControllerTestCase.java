package edu.northwestern.bioinformatics.studycalendar.web;

import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.Map;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public abstract class ControllerTestCase extends WebTestCase {
    protected static void assertNoBindingErrorsFor(String fieldName, Map<String, Object> model) {
        BindingResult result = (BindingResult) model.get(BindingResult.MODEL_KEY_PREFIX + "command");
        List<FieldError> fieldErrors = result.getFieldErrors(fieldName);
        assertEquals("There were errors for field " + fieldName + ": " + fieldErrors, 0, fieldErrors.size());
    }
}
