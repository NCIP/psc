package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.context.SecurityContextHolder;

public class SecurityContextHolderTestHelper {
    public static void setSecurityContext(String username, String password) {
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
        SecurityContextHolder.getContext().setAuthentication(authRequest);
    }
}
