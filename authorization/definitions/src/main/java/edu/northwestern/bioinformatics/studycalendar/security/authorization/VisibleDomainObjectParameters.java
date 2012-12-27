/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.authorization;

import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * @author Rhett Sutphin
 */
public abstract class VisibleDomainObjectParameters<S extends VisibleDomainObjectParameters<S>> {
    protected Collection<String> managingSiteIdentifiers;
    protected Collection<String> participatingSiteIdentifiers;

    public VisibleDomainObjectParameters() {
        participatingSiteIdentifiers = new LinkedHashSet<String>();
        managingSiteIdentifiers = new LinkedHashSet<String>();
    }

    ////// CREATION

    protected void applyMembership(SuiteRoleMembership m) {
        if (m == null) return;
        PscRole role = PscRole.valueOf(m.getRole());
        if (role == null) return;
        if (role.getUses().contains(PscRoleUse.SITE_PARTICIPATION)) {
            this.applyParticipation(m);
        }
        if (role.getUses().contains(PscRoleUse.TEMPLATE_MANAGEMENT)) {
            this.applyTemplateManagement(m);
        }
    }

    protected void applyParticipation(SuiteRoleMembership membership) {
        if (membership.isAllSites() || !membership.getRole().isSiteScoped()) {
            forAllParticipatingSites();
        } else {
            forParticipatingSiteIdentifiers(membership.getSiteIdentifiers());
        }
    }

    protected void applyTemplateManagement(SuiteRoleMembership membership) {
        if (membership.isAllSites() || !membership.getRole().isSiteScoped()) {
            forAllManagingSites();
        } else {
            forManagingSiteIdentifiers(membership.getSiteIdentifiers());
        }
    }

    ////// PROPERTIES & CONFIGURATION

    @SuppressWarnings({ "unchecked" })
    public S forAllManagingSites() {
        managingSiteIdentifiers = null;
        return (S) this;
    }

    @SuppressWarnings({ "unchecked" })
    public S forManagingSiteIdentifiers(Collection<String> idents) {
        if (!isAllManagingSites()) {
            for (String ident : idents) {
                getManagingSiteIdentifiers().add(ident);
            }
        }
        return (S) this;
    }

    public S forManagingSiteIdentifiers(String... idents) {
        return forManagingSiteIdentifiers(Arrays.asList(idents));
    }

    @SuppressWarnings({ "unchecked" })
    public S forAllParticipatingSites() {
        participatingSiteIdentifiers = null;
        return (S) this;
    }

    @SuppressWarnings({ "unchecked" })
    public S forParticipatingSiteIdentifiers(Collection<String> idents) {
        if (!isAllParticipatingSites()) {
            for (String ident : idents) {
                getParticipatingSiteIdentifiers().add(ident);
            }
        }
        return (S) this;
    }

    public S forParticipatingSiteIdentifiers(String... idents) {
        return forParticipatingSiteIdentifiers(Arrays.asList(idents));
    }

    ////// ACCESSORS

    public boolean isAllManagingSites() {
        return managingSiteIdentifiers == null;
    }

    public boolean isAllParticipatingSites() {
        return participatingSiteIdentifiers == null;
    }

    /**
     * The semantics for this collection are defined by the subclasses.
     */
    public Collection<String> getManagingSiteIdentifiers() {
        return managingSiteIdentifiers;
    }

    /**
     * The semantics for this collection are defined by the subclasses.
     */
    public Collection<String> getParticipatingSiteIdentifiers() {
        return participatingSiteIdentifiers;
    }

    ////// OBJECT METHODS

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VisibleDomainObjectParameters)) return false;

        VisibleDomainObjectParameters that = (VisibleDomainObjectParameters) o;

        if (managingSiteIdentifiers != null ? !managingSiteIdentifiers.equals(that.managingSiteIdentifiers) : that.managingSiteIdentifiers != null)
            return false;
        if (participatingSiteIdentifiers != null ? !participatingSiteIdentifiers.equals(that.participatingSiteIdentifiers) : that.participatingSiteIdentifiers != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = managingSiteIdentifiers != null ? managingSiteIdentifiers.hashCode() : 0;
        result = 31 * result + (participatingSiteIdentifiers != null ? participatingSiteIdentifiers.hashCode() : 0);
        return result;
    }
}
