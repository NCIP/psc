package edu.northwestern.bioinformatics.studycalendar.security.authorization;

import edu.northwestern.bioinformatics.studycalendar.tools.StringTools;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class PscUser implements UserDetails, Comparable<PscUser> {
    private User user;
    private Map<SuiteRole, SuiteRoleMembership> memberships;
    private edu.northwestern.bioinformatics.studycalendar.domain.User legacyUser;

    private boolean stale;
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

        this.stale = false;
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

    public String getDisplayName() {
        List<String> parts = new ArrayList<String>(
            Arrays.asList(getCsmUser().getFirstName(), getCsmUser().getLastName()));
        for (Iterator<String> it = parts.iterator(); it.hasNext();) {
            if (isBlankNamePart(it.next())) it.remove();
        }
        if (parts.isEmpty()) {
            return getUsername();
        } else {
            return StringUtils.join(parts, ' ');
        }
    }

    public String getLastFirst() {
        List<String> parts = new ArrayList<String>(
            Arrays.asList(getCsmUser().getLastName(), getCsmUser().getFirstName()));
        for (Iterator<String> it = parts.iterator(); it.hasNext();) {
            if (isBlankNamePart(it.next())) it.remove();
        }
        if (parts.isEmpty()) {
            return getUsername();
        } else {
            return StringUtils.join(parts, ", ");
        }
    }

    private boolean isBlankNamePart(String p) {
        return StringUtils.isBlank(p) || p.equals(".");
    }

    public boolean isActive() {
        return getCsmUser().getEndDate() == null ||
            getCsmUser().getEndDate().compareTo(new Date()) > 0;
    }

    public int compareTo(PscUser o) {
        if (getCsmUser() != null && o.getCsmUser() != null) {
            int comp;
            comp = StringTools.CASE_INSENSITIVE_NULL_SAFE_ORDER.
                compare(getCsmUser().getLastName(), o.getCsmUser().getLastName());
            if (comp != 0) return comp;
            comp = StringTools.CASE_INSENSITIVE_NULL_SAFE_ORDER.
                compare(getCsmUser().getFirstName(), o.getCsmUser().getFirstName());
            if (comp != 0) return comp;
        }
        return getUsername().compareToIgnoreCase(o.getUsername());
    }

    public boolean isStale() {
        return stale;
    }

    public void setStale(boolean stale) {
        this.stale = stale;
    }

    ////// ARBITRARY ATTRIBUTES

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public void copyAttributesInto(PscUser other) {
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            other.setAttribute(entry.getKey(), entry.getValue());
        }
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

    ////// DOMAIN RELATIONSHIPS

    /**
     * Returns a description of the studies visible to the user in the given roles.
     * If no roles are specified, it returns a description for all the roles the
     * user has.
     */
    public VisibleStudyParameters getVisibleStudyParameters(PscRole... roles) {
        return VisibleStudyParameters.create(this, roles.length == 0 ? PscRole.values() : roles);
    }

    /**
     * Returns a description of the sites visible to the user in the given roles.
     * If no roles are specified, it returns a description for all the roles the
     * user has.
     */
    public VisibleSiteParameters getVisibleSiteParameters(PscRole... roles) {
        return VisibleSiteParameters.create(this, roles.length == 0 ? PscRole.values() : roles);
    }

    ////// OBJECT METHODS

    @Override
    // This behavior is necessary so that this class can be a Principal
    public String toString() {
        return getUsername();
    }
}
