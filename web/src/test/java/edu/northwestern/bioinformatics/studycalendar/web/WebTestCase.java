/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.TestingTemplateService;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import gov.nih.nci.cabig.ctms.suite.authorization.ScopeType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationScopeMappings.getMapping;
import static org.apache.commons.collections.CollectionUtils.subtract;

/**
 * @author Rhett Sutphin
 */
public abstract class WebTestCase extends StudyCalendarTestCase {
    protected MockHttpServletRequest request;
    protected MockHttpServletResponse response;
    protected MockServletContext servletContext;
    protected MockHttpSession session;
    protected ControllerTools controllerTools;
    protected TemplateService templateService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        servletContext = new MockServletContext();
        session = new MockHttpSession(servletContext);
        request = new MockHttpServletRequest(servletContext);
        request.setMethod("POST");
        request.setSession(session);
        response = new MockHttpServletResponse();
        templateService = new TestingTemplateService();
        controllerTools = new ControllerTools();
        controllerTools.setTemplateService(templateService);
    }

    public static String findWebappSrcDirectory() {
        File dir = new File("src/main/webapp");
        if (dir.exists()) {
            return dir.getPath();
        }
        dir = new File("web", dir.toString());
        if (dir.exists()) {
            return dir.getPath();
        }
        throw new IllegalStateException("Could not find webapp path");
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
