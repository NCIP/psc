package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.providers.AuthenticationProvider;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;

/**
 * This authentication system allows both GUI-interactive and token-based authentication
 * using only the username.
 * <p>
 * This is obviously 100% insecure, so it should only be used for testing and as an example.
 *
 * @author Rhett Sutphin
 */
public class InsecureAuthenticationSystem extends UsernameAndPasswordAuthenticationSystem {
    private static final ConfigurationProperties PROPERTIES = ConfigurationProperties.empty();

    public ConfigurationProperties configurationProperties() {
        return PROPERTIES;
    }

    @Override
    protected AuthenticationManager createAuthenticationManager() {
        UserDetailsService service = (UserDetailsService) getApplicationContext()
            .getBean("pscUserDetailsService");
        AuthenticationProvider insecure = new UsernameAssertedAuthenticationProvider(service);

        return AuthenticationSystemTools.createProviderManager(getApplicationContext(), insecure);
    }

    @Override
    public Authentication createTokenAuthenticationRequest(String token) {
        return new UsernamePasswordAuthenticationToken(token, null);
    }

    private static class UsernameAssertedAuthenticationProvider implements AuthenticationProvider {
        private UserDetailsService userDetailsService;

        public UsernameAssertedAuthenticationProvider(UserDetailsService userDetailsService) {
            this.userDetailsService = userDetailsService;
        }

        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            UserDetails user = userDetailsService.loadUserByUsername(authentication.getPrincipal().toString());
            return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        }

        @SuppressWarnings({ "RawUseOfParameterizedType" })
        public boolean supports(Class authentication) {
            return Authentication.class.isAssignableFrom(authentication);
        }
    }
}