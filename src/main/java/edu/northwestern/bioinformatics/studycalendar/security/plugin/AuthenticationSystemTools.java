package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.providers.AuthenticationProvider;
import org.acegisecurity.providers.ProviderManager;
import org.acegisecurity.providers.anonymous.AnonymousAuthenticationProvider;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import static edu.northwestern.bioinformatics.studycalendar.tools.spring.SpringBeanConfigurationTools.*;

/**
 * @author Rhett Sutphin
 */
public class AuthenticationSystemTools {
    public static AuthenticationManager createProviderManager(
        ApplicationContext parent, AuthenticationProvider provider
    ) {
        return createProviderManager(parent, Arrays.asList(provider));
    }

    public static AuthenticationManager createProviderManager(
        ApplicationContext parent, List<AuthenticationProvider> providers
    ) {
        List<AuthenticationProvider> providersPlusAnonymous
            = new ArrayList<AuthenticationProvider>(providers.size() + 1);
        providersPlusAnonymous.addAll(providers);
        providersPlusAnonymous.add(createAnonymousAuthenticationProvider(parent));

        ProviderManager manager = new ProviderManager();
        manager.setProviders(providersPlusAnonymous);

        return prepareBean(parent, manager);
    }

    private static AnonymousAuthenticationProvider createAnonymousAuthenticationProvider(ApplicationContext parent) {
        AnonymousAuthenticationProvider provider = new AnonymousAuthenticationProvider();
        provider.setKey("PSC_ANON");
        return prepareBean(parent, provider);
    }
}
