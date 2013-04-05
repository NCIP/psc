/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin.local;

import edu.northwestern.bioinformatics.studycalendar.database.StudyCalendarDbTestCase;
import org.acegisecurity.Authentication;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;

import javax.sql.DataSource;

/**
 * @author Jalpa Patel
 */
public class PscAuthenticationHelperTest extends StudyCalendarDbTestCase {
    private PscAuthenticationHelper pscAuthenticationHelper;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        DataSource dataSource = getDataSource();
        pscAuthenticationHelper = new PscAuthenticationHelper();
        pscAuthenticationHelper.setDataSource(dataSource);
    }

    public void testAuthenticateWithValidUserCredentials() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken("superuser","superuser");
        assertTrue("Not valid User",pscAuthenticationHelper.authenticate(authentication));
    }

    public void testAuthenticateWithWrongUserName() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken("user","superuser");
        assertFalse("Valid Username",pscAuthenticationHelper.authenticate(authentication));
    }

    public void testAuthenticateWithWrongPassword() throws Exception {
       Authentication authentication = new UsernamePasswordAuthenticationToken("superuser","user");
       assertFalse("Valid Password",pscAuthenticationHelper.authenticate(authentication));
    }
}
