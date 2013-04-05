/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.presenter;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationScopeMappings;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRoleUse;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.cabig.ctms.suite.authorization.ScopeType;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
class UserRelationshipTools {
    private final PscUser user;
    private final Study study;

    public UserRelationshipTools(PscUser user, Study study) {
        this.user = user;
        this.study = study;
    }

    /**
     * Indicates that the user has any appropriately scoped managing role.
     */
    public boolean isManaging() {
        return isManagingAsOneOf(PscRoleUse.TEMPLATE_MANAGEMENT.roles());
    }

    /**
     * Indicates that the user has at least one of the given roles, scoped
     * appropriately for "managing" actions.
     */
    public boolean isManagingAsOneOf(PscRole... roles) {
        for (PscRole role : roles) {
            if (hasManagingMembership(role)) return true;
        }
        return false;
    }

    /**
     * Indicates that the user has at least one of the given roles, scoped
     * appropriately for "participating" actions in at least one of the
     * participating sites.
     */
    public boolean isParticipatingAsOneOf(PscRole... roles) {
        for (PscRole role : roles) {
            if (!getParticipatingStudySites(role).isEmpty()) return true;
        }
        return false;
    }

    public Collection<StudySite> getParticipatingStudySites(PscRole role) {
        if (!role.getUses().contains(PscRoleUse.SITE_PARTICIPATION)) {
            throw new StudyCalendarError("%s is not a site participation role", role);
        }
        SuiteRoleMembership membership = getUser().getMembership(role);
        if (membership == null || !hasMatchingStudyScope(role)) return Collections.emptySet();
        if (membership.isAllSites()) return study.getStudySites();

        Set<StudySite> matches = new LinkedHashSet<StudySite>();
        for (StudySite studySite : study.getStudySites()) {
            if (membership.getSites().contains(studySite.getSite())) matches.add(studySite);
        }
        return matches;
    }

    private boolean hasManagingMembership(PscRole role) {
        if (!role.getUses().contains(PscRoleUse.TEMPLATE_MANAGEMENT)) {
            throw new StudyCalendarError("%s is not a template management role", role);
        }
        SuiteRoleMembership membership = getUser().getMembership(role);

        if (membership == null) return false;
        if (!role.isScoped()) return true;
        if (!hasMatchingStudyScope(role)) return false;
        if (!study.isManaged()) return true;

        if (membership.isAllSites()) return true;
        for (Site site : getStudy().getManagingSites()) {
            if (membership.getSites().contains(site)) return true;
        }
        return false;
    }

    public boolean hasMatchingStudyScope(PscRole role) {
        return hasMatchingScope(role, this.study);
    }

    public boolean hasMatchingScope(PscRole role, Study study) {
        return hasMatchingScope(role, ScopeType.STUDY, study);
    }

    public boolean hasMatchingScope(PscRole role, Site site) {
        return hasMatchingScope(role, ScopeType.SITE, site);
    }

    @SuppressWarnings({"unchecked"})
    private boolean hasMatchingScope(PscRole role, ScopeType type, Object site) {
        SuiteRoleMembership membership = getUser().getMembership(role);
        return membership != null && (
            !membership.getRole().getScopes().contains(type) ||
            membership.isAll(type) ||
            membership.getIdentifiers(type).contains(
                AuthorizationScopeMappings.getMapping(type).getSharedIdentity(site))
        );
    }

    ////// ACCESSORS

    public PscUser getUser() {
        return user;
    }

    public Study getStudy() {
        return study;
    }
}
