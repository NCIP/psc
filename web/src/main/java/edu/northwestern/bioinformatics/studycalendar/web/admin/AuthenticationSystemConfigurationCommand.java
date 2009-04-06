package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarUserException;
import edu.northwestern.bioinformatics.studycalendar.security.AuthenticationSystemConfiguration;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.Errors;

/**
 * @author Rhett Sutphin
 */
public class AuthenticationSystemConfigurationCommand implements Validatable {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private AuthenticationSystemConfiguration liveConfiguration;
    private AuthenticationSystemConfiguration workConfiguration;
    private BindableConfiguration conf;

    public AuthenticationSystemConfigurationCommand(String newAuthenticationSystem, AuthenticationSystemConfiguration liveConfiguration, ApplicationContext applicationContext) {
        /*
        this.liveConfiguration = liveConfiguration;
        workConfiguration = new AuthenticationSystemConfiguration();
        workConfiguration.setDelegate(
            TransientConfiguration.create(liveConfiguration));

        String oldAuthenticationSystem = workConfiguration.get(AUTHENTICATION_SYSTEM);
        try {
            if (newAuthenticationSystem != null) {
                workConfiguration.set(AUTHENTICATION_SYSTEM, newAuthenticationSystem);
                log.debug("Updated properties: {}", workConfiguration.getProperties().getAll());
                // update delegate with new properties
                workConfiguration.setDelegate(
                    TransientConfiguration.create(liveConfiguration, workConfiguration.getProperties()));

                workConfiguration.getAuthenticationSystem(); // attempt to load
            }
        } catch (AuthenticationSystemLoadingFailure failure) {
            // reset back to original so that binding can proceed
            workConfiguration.set(AUTHENTICATION_SYSTEM, oldAuthenticationSystem);
        } catch (StudyCalendarUserException scue) {
            // ignore the error -- it will be re-raised during validation
        }

        conf = new BindableConfiguration(workConfiguration, true);
        */
    }

    public void validate(Errors errors) {
        try {
            workConfiguration.getAuthenticationSystem();
        } catch (StudyCalendarUserException scve) {
            scve.rejectInto(errors);
        }
    }

    public void apply() {
        /*
        for (ConfigurationProperty<?> property : workConfiguration.getProperties().getAll()) {
            if (workConfiguration.isSet(property)) {
                liveConfiguration.set(
                    (ConfigurationProperty<Object>) property, workConfiguration.get(property));
            } else {
                liveConfiguration.reset(property);
            }
        }
        */
    }

    public AuthenticationSystemConfiguration getWorkConfiguration() {
        return workConfiguration;
    }

    ////// BOUND PROPERTIES

    public BindableConfiguration getConf() {
        return conf;
    }
/*
    public String getCustomAuthenticationSystemClass() {
        if (workConfiguration.isCustomAuthenticationSystem()) {
            return workConfiguration.get(AUTHENTICATION_SYSTEM);
        } else {
            return null;
        }
    }

    public void setCustomAuthenticationSystemClass(String customAuthenticationSystemClass) {
        if (!StringUtils.isBlank(customAuthenticationSystemClass)) {
            workConfiguration.set(AUTHENTICATION_SYSTEM, customAuthenticationSystemClass);
        }
    }

    ////// OBJECT METHODS

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).append("[conf=").append(getConf())
            .append(", customAuthenticationSystemClass=")
            .append(getCustomAuthenticationSystemClass())
            .append(']').toString();
    }
*/
}
