package edu.northwestern.bioinformatics.studycalendar.domain;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.*;
import junit.framework.TestCase;
import org.acegisecurity.GrantedAuthority;

import java.util.Arrays;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class UserTest extends TestCase {
    private User user;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        user = createNamedInstance("joe", User.class);
    }

    public void testGetUserRole() throws Exception {
        UserRole userRole0 = createUserRole(user, Role.SUBJECT_COORDINATOR);
        UserRole userRole1 = createUserRole(user, Role.SITE_COORDINATOR);
        user.addUserRole(userRole0);
        user.addUserRole(userRole1);

        assertSame("Wrong user role", userRole0, user.getUserRole(Role.SUBJECT_COORDINATOR));
        assertSame("Wrong user role", userRole1, user.getUserRole(Role.SITE_COORDINATOR));
    }

    public void testHasUserRole() throws Exception {
        UserRole userRole0 = createUserRole(user, Role.SUBJECT_COORDINATOR);
        UserRole userRole1 = createUserRole(user, Role.SITE_COORDINATOR);
        user.addUserRole(userRole0);
        user.addUserRole(userRole1);

        assertTrue(user.hasRole(Role.SUBJECT_COORDINATOR));
        assertTrue(user.hasRole(Role.SITE_COORDINATOR));
        assertFalse(user.hasRole(Role.STUDY_ADMIN));
        assertFalse(user.hasRole(Role.STUDY_COORDINATOR));
        assertFalse(user.hasRole(Role.SYSTEM_ADMINISTRATOR));
    }
    
    public void testUserDetailsGrantedAuthorities() throws Exception {
        user.addUserRole(createUserRole(user,  Role.SITE_COORDINATOR));
        user.addUserRole(createUserRole(user,  Role.SYSTEM_ADMINISTRATOR));

        GrantedAuthority[] actual = user.getAuthorities();
        assertEquals(2, actual.length);
        assertContains(Arrays.asList(actual), Role.SITE_COORDINATOR);
        assertContains(Arrays.asList(actual), Role.SYSTEM_ADMINISTRATOR);
    }
    
    public void testUserDetailsEnabledTracksActiveFlag() throws Exception {
        user.setActiveFlag(false);
        assertFalse(user.isEnabled());
        user.setActiveFlag(true);
        assertTrue(user.isEnabled());
    }
    
    public void testUserToStringIsUsername() throws Exception {
        assertEquals("In order to be used as a Principal, user.toString must match the username",
            user.getName(), user.toString());
    }

    public void testUserDisplayNameWithUsernameOnly() throws Exception {
        assertEquals("In order to be used as a Principal, user.displayName must match the username",
            user.getDisplayName(), user.getName());
    }

    public void testUserDisplayNameWithFirstMiddleLastName() throws Exception {
        String firstName = "First";
        String middleName = "M";
        String lastName = "Last";
        user.setFirstName(firstName);
        user.setMiddleName(middleName);
        user.setLastName(lastName);
        String expectedDisplayName = firstName + " " + middleName+ " " + lastName;

        assertEquals("In order to be used as a Principal, user.displayName must match the first, last, and middle name",
            user.getDisplayName(), expectedDisplayName);
    }

    public void testUserDisplayNameWithFirstAndUserName() throws Exception {
        user.setFirstName("First");
        assertEquals("In order to be used as a Principal, user.displayName must match the first name",
            user.getDisplayName(), user.getFirstName()+" ");
    }

    public void testEqualityConsidersRoles() throws Exception {
        assertNotEquals(
            Fixtures.createUser("joe", Role.STUDY_ADMIN),
            Fixtures.createUser("joe", Role.SYSTEM_ADMINISTRATOR));
    }
    
    public void testEqualityConsidersSitesForRoles() throws Exception {
        User joeAtNu = Fixtures.createUser("joe", Role.SUBJECT_COORDINATOR);
        joeAtNu.getUserRole(Role.SUBJECT_COORDINATOR).addSite(Fixtures.createSite("NU"));
        User joeAtMayo = Fixtures.createUser("joe", Role.SUBJECT_COORDINATOR);
        joeAtMayo.getUserRole(Role.SUBJECT_COORDINATOR).addSite(Fixtures.createSite("Mayo"));

        assertNotEquals(joeAtNu, joeAtMayo);
    }

    public void testAllSitesIncludesSiteCoordSites() throws Exception {
        Site expectedSite = createSite("NU");
        createUserRole(user, Role.SITE_COORDINATOR, expectedSite);

        List<Site> actual = user.getAllSites();
        assertEquals("Wrong number of sites", 1, actual.size());
        assertEquals("Wrong site", "NU", actual.get(0).getName());
    }

    public void testAllSitesIncludesSitesFromStudySites() throws Exception {
        Site expectedSite = createSite("TJU");
        UserRole sc = createUserRole(user, Role.SUBJECT_COORDINATOR);
        sc.addStudySite(createStudySite(createReleasedTemplate(), expectedSite));

        List<Site> actual = user.getAllSites();
        assertEquals("Wrong number of sites", 1, actual.size());
        assertEquals("Wrong site", "TJU", actual.get(0).getName());
    }

    public void testAllSitesIncludesEachSiteOnlyOnce() throws Exception {
        Site nu = createSite("NU");
        Site tju = createSite("TJU");
        Study s = createReleasedTemplate();

        createUserRole(user, Role.SITE_COORDINATOR, nu);
        createUserRole(user, Role.SUBJECT_COORDINATOR, nu, tju);

        List<Site> actual = user.getAllSites();
        assertEquals("Wrong number of sites: " + actual, 2, actual.size());
        assertContains("Missing NU", actual, nu);
        assertContains("Missing TJU", actual, tju);
    }
}
