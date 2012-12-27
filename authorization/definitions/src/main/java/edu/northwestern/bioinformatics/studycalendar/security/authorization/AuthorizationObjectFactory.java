/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.authorization;

import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.security.authorization.domainobjects.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
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

    // this explicit no-varags variant prevents a conflict between the other two
    public static PscUser createPscUser(String username) {
        return createPscUser(username, (Long) null);
    }

    public static PscUser createPscUser(String username, Long csmUserId) {
        return createPscUser(username, csmUserId, new SuiteRoleMembership[0]);
    }

    public static PscUser createPscUser(String username, PscRole... roles) {
        return createPscUser(username, null, roles);
    }

    public static PscUser createPscUser(String username, Long csmUserId, PscRole... roles) {
        return createPscUser(username, csmUserId, createSuiteRoleMemberships(roles));
    }

    public static PscUser createPscUser(String username, SuiteRoleMembership... memberships) {
        return createPscUser(username, null, memberships);
    }

    public static PscUser createPscUser(String username, Long csmUserId, SuiteRoleMembership... memberships) {
        return new PscUser(createCsmUser(csmUserId, username), indexSuiteRoleMemberships(memberships));
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

   public static User createCsmUser(int id, String username) {
        return createCsmUser((long) id, username);
    }

    public static User createCsmUser(Long id, String username) {
        User csmUser = new User();
        csmUser.setUserId(id);
        csmUser.setLoginName(username);
        csmUser.setUpdateDate(new Date()); // because otherwise toString NPEs
        return csmUser;
    }

    private static SuiteRoleMembership[] createSuiteRoleMemberships(PscRole... roles) {
        List<SuiteRoleMembership> memberships = new ArrayList<SuiteRoleMembership>(roles.length);
        for (PscRole role : roles) {
            SuiteRoleMembership mem = new SuiteRoleMembership(role.getSuiteRole(), null, null);
            if (mem.getRole().isSiteScoped()) mem.forAllSites();
            if (mem.getRole().isStudyScoped()) mem.forAllStudies();
            memberships.add(mem);
        }
        return memberships.toArray(new SuiteRoleMembership[memberships.size()]);
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
