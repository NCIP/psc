/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import static edu.northwestern.bioinformatics.studycalendar.tools.spring.SpringBeanConfigurationTools.*;
import edu.northwestern.bioinformatics.studycalendar.tools.spring.StringXmlApplicationContext;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.providers.AuthenticationProvider;
import org.acegisecurity.providers.ProviderManager;
import org.acegisecurity.providers.anonymous.AnonymousAuthenticationProvider;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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

    public static ApplicationContext createApplicationContextWithPropertiesBean(
        ApplicationContext parent, String beanName, Properties properties, ClassLoader beanClassLoader
    ) {
        StringBuilder propString = new StringBuilder();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            propString.append(String.format("  <prop key=\"%s\">%s</prop>\n", entry.getKey(), entry.getValue()));
        }
        String xml = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<beans xmlns=\"http://www.springframework.org/schema/beans\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "       xsi:schemaLocation=\"" +
            "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd\"\n" +
            "       >\n<bean id=\"%s\" class=\"%s\">\n<property name=\"properties\">\n<props>\n%s</props>\n</property>\n</bean>\n</beans>",
            beanName, PropertiesFactoryBean.class.getName(), propString);
        return new StringXmlApplicationContext(parent, xml, beanClassLoader);
    }

    private static AnonymousAuthenticationProvider createAnonymousAuthenticationProvider(ApplicationContext parent) {
        AnonymousAuthenticationProvider provider = new AnonymousAuthenticationProvider();
        provider.setKey("PSC_ANON");
        return prepareBean(parent, provider);
    }
}
