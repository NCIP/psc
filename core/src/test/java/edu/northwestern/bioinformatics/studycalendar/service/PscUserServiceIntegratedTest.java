package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembershipLoader;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class PscUserServiceIntegratedTest extends DaoTestCase {
    private PscUserService service;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        service = (PscUserService) getApplicationContext().getBean("pscUserService");
    }

    public void testUserLoading() throws Exception {
        PscUser loaded = service.loadUserByUsername("joe2061");
        assertNotNull(loaded);
    }

    public void testSearchMatchesUsernameCaseSensitively() throws Exception {
        assertSearchWithMatches("oe2", "joe2061");
    }

    public void testSearchMatchesFirstNameCaseSensitively() throws Exception {
        assertSearchWithMatches("Mai", "jv2008");
    }

    public void testSearchMatchesLastNameCaseInsensitively() throws Exception {
        assertSearchWithMatches("oo", "joe2061");
    }

    public void testSearchDoesNotDuplicateUsersWhichMatchOnMultipleCriteria() throws Exception {
        assertSearchWithMatches("joe", "joe2061");
    }

    public void testSearchReturnsUsersInOrderByLastName() throws Exception {
        assertSearchWithMatches("a", "jv2008", "joe2061");
    }

    public void testSearchReturnsAllUsersForNull() throws Exception {
        assertSearchWithMatches(null, "jv2008", "joe2061");
    }

    private List<User> assertSearchWithMatches(String searchText, String... expectedUsernames) {
        List<User> actual = service.search(searchText);
        assertEquals("Wrong number of users returned: " + actual,
            expectedUsernames.length, actual.size());
        for (int i = 0; i < expectedUsernames.length; i++) {
            String expected = expectedUsernames[i];
            assertEquals("Wrong user at " + i, expected, actual.get(i).getLoginName());
        }
        return actual;
    }
}
