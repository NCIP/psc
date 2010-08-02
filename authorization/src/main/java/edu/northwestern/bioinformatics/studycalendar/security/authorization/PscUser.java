package edu.northwestern.bioinformatics.studycalendar.security.authorization;

import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class PscUser implements UserDetails {
    private User user;
    private Map<SuiteRole, SuiteRoleMembership> memberships;
    private VisibleStudyParameters visibleStudyParameters;
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

    public User getCsmUser() {
        return user;
    }

    public SuiteRoleMembership getMembership(PscRole pscRole) {
        return getMemberships().get(pscRole.getSuiteRole());
    }

    public Map<SuiteRole, SuiteRoleMembership> getMemberships() {
        return memberships;
    }

    public boolean hasRole(GrantedAuthority ga) {
        return Arrays.asList(getAuthorities()).contains(ga);
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
            List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(memberships.size());
            for (SuiteRole suiteRole : memberships.keySet()) {
                PscRole match = PscRole.valueOf(suiteRole);
                if (match != null) {
                    authorities.add(match);
                }
            }
            return authorities.toArray(new GrantedAuthority[authorities.size()]);
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

    public synchronized VisibleStudyParameters getVisibleStudyParameters() {
        if (visibleStudyParameters == null) {
            visibleStudyParameters = VisibleStudyParameters.create(this);
        }
        return visibleStudyParameters;
    }

    ////// OBJECT METHODS

    @Override
    // This behavior is necessary so that this class can be a Principal
    public String toString() {
        return getUsername();
    }
}
