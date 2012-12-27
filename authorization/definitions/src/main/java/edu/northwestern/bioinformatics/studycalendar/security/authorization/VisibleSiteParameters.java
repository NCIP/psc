/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.authorization;

/**
 * @author Rhett Sutphin
 */
public class VisibleSiteParameters extends VisibleDomainObjectParameters<VisibleSiteParameters> {
    public static VisibleSiteParameters create(PscUser user) {
        return create(user, PscRole.values());
    }

    public static VisibleSiteParameters create(PscUser user, PscRole... roles) {
        VisibleSiteParameters params = new VisibleSiteParameters();
        for (PscRole role : roles) {
            params.applyMembership(user.getMembership(role));
        }
        return params;
    }

    ////// OBJECT METHODS

    @Override // to ensure class equality
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return super.equals(o);
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).
            append("[participatingSites=").
            append(getParticipatingSiteIdentifiers() == null ? "all" : getParticipatingSiteIdentifiers()).
            append("; managingSites=").
            append(getManagingSiteIdentifiers() == null ? "all" : getManagingSiteIdentifiers()).
            append(']').
            toString();
    }
}
