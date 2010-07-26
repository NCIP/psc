package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;

import static edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.AuthorizationScopeMappings.*;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.*;

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

    @Deprecated
    public static void setSecurityContext(
        edu.northwestern.bioinformatics.studycalendar.domain.User principal, String password
    ) {
        User csmUser = new User();
        csmUser.setLoginName(principal.getName());
        setSecurityContext(new PscUser(csmUser, null, principal), password);
    }
}
