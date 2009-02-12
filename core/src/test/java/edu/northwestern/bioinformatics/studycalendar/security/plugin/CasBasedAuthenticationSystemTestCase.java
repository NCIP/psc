package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.tools.configuration.MockConfiguration;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import org.springframework.context.ApplicationContext;

/**
 * @author Rhett Sutphin
 */
public abstract class CasBasedAuthenticationSystemTestCase extends StudyCalendarTestCase {
    protected static final String EXPECTED_SERVICE_URL = "http://etc:5443/cas";
    protected static final String EXPECTED_APP_URL = "http://psc.etc/";
    protected Configuration configuration;
    protected ApplicationContext applicationContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        configuration = new MockConfiguration();
        applicationContext = getDeployedApplicationContext();
    }

    protected void doValidInitialize() {
        configuration.set(CasAuthenticationSystem.SERVICE_URL, EXPECTED_SERVICE_URL);
        configuration.set(CasAuthenticationSystem.APPLICATION_URL, EXPECTED_APP_URL);
        getSystem().initialize(applicationContext, configuration);
    }

    public abstract CasAuthenticationSystem getSystem();
}
