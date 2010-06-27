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

    public void testAuthoritiesArePscRolesInLegacyMode() throws Exception {
        Fixtures.setUserRoles(legacyUser, Role.STUDY_ADMIN, Role.STUDY_COORDINATOR);
        GrantedAuthority[] actual = createLegacy().getAuthorities();
        assertEquals("Wrong number of authorities", 2, actual.length);
        assertEquals("Wrong 1st entry", Role.STUDY_ADMIN, actual[0]);
        assertEquals("Wrong 2nd entry", Role.STUDY_COORDINATOR, actual[1]);
    }

    public void testAuthoritiesAreSuiteRoles() throws Exception {
        Fixtures.setUserRoles(legacyUser, Role.STUDY_ADMIN, Role.STUDY_COORDINATOR);
        GrantedAuthority[] actual = create(
            createMembership(SuiteRole.STUDY_QA_MANAGER),
            createMembership(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER)
        ).getAuthorities();
        assertEquals("Wrong number of authorities", 2, actual.length);
        assertEquals("Wrong 1st entry", "study_qa_manager", actual[0].getAuthority());
        assertEquals("Wrong 2nd entry", "study_calendar_template_builder", actual[1].getAuthority());
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

    public void testToStringIsUsername() throws Exception {
        assertEquals("jo", create().toString());
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
