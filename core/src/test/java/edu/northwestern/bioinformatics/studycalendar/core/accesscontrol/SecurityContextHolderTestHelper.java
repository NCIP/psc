package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;

public class SecurityContextHolderTestHelper {
    public static void setSecurityContext(PscUser principal, String password) {
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
            principal, password, principal.getAuthorities());
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
