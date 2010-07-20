package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;

import java.util.*;

/**
 * @author Rhett Sutphin
 */
public abstract class ControllerTestCase extends WebTestCase {
    protected static void assertNoBindingErrorsFor(String fieldName, Map<String, Object> model) {
        BindingResult result = (BindingResult) model.get(BindingResult.MODEL_KEY_PREFIX + "command");
        List<FieldError> fieldErrors = result.getFieldErrors(fieldName);
        assertEquals("There were errors for field " + fieldName + ": " + fieldErrors, 0, fieldErrors.size());
    }

    protected void assertRolesAllowed(Collection<ResourceAuthorization> resourceAuthorizations, PscRole... roles) {
        Collection<PscRole> expected = Arrays.asList(roles);
        Collection<PscRole> actual = new ArrayList<PscRole>();
        if (resourceAuthorizations == null) {
            actual = Arrays.asList(PscRole.values());
        } else {
            for (ResourceAuthorization actualResourceAuthorization : resourceAuthorizations) {
                actual.add(actualResourceAuthorization.getRole());
            }
        }

        for (PscRole role : expected) {
            assertTrue(role.getDisplayName() + " should be allowed",
                actual.contains(role));
        }
    }
}
