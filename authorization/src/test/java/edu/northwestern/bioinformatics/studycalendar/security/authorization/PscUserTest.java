package edu.northwestern.bioinformatics.studycalendar.security.authorization;

import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.security.authorization.domainobjects.User;
import junit.framework.TestCase;
import org.acegisecurity.GrantedAuthority;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class PscUserTest extends TestCase {
    private User csmUser;
    private edu.northwestern.bioinformatics.studycalendar.domain.User legacyUser;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        csmUser = new User();
        csmUser.setLoginName("jo");
        csmUser.setUpdateDate(new Date());

        legacyUser = new edu.northwestern.bioinformatics.studycalendar.domain.User();
        legacyUser.setName(csmUser.getLoginName());
    }

    public void testAuthoritiesAreInternalRolesInLegacyMode() throws Exception {
        Fixtures.setUserRoles(legacyUser, Role.STUDY_ADMIN, Role.STUDY_COORDINATOR);
        GrantedAuthority[] actual = createLegacy().getAuthorities();
        assertEquals("Wrong number of authorities", 2, actual.length);
        assertEquals("Wrong 1st entry", Role.STUDY_ADMIN, actual[0]);
        assertEquals("Wrong 2nd entry", Role.STUDY_COORDINATOR, actual[1]);
    }

    public void testHasRoleWhenHasInLegacyMode() throws Exception {
        Fixtures.setUserRoles(legacyUser, Role.STUDY_ADMIN, Role.STUDY_COORDINATOR);
        assertTrue(createLegacy().hasRole(Role.STUDY_COORDINATOR));
    }

    public void testHasRoleWhenDoesNotHaveInLegacyMode() throws Exception {
        Fixtures.setUserRoles(legacyUser, Role.STUDY_ADMIN, Role.STUDY_COORDINATOR);
        assertFalse(createLegacy().hasRole(Role.SYSTEM_ADMINISTRATOR));
    }

    public void testAuthoritiesArePscRoles() throws Exception {
        Fixtures.setUserRoles(legacyUser, Role.STUDY_ADMIN, Role.STUDY_COORDINATOR);
        GrantedAuthority[] actual = create(
            createMembership(SuiteRole.STUDY_QA_MANAGER),
            createMembership(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER)
        ).getAuthorities();
        assertEquals("Wrong number of authorities", 2, actual.length);
        assertEquals("Wrong 1st entry", "study_qa_manager", actual[0].getAuthority());
        assertEquals("1st entry not a PscRole", PscRole.class, actual[0].getClass());
        assertEquals("Wrong 2nd entry", "study_calendar_template_builder", actual[1].getAuthority());
        assertEquals("2nd entry not a PscRole", PscRole.class, actual[1].getClass());
    }

    public void testHasRoleWhenHas() throws Exception {
        Fixtures.setUserRoles(legacyUser, Role.STUDY_ADMIN, Role.STUDY_COORDINATOR);
        assertTrue(create(createMembership(SuiteRole.DATA_READER)).hasRole(PscRole.DATA_READER));
    }

    public void testHasRoleWhenDoesNotHave() throws Exception {
        Fixtures.setUserRoles(legacyUser, Role.STUDY_ADMIN, Role.STUDY_COORDINATOR);
        assertFalse(create(createMembership(SuiteRole.DATA_READER)).hasRole(PscRole.SYSTEM_ADMINISTRATOR));
    }

    public void testAuthoritiesDoNotIncludeSuiteRolesWhichAreNotUsedInPsc() throws Exception {
        Fixtures.setUserRoles(legacyUser, Role.STUDY_ADMIN, Role.STUDY_COORDINATOR);
        GrantedAuthority[] actual = create(
            createMembership(SuiteRole.DATA_ANALYST)
        ).getAuthorities();
        assertEquals("Wrong number of authorities", 0, actual.length);
    }

    public void testIsExpiredIfEndDateInPast() throws Exception {
        csmUser.setEndDate(DateTools.createDate(2003, Calendar.APRIL, 6));
        assertFalse(create().isAccountNonExpired());
    }

    public void testIsNotExpiredWithNoEndDate() throws Exception {
        csmUser.setEndDate(null);
        assertTrue(create().isAccountNonExpired());
    }

    public void testSetAttributeIsRetrievable() throws Exception {
        PscUser u = create();
        u.setAttribute("a", 42L);
        assertEquals("Not retrievable", 42L, u.getAttribute("a"));
    }

    public void testUnsetAttributeReturnsNull() throws Exception {
        assertNull("Not retrievable", create().getAttribute("a"));
    }

    public void testIsNotExpiredWithFutureEndDate() throws Exception {
        Calendar future = Calendar.getInstance();
        future.add(Calendar.DAY_OF_YEAR, 1);
        csmUser.setEndDate(future.getTime());
        assertTrue(create().isAccountNonExpired());
    }

    public void testGetMembershipByPscRole() throws Exception {
        SuiteRoleMembership actual = create(
            createMembership(SuiteRole.AE_REPORTER),
            createMembership(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER)
        ).getMembership(PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER);

        assertNotNull("Not found", actual);
        assertEquals(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER, actual.getRole());
    }

    public void testGetNonExistentMembershipByPscRoleIsNull() throws Exception {
        SuiteRoleMembership actual = create(
            createMembership(SuiteRole.AE_REPORTER),
            createMembership(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER)
        ).getMembership(PscRole.USER_ADMINISTRATOR);

        assertNull("Found", actual);
    }

    public void testToStringIsUsername() throws Exception {
        assertEquals("jo", create().toString());
    }

    public void testUserDisplayNameWithUsernameOnly() throws Exception {
        csmUser.setFirstName(".");
        csmUser.setLastName("\t");
        assertEquals("jo", create().getDisplayName());
    }

    public void testUserDisplayNameWithFirstLastName() throws Exception {
        csmUser.setFirstName("Josephine");
        csmUser.setLastName("Miller");

        assertEquals("Josephine Miller", create().getDisplayName());
    }

    public void testUserDisplayNameWithFirstNameOnly() throws Exception {
        csmUser.setFirstName("Josephine");
        csmUser.setLastName("\n");

        assertEquals("Josephine", create().getDisplayName());
    }

    public void testUserDisplayNameWithLastNameOnly() throws Exception {
        csmUser.setFirstName(null);
        csmUser.setLastName("Miller");

        assertEquals("Miller", create().getDisplayName());
    }

    private PscUser create(SuiteRoleMembership... memberships) {
        return new PscUser(csmUser, createMembershipMap(memberships), null);
    }

    private PscUser createLegacy(SuiteRoleMembership... memberships) {
        return new PscUser(csmUser, createMembershipMap(memberships), legacyUser);
    }

    private SuiteRoleMembership createMembership(SuiteRole suiteRole) {
        return new SuiteRoleMembership(suiteRole, null, null);
    }

    private Map<SuiteRole, SuiteRoleMembership> createMembershipMap(SuiteRoleMembership[] memberships) {
        Map<SuiteRole, SuiteRoleMembership> map = new LinkedHashMap<SuiteRole, SuiteRoleMembership>();
        for (SuiteRoleMembership membership : memberships) {
            map.put(membership.getRole(), membership);
        }
        return map;
    }
}
