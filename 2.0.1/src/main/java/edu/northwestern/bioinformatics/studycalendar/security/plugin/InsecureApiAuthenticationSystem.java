package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.providers.AuthenticationProvider;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.providers.AbstractAuthenticationToken;

import gov.nih.nci.security.acegi.csm.authentication.CSMAuthenticationProvider;

import java.util.Arrays;

/**
 * This authentication system behaves mostly like {@link LocalAuthenticationSystem},
 * except that it permits token-based API authentication using the username as the token.
 * This is obviously 100% insecure, so it should only be used for testing and as an example.
 *
 * @author Rhett Sutphin
 */
public class InsecureApiAuthenticationSystem extends LocalAuthenticationSystem {

    @Override
    protected AuthenticationManager createAuthenticationManager() {
        AuthenticationProvider csm = (CSMAuthenticationProvider) getApplicationContext()
            .getBean("csmAuthenticationProvider");
        UserDetailsService service = (UserDetailsService) getApplicationContext()
            .getBean("pscUserDetailsService");
        AuthenticationProvider insecure = new UsernameAssertedAuthenticationProvider(service);

        return AuthenticationSystemTools.createProviderManager(
            getApplicationContext(), Arrays.asList(csm, insecure));
    }

    @Override
    public Authentication createTokenAuthenticationRequest(String token) {
        return new UsernameAssertion(token);
    }

    private static class UsernameAssertion extends AbstractAuthenticationToken {
        private String username;

        public UsernameAssertion(String username) {
            this.username = username;
        }

        public Object getCredentials() {
            return username;
        }

        public Object getPrincipal() {
            return username;
        }
    }

    private static class UsernameAssertedAuthenticationProvider implements AuthenticationProvider {
        private UserDetailsService userDetailsService;

        public UsernameAssertedAuthenticationProvider(UserDetailsService userDetailsService) {
            this.userDetailsService = userDetailsService;
        }

        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            UserDetails user = userDetailsService.loadUserByUsername(authentication.getCredentials().toString());
            return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        }

        @SuppressWarnings({ "RawUseOfParameterizedType" })
        public boolean supports(Class authentication) {
            return UsernameAssertion.class.isAssignableFrom(authentication);
        }
    }
}
