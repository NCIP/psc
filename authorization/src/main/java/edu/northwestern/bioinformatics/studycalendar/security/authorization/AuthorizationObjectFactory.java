package edu.northwestern.bioinformatics.studycalendar.security.authorization;

import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.security.authorization.domainobjects.User;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides helpers for creating various transient CSM and PSC authorization-related objects.
 *
 * @author Rhett Sutphin
 */
public class AuthorizationObjectFactory {
    public static PscUser createPscUser() {
        return createPscUser((String) null);
    }

    public static PscUser createPscUser(String username) {
        return createPscUser(username, new SuiteRoleMembership[0]);
    }

    public static PscUser createPscUser(String username, PscRole... roles) {
        return new PscUser(createCsmUser(username), indexSuiteRoleMemberships(createSuiteRoleMemberships(roles)));
    }

    public static PscUser createPscUser(String username, SuiteRoleMembership... memberships) {
        return new PscUser(createCsmUser(username), indexSuiteRoleMemberships(memberships));
    }

    @Deprecated
    public static PscUser createLegacyPscUser(String username, PscRole... roles) {
        edu.northwestern.bioinformatics.studycalendar.domain.User user = new edu.northwestern.bioinformatics.studycalendar.domain.User();
        user.setName(username);
        return new PscUser(createCsmUser(username), indexSuiteRoleMemberships(createSuiteRoleMemberships(roles)), user);
    }

    public static PscUser createPscUser(User csmUser) {
        return new PscUser(csmUser, Collections.<SuiteRole, SuiteRoleMembership>emptyMap());
    }

    public static User createCsmUser(String username) {
        User csmUser = new User();
        csmUser.setLoginName(username);
        csmUser.setUpdateDate(new Date()); // Or else CSM pukes
        return csmUser;
    }

    private static SuiteRoleMembership[] createSuiteRoleMemberships(PscRole... roles) {
        SuiteRoleMembership[] memberships = new SuiteRoleMembership[roles.length];
        for (int i = 0; i < roles.length; i++) {
            PscRole role = roles[i];
            SuiteRoleMembership mem = new SuiteRoleMembership(role.getSuiteRole(), null, null);
            if (role.isSiteScoped()) mem.forAllSites();
            if (role.isStudyScoped()) mem.forAllStudies();
            memberships[i] = mem;
        }
        return memberships;
    }

    private static Map<SuiteRole, SuiteRoleMembership> indexSuiteRoleMemberships(SuiteRoleMembership... memberships) {
        Map<SuiteRole, SuiteRoleMembership> membershipMap = new LinkedHashMap<SuiteRole, SuiteRoleMembership>();
        for (SuiteRoleMembership m : memberships) {
            membershipMap.put(m.getRole(), m);
        }
        return membershipMap;
    }

    // static class
    private AuthorizationObjectFactory() { }
}
