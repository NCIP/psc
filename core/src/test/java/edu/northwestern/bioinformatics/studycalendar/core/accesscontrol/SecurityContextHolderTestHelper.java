package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.context.SecurityContextHolder;
import edu.northwestern.bioinformatics.studycalendar.domain.User;

public class SecurityContextHolderTestHelper {
    public static void setSecurityContext(User principal, String password) {
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(principal, password);
        SecurityContextHolder.getContext().setAuthentication(authRequest);
    }
}
