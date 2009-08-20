package edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.direct;

import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.GrantedAuthority;

/**
 * @author Rhett Sutphin
 */
public class CasDirectUsernamePasswordAuthenticationToken extends UsernamePasswordAuthenticationToken {
    public CasDirectUsernamePasswordAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }

    public CasDirectUsernamePasswordAuthenticationToken(
        Object principal, Object credentials, GrantedAuthority[] grantedAuthorities
    ) {
        super(principal, credentials, grantedAuthorities);
    }
}
