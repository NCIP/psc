package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.restlet.data.Method;
import org.restlet.resource.Resource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * @author John Dzak
 */
public abstract class AuthorizedResourceTestCase<R extends Resource & AuthorizedResource> extends ResourceTestCase<R> {
    private PscUser user;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        User csmUser = new User();
        csmUser.setLoginName("josephine");
        user = new PscUser(
            csmUser, Collections.singletonMap(
                SuiteRole.STUDY_SUBJECT_CALENDAR_MANAGER,
                new SuiteRoleMembership(SuiteRole.STUDY_SUBJECT_CALENDAR_MANAGER, null, null)
            ),
            Fixtures.createUser("josephine")
        );
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

    protected void assertAllLegacyRolesAllowedForMethod(Method method) {
        assertTrue("All roles should be allowed", getResource().legacyAuthorizedRoles(method).isEmpty());
    }
}
