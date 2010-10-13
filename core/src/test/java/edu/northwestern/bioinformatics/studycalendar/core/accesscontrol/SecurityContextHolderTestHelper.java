package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationScopeMappings.createSuiteRoleMembership;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.createPscUser;

public class SecurityContextHolderTestHelper {
    public static SuiteRoleMembership setUserAndReturnMembership(String username, PscRole role) {
        PscUser user = createPscUser(username, createSuiteRoleMembership(role));
        SecurityContextHolderTestHelper.setSecurityContext(user);
        return user.getMembership(role);
    }

    public static void setSecurityContext(PscUser principal, String password) {
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
            principal, password, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authRequest);
    }

    public static void setSecurityContext(PscUser principal) {
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
            principal, "hmsbadpass", principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authRequest);
    }
}
