package edu.northwestern.bioinformatics.studycalendar.security.plugin.websso;

import edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.CasAuthenticationSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class WebSSOAuthenticationSystem extends CasAuthenticationSystem {
    @Override
    public String name() {
        return "caGrid WebSSO";
    }

    @Override
    public String behaviorDescription() {
        return "delegates authentication to a caGrid WebSSO server (use this option for CCTS)";
    }

    @Override
    protected List<String> applicationContextResourceNames() {
        List<String> names = new ArrayList<String>();
        names.add("websso-authentication-beans.xml");
        names.addAll(super.applicationContextResourceNames());
        return names;
    }

    @Override
    protected String getPopulatorBeanName() {
        return "cctsAuthoritiesPopulator";
    }

    @Override
    protected String getTicketValidatorBeanName() {
        return "cctsCasProxyTicketValidator";
    }
}
