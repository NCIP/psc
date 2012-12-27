/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin.cas;

import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationTestCase;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;

/**
 * @author Rhett Sutphin
 */
public abstract class CasBasedAuthenticationSystemTestCase extends AuthenticationTestCase {
    protected static final String EXPECTED_SERVICE_URL = "http://etc:5443/cas";
    protected static final String EXPECTED_APP_URL = "http://psc.etc/";
    protected Configuration configuration;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        configuration = blankConfiguration();
    }

    protected void doValidInitialize() {
        configuration.set(CasAuthenticationSystem.SERVICE_URL, EXPECTED_SERVICE_URL);
        configuration.set(CasAuthenticationSystem.APPLICATION_URL, EXPECTED_APP_URL);
        getSystem().initialize(configuration);
    }

    public abstract CasAuthenticationSystem getSystem();
}
