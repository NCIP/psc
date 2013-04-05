/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Map;

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
