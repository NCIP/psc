package edu.northwestern.bioinformatics.studycalendar.security.authorization;

import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.userdetails.UserDetails;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class PscUser implements UserDetails {
    private User user;
    private Map<SuiteRole, SuiteRoleMembership> memberships;
    private edu.northwestern.bioinformatics.studycalendar.domain.User legacyUser;

    private Map<String, Object> attributes;

    /** Default constructor is for proxying only. */
    public PscUser() {
        this(null, null);
    }

    public PscUser(
        User user, Map<SuiteRole, SuiteRoleMembership> memberships
    ) {
        this.user = user;
        this.memberships = memberships;

        this.attributes = new LinkedHashMap<String, Object>();
    }

    @Deprecated
    public PscUser(
        User user, Map<SuiteRole, SuiteRoleMembership> memberships,
        edu.northwestern.bioinformatics.studycalendar.domain.User legacyUser
    ) {
        this(user, memberships);
        this.legacyUser = legacyUser;
    }

    @Deprecated
    public edu.northwestern.bioinformatics.studycalendar.domain.User getLegacyUser() {
        return legacyUser;
    }

    public Map<SuiteRole, SuiteRoleMembership> getMemberships() {
        return memberships;
    }

    private boolean legacyMode() {
        return this.legacyUser != null;
    }

    ////// ARBITRARY ATTRIBUTES

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    ////// IMPLEMENTATION OF UserDetails

    public GrantedAuthority[] getAuthorities() {
        if (legacyMode()) {
            return legacyUser.getAuthorities();
        } else {
            GrantedAuthority[] authorities = new GrantedAuthority[memberships.size()];
            int i = 0;
            for (SuiteRole suiteRole : memberships.keySet()) {
                authorities[i] = new GrantedAuthorityImpl(suiteRole.getCsmName());
                i++;
            }
            return authorities;
        }
    }

    public String getPassword() {
        return "PASSWORD NOT AVAILABLE FOR " + getClass().getName();
    }

    public String getUsername() {
        return user.getLoginName();
    }

    public boolean isAccountNonExpired() {
        return user.getEndDate() == null || new Date().compareTo(user.getEndDate()) < 0;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return isAccountNonExpired();
    }

    ////// OBJECT METHODS

    @Override
    // This behavior is necessary so that this class can be a Principal
    public String toString() {
        return getUsername();
    }
}
