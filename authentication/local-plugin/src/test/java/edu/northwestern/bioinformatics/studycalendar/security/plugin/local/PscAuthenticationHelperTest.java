package edu.northwestern.bioinformatics.studycalendar.security.plugin.local;

import edu.northwestern.bioinformatics.studycalendar.database.StudyCalendarDbTestCase;
import javax.sql.DataSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.acegisecurity.Authentication;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;

/**
 * @author Jalpa Patel
 */
public class PscAuthenticationHelperTest extends StudyCalendarDbTestCase {
    private PscAuthenticationHelper pscAuthenticationHelper;

    protected DataSource getDataSource() {
        return (DataSource) new ClassPathXmlApplicationContext("test-datasource-context.xml", getClass()).getBean("dataSource");
    }

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

    public void testAuthenticateWithDisableUser() throws Exception {
       Authentication authentication = new UsernamePasswordAuthenticationToken("study_admin","study_admin1");
       assertFalse("Enable User",pscAuthenticationHelper.authenticate(authentication));
    }
}
