/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.authorization;

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

import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.*;

/**
 * @author Rhett Sutphin
 */
public class PscUserTest extends TestCase {
    private PscUser a, b;
    private User csmUser;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        csmUser = new User();
        csmUser.setLoginName("jo");
        csmUser.setUpdateDate(new Date());

        a = AuthorizationObjectFactory.createPscUser("a");
        a.getCsmUser().setFirstName("Aeneas");
        a.getCsmUser().setLastName("Miller");
        b = AuthorizationObjectFactory.createPscUser("b");
        b.getCsmUser().setFirstName("Bacchus");
        b.getCsmUser().setLastName("Agricola");
    }

    public void testAuthoritiesArePscRoles() throws Exception {
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
        assertTrue(create(createMembership(SuiteRole.DATA_READER)).hasRole(PscRole.DATA_READER));
    }

    public void testHasRoleWhenDoesNotHave() throws Exception {
        assertFalse(create(createMembership(SuiteRole.DATA_READER)).hasRole(PscRole.SYSTEM_ADMINISTRATOR));
    }

    public void testAuthoritiesDoNotIncludeSuiteRolesWhichAreNotUsedInPsc() throws Exception {
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

    public void testPrincipalNameIsUsername() throws Exception {
        assertEquals("jo", create().getName());
    }

    public void testToStringIsUsername() throws Exception {
        assertEquals("jo", create().toString());
    }

    public void testIsActiveWithNoEndDate() throws Exception {
        csmUser.setEndDate(null);
        assertTrue(create().isActive());
    }

    public void testIsActiveWithFutureEndDate() throws Exception {
        csmUser.setEndDate(DateTools.createDate(
            Calendar.getInstance().get(Calendar.YEAR) + 1, 7, 11));
        assertTrue(create().isActive());
    }

    public void testIsNotActiveWithPastEndDate() throws Exception {
        csmUser.setEndDate(DateTools.createDate(2004, 7, 11));
        assertFalse(create().isActive());
    }

    ////// displayName

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

    public void testLastFirstWithUsernameOnly() throws Exception {
        csmUser.setFirstName(".");
        csmUser.setLastName("\t");
        assertEquals("jo", create().getLastFirst());
    }

    public void testLastFirstWithFirstAndLastName() throws Exception {
        csmUser.setFirstName("Josephine");
        csmUser.setLastName("Miller");

        assertEquals("Miller, Josephine", create().getLastFirst());
    }

    public void testLastFirstWithFirstNameOnly() throws Exception {
        csmUser.setFirstName("Josephine");
        csmUser.setLastName("\n");

        assertEquals("Josephine", create().getLastFirst());
    }

    public void testLastFirstWithLastNameOnly() throws Exception {
        csmUser.setFirstName(null);
        csmUser.setLastName("Miller");

        assertEquals("Miller", create().getLastFirst());
    }

    ////// NATURAL ORDER

    public void testNaturalOrderIsByLastFirst() throws Exception {
        System.out.println("a.getLastFirst() = " + a.getLastFirst());
        System.out.println("b.getLastFirst() = " + b.getLastFirst());
        assertNaturalOrder(b, a);
    }

    public void testNaturalOrderIsByFirstNameSecond() throws Exception {
        a.getCsmUser().setLastName(null);
        b.getCsmUser().setLastName(null);

        assertNaturalOrder(a, b);
    }

    public void testNaturalOrderComparesUsernameForUserWithNoLastFirst() throws Exception {
        a.getCsmUser().setLastName(null);
        a.getCsmUser().setFirstName(null);

        assertNaturalOrder(a, b);
    }

    public void testNaturalOrderIsEqualWhenEqual() throws Exception {
        assertEquals(0, a.compareTo(a));
        assertEquals(0, b.compareTo(b));
    }

    public void assertNaturalOrder(PscUser first, PscUser second) {
        assertNegative(first.compareTo(second));
        assertPositive(second.compareTo(first));
    }

    ////// HELPERS

    private PscUser create(SuiteRoleMembership... memberships) {
        return new PscUser(csmUser, createMembershipMap(memberships));
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
