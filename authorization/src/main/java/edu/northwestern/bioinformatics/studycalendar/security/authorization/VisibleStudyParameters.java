package edu.northwestern.bioinformatics.studycalendar.security.authorization;

import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Collates the details needed to find all the studies accessible
 * to a particular user.  Does not preserve _why_ they are accessible
 * -- this object is just for optimizing the query to load them.
 *
 * @see UserTemplateRelationship
 * @author Rhett Sutphin
 */
public class VisibleStudyParameters {
    private Collection<String> managingSiteIdentifiers;
    private Collection<String> participatingSiteIdentifiers;
    private Collection<String> specificStudyIdentifiers;

    public static VisibleStudyParameters create(PscUser user) {
        VisibleStudyParameters params = new VisibleStudyParameters();
        for (PscRole role : PscRole.values()) {
            SuiteRoleMembership m = user.getMembership(role);
            if (m == null) continue;
            if (role.getUses().contains(PscRoleUse.SITE_PARTICIPATION)) {
                params.applyParticipation(m);
            }
            if (role.getUses().contains(PscRoleUse.TEMPLATE_MANAGEMENT)) {
                params.applyTemplateManagement(m);
            }
        }
        return params;
    }

    public VisibleStudyParameters() {
        managingSiteIdentifiers = new LinkedHashSet<String>();
        participatingSiteIdentifiers = new LinkedHashSet<String>();
        specificStudyIdentifiers = new LinkedHashSet<String>();
    }

    ////// LOGIC

    private void applyParticipation(SuiteRoleMembership membership) {
        if (membership.getRole().isStudyScoped() && !membership.isAllStudies()) {
            forSpecificStudyIdentifiers(membership.getStudyIdentifiers());
        } else if (membership.isAllSites()) {
            forAllParticipatingSites();
        } else {
            forParticipatingSiteIdentifiers(membership.getSiteIdentifiers());
        }
    }

    private void applyTemplateManagement(SuiteRoleMembership membership) {
        if (membership.getRole().isStudyScoped() && !membership.isAllStudies()) {
            forSpecificStudyIdentifiers(membership.getStudyIdentifiers());
        } else if (membership.isAllSites() || !membership.getRole().isSiteScoped()) {
            forAllManagingSites();
        } else {
            forManagingSiteIdentifiers(membership.getSiteIdentifiers());
        }
    }

    public boolean isAllManagingSites() {
        return managingSiteIdentifiers == null;
    }

    public VisibleStudyParameters forAllManagingSites() {
        managingSiteIdentifiers = null;
        return this;
    }

    public VisibleStudyParameters forManagingSiteIdentifiers(Collection<String> idents) {
        if (!isAllManagingSites()) {
            for (String ident : idents) {
                getManagingSiteIdentifiers().add(ident);
            }
        }
        return this;
    }

    public boolean isAllParticipatingSites() {
        return participatingSiteIdentifiers == null;
    }

    public VisibleStudyParameters forAllParticipatingSites() {
        participatingSiteIdentifiers = null;
        return this;
    }

    public VisibleStudyParameters forParticipatingSiteIdentifiers(Collection<String> idents) {
        if (!isAllParticipatingSites()) {
            for (String ident : idents) {
                getParticipatingSiteIdentifiers().add(ident);
            }
        }
        return this;
    }

    public VisibleStudyParameters forSpecificStudyIdentifiers(Collection<String> idents) {
        for (String ident : idents) {
            getSpecificStudyIdentifiers().add(ident);
        }
        return this;
    }

    ////// ACCESSORS

    /**
     * A list of sites for which the user has access to all managed studies.
     * If null, the user has access to all studies.  If empty, the user has
     * no blanket access to studies for management, but may still have
     * specifically-granted study access. 
     */
    public Collection<String> getManagingSiteIdentifiers() {
        return managingSiteIdentifiers;
    }

    /**
     * A list of sites for which the user has access to all participating studies.
     * If null, the user has access to all studies for participation (i.e., all
     * studies that have at least one StudySite).  If empty, the user has no blanket
     * access to studies for participation, but may still have specifically-granted
     * study access.
     */
    public Collection<String> getParticipatingSiteIdentifiers() {
        return participatingSiteIdentifiers;
    }

    /**
     * A list of studies for which the user has been specifically granted access
     * in some role.
     */
    public Collection<String> getSpecificStudyIdentifiers() {
        return specificStudyIdentifiers;
    }

    ////// OBJECT METHODS

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).
            append("[participatingSites=").
            append(participatingSiteIdentifiers == null ? "all" : participatingSiteIdentifiers).
            append("; managingSites=").
            append(managingSiteIdentifiers == null ? "all" : managingSiteIdentifiers).
            append("; specificStudies=").append(specificStudyIdentifiers).
            append(']').
            toString();
    }
}
