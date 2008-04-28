package edu.northwestern.bioinformatics.studycalendar.security.plugin;

/**
 * @author Rhett Sutphin
 */
public class WebSSOAuthenticationSystem extends CasAuthenticationSystem {
    @Override
    protected String getPopulatorBeanName() {
        return "cctsAuthoritiesPopulator";
    }
}
