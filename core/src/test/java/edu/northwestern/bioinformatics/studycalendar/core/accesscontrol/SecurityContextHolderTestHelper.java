package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.context.SecurityContextHolder;

public class SecurityContextHolderTestHelper {
    public static void setSecurityContext(Object principal, String password) {
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(principal, password);
        SecurityContextHolder.getContext().setAuthentication(authRequest);
    }
}
