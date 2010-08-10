package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import gov.nih.nci.cabig.ctms.suite.authorization.ScopeType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.*;

import static edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.AuthorizationScopeMappings.getMapping;
import static org.apache.commons.collections.CollectionUtils.subtract;

/**
 * @author Rhett Sutphin
 */
public abstract class ControllerTestCase extends WebTestCase {
    protected static void assertNoBindingErrorsFor(String fieldName, Map<String, Object> model) {
        BindingResult result = (BindingResult) model.get(BindingResult.MODEL_KEY_PREFIX + "command");
        List<FieldError> fieldErrors = result.getFieldErrors(fieldName);
        assertEquals("There were errors for field " + fieldName + ": " + fieldErrors, 0, fieldErrors.size());
    }

    protected void assertRolesAllowed(Collection<ResourceAuthorization> actual, PscRole... expected) {
        Collection<PscRole> actualRoles = new ArrayList<PscRole>();
        if (actual == null) {
            actualRoles = Arrays.asList(PscRole.values());
        } else {
            for (ResourceAuthorization actualResourceAuthorization : actual) {
                actualRoles.add(actualResourceAuthorization.getRole());
            }
        }

        for (PscRole role : expected) {
            assertTrue(role.getDisplayName() + " should be allowed",
                actualRoles.contains(role));
        }

        for (PscRole role : actualRoles) {
            assertTrue(role.getDisplayName() + " should not be allowed", Arrays.asList(expected).contains(role));
        }
    }

    @SuppressWarnings("unchecked")
    protected void assertSiteScopedRolesAllowed(Collection<ResourceAuthorization> actual, Site expectedSite, PscRole... expectedRoles) {
        Map<PscRole, String> actualRoles = new HashMap<PscRole, String>();

        if (actual == null) {
            fail("No roles scoped to sites");
        } else {
            for (ResourceAuthorization actualResourceAuthorization : actual) {
                actualRoles.put(actualResourceAuthorization.getRole(), actualResourceAuthorization.getScope(ScopeType.SITE));
            }

            String sa = getMapping(ScopeType.SITE).getSharedIdentity(expectedSite);

            for (PscRole role : expectedRoles) {
                assertTrue(role.getDisplayName() + " should be scoped to " + expectedSite.getAssignedIdentifier(),
                        actualRoles.get(role) != null && actualRoles.get(role).equals(sa));
            }

            Collection<PscRole> minus = subtract(actualRoles.keySet(), Arrays.asList(expectedRoles));
            for (PscRole role : minus) {
                fail(role.getDisplayName() + " should not be allowed");
            }
        }
    }

    protected void assertOnlyAllScopedRolesAllowed(Collection<ResourceAuthorization> actual, ScopeType expectedScope, PscRole... expectedRoles) {
        Map<PscRole, Boolean> actualRoles = new HashMap<PscRole, Boolean>();

        if (actual == null) {
            fail("No roles scoped to sites");
        } else {
            for (ResourceAuthorization actualResourceAuthorization : actual) {
                actualRoles.put(actualResourceAuthorization.getRole(), actualResourceAuthorization.isAllScoped(expectedScope));
            }

            for (PscRole role : expectedRoles) {
                assertTrue(role.getDisplayName() + " should be all-" + expectedScope.getName() + " scoped",
                        actualRoles.get(role) != null && actualRoles.get(role).equals(true));
            }

            Collection<PscRole> minus = subtract(actualRoles.keySet(), Arrays.asList(expectedRoles));
            for (PscRole role : minus) {
                fail(role.getDisplayName() + " should not be allowed");
            }
        }
    }
}
