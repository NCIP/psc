package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.restlet.data.Method;
import org.restlet.resource.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author John Dzak
 */
public abstract class AuthorizedResourceTestCase<R extends Resource & AuthorizedResource> extends ResourceTestCase<R> {
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        PscUser user = AuthorizationObjectFactory.
            createLegacyPscUser("josephine", PscRole.STUDY_SUBJECT_CALENDAR_MANAGER);
        setCurrentUser(user);
    }

    protected void setCurrentUser(PscUser u) {
        PscGuard.setCurrentAuthenticationToken(request, new UsernamePasswordAuthenticationToken(
            u, "dc", u.getAuthorities()));
    }

    @Deprecated
    protected void setLegacyCurrentUser(edu.northwestern.bioinformatics.studycalendar.domain.User u) {
        User csmUser = new User();
        csmUser.setLoginName(u.getName());
        setCurrentUser(new PscUser(csmUser, null, u));
    }

    protected PscUser getCurrentUser() {
        return (PscUser) PscGuard.getCurrentAuthenticationToken(request).getPrincipal();
    }

    @Deprecated
    protected edu.northwestern.bioinformatics.studycalendar.domain.User getLegacyCurrentUser() {
        return getCurrentUser().getLegacyUser();
    }

    @Override
    protected final R createResource() {
        return createAuthorizedResource();
    }

    protected abstract R createAuthorizedResource();

    protected void assertLegacyRolesAllowedForMethod(Method method, Role... roles) {
        doInitOnly();

        Collection<Role> expected = Arrays.asList(roles);
        Collection<Role> actual = getResource().legacyAuthorizedRoles(method);
        // if authorizedRoles == null, that means everything is allowed
        if (actual == null) actual = Arrays.asList(Role.values());

        for (Role role : expected) {
            assertTrue(method.toString() + " for " + role.getDisplayName() + " should be allowed",
                actual.contains(role));
        }

        for (Role role : actual) {
            assertTrue(method.toString() + " for " + role.getDisplayName() + " should not be allowed", expected.contains(role));
        }
    }

    protected void assertRolesAllowedForMethod(Method method, PscRole... roles) {
        doInitOnly();

        Collection<ResourceAuthorization> resourceAuthorizations = getResource().authorizations(method);
        Collection<PscRole> actual = new ArrayList<PscRole>();
        if (resourceAuthorizations == null) {
            // if authorizations == null, that means everything is allowed
            actual = Arrays.asList(PscRole.values());
        } else {
            for (ResourceAuthorization actualResourceAuthorization : resourceAuthorizations) {
                actual.add(actualResourceAuthorization.getRole());
            }
        }

        for (PscRole role : roles) {
            assertTrue(method.toString() + " for " + role.getDisplayName() + " should be allowed",
                actual.contains(role));
        }

        for (PscRole role : actual) {
            assertTrue(method.toString() + " for " + role.getDisplayName() + " should not be allowed", Arrays.asList(roles).contains(role));
        }
    }

    protected void assertAllLegacyRolesAllowedForMethod(Method method) {
        assertTrue("All roles should be allowed", getResource().legacyAuthorizedRoles(method).isEmpty());
    }
}
