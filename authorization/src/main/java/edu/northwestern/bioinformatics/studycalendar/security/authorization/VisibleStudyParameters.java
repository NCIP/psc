package edu.northwestern.bioinformatics.studycalendar.security.authorization;

import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Collates the details needed to find all the studies accessible
 * to a particular user.  Does not preserve _why_ they are accessible
 * -- this object is just for optimizing the queries to load them.
 * <p>
 * Main properties:
 * <ul>
 *   <li>managingSiteIdentifiers: A list of sites for which the user has access to all managed
 *       studies. If null, the user has access to all studies.  If empty, the user has no
 *       blanket access to studies for management, but may still have specifically-granted study
 *       access.</li>
 *   <li>participatingSiteIdentifiers:  A list of sites for which the user has access to all
 *       participating studies. If null, the user has access to all studies for participation
 *       (i.e., all studies that have at least one StudySite).  If empty, the user has no blanket
 *       access to studies for participation, but may still have specifically-granted study
 *       access.</li>
 *   <li>specificStudyIdentifiers: A list of studies for which the user has been specifically
 *       granted access in some role.</li>
 * </ul>
 *
 * @see UserTemplateRelationship
 * @author Rhett Sutphin
 */
public class VisibleStudyParameters extends VisibleDomainObjectParameters<VisibleStudyParameters> {
    private Collection<String> specificStudyIdentifiers;

    public static VisibleStudyParameters create(PscUser user) {
        return create(user, PscRole.values());
    }

    public static VisibleStudyParameters create(PscUser user, PscRole... roles) {
        VisibleStudyParameters params = new VisibleStudyParameters();
        for (PscRole role : roles) {
            params.applyMembership(user.getMembership(role));
        }
        return params;
    }

    public VisibleStudyParameters() {
        super();
        specificStudyIdentifiers = new LinkedHashSet<String>();
    }

    ////// CREATION

    @Override
    protected void applyParticipation(SuiteRoleMembership membership) {
        if (membership.getRole().isStudyScoped() && !membership.isAllStudies()) {
            forSpecificStudyIdentifiers(membership.getStudyIdentifiers());
        } else {
            super.applyParticipation(membership);
        }
    }

    @Override
    protected void applyTemplateManagement(SuiteRoleMembership membership) {
        if (membership.getRole().isStudyScoped() && !membership.isAllStudies()) {
            forSpecificStudyIdentifiers(membership.getStudyIdentifiers());
        } else {
            super.applyTemplateManagement(membership);
        }
    }

    public VisibleStudyParameters forSpecificStudyIdentifiers(Collection<String> idents) {
        for (String ident : idents) {
            getSpecificStudyIdentifiers().add(ident);
        }
        return this;
    }

    ////// ACCESSORS

    public Collection<String> getSpecificStudyIdentifiers() {
        return specificStudyIdentifiers;
    }

    ////// OBJECT METHODS

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        VisibleStudyParameters that = (VisibleStudyParameters) o;

        if (specificStudyIdentifiers != null ? !specificStudyIdentifiers.equals(that.specificStudyIdentifiers) : that.specificStudyIdentifiers != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (specificStudyIdentifiers != null ? specificStudyIdentifiers.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).
            append("[participatingSites=").
            append(getParticipatingSiteIdentifiers() == null ? "all" : getParticipatingSiteIdentifiers()).
            append("; managingSites=").
            append(getManagingSiteIdentifiers() == null ? "all" : getManagingSiteIdentifiers()).
            append("; specificStudies=").append(getSpecificStudyIdentifiers()).
            append(']').
            toString();
    }
}
