package edu.northwestern.bioinformatics.studycalendar.web.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRoleUse;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.cabig.ctms.suite.authorization.ScopeDescription;
import gov.nih.nci.cabig.ctms.suite.authorization.ScopeType;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationScopeMappings.getMapping;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRoleUse.*;

/**
 * Indicates that a particular resource should be available to someone with
 * the given scoping parameters.
 *
 * @see edu.northwestern.bioinformatics.studycalendar.restlets.AuthorizedResource
 * @author Rhett Sutphin
 */
public class ResourceAuthorization {
    private PscRole role;
    private Collection<ScopeDescription> scopes;

    public static ResourceAuthorization create(PscRole role) {
        return new ResourceAuthorization(role);
    }

    public static ResourceAuthorization create(PscRole role, Site site) {
        ResourceAuthorization ra = create(role);
        if (site != null) ra.addScope(ScopeType.SITE, site);
        return ra;
    }

    public static ResourceAuthorization create(PscRole role, Site site, Study study) {
        ResourceAuthorization ra = create(role, site);
        if (study != null) ra.addScope(ScopeType.STUDY, study);
        return ra;
    }

    public static ResourceAuthorization[] createSeveral(PscRole... roles) {
        return createSeveral(null, null, roles);
    }

    public static ResourceAuthorization[] createSeveral(Site site, PscRole... roles) {
        return createSeveral(site, null, roles);
    }

    public static ResourceAuthorization[] createSeveral(Site site, Study study, PscRole... roles) {
        ResourceAuthorization[] ras = new ResourceAuthorization[roles.length];
        for (int i = 0; i < roles.length; i++) {
            ras[i] = create(roles[i], site, study);
        }
        return ras;
    }

    public static Collection<ResourceAuthorization> createCollection(PscRole... roles) {
        return Arrays.asList(createSeveral(roles));
    }

    public static Collection<ResourceAuthorization> createCollection(Site site, PscRole... roles) {
        return Arrays.asList(createSeveral(site, roles));
    }

    public static Collection<ResourceAuthorization> createCollection(Site site, Study study, PscRole... roles) {
        return Arrays.asList(createSeveral(site, study, roles));
    }

    public static Collection<ResourceAuthorization> createAllScopedCollection(ScopeType type, PscRole... roles) {
        ResourceAuthorization[] ras = new ResourceAuthorization[roles.length];
        for (int i = 0; i < roles.length; i++) {
            ras[i] = create(roles[i]);
            ras[i].scopes.add(ScopeDescription.createForAll(type));
        }
        return Arrays.asList(ras);
    }

    /**
     * Creates a set of authorizations reflecting all the appropriate template management roles
     * for the study (i.e., taking into account the managing sites for the study).
     */
    public static Collection<ResourceAuthorization> createTemplateManagementAuthorizations(Study study) {
        return createTemplateManagementAuthorizations(study, TEMPLATE_MANAGEMENT.roles());
    }

    public static Collection<ResourceAuthorization> createTemplateManagementAuthorizations(
        Study study, PscRole... managementRoles
    ) {
        return createStudyUseAuthorizations(study, TEMPLATE_MANAGEMENT, managementRoles);
    }

    private static Collection<Site> getSitesToAuthorizeForStudyUse(PscRoleUse use, Study study) {
        if (study == null) return Collections.singleton(null);
        switch (use) {
            case TEMPLATE_MANAGEMENT:
                return study.isManaged() ? study.getManagingSites() : Collections.<Site>singleton(null);
            case SITE_PARTICIPATION:
                return study.getSites();
            default:
                throw new IllegalArgumentException(use + " is not a study use");
        }
    }

    /**
     * Creates a set of authorizations reflecting all the appropriate site participation roles
     * for the study (i.e., taking into account the study sites).
     */
    public static Collection<ResourceAuthorization> createSiteParticipationAuthorizations(Study study) {
        return createSiteParticipationAuthorizations(study, SITE_PARTICIPATION.roles());
    }

    public static Collection<ResourceAuthorization> createSiteParticipationAuthorizations(
        Study study, PscRole... participationRoles
    ) {
        return createStudyUseAuthorizations(study, SITE_PARTICIPATION, participationRoles);
    }

    /**
     * Creates a set of authorizations reflecting all the legal ways the study could be accessed.
     * This is the set union of management and participation.
     */
    public static Collection<ResourceAuthorization> createAllStudyAuthorizations(Study study) {
        Set<ResourceAuthorization> union = new LinkedHashSet<ResourceAuthorization>();
        union.addAll(createSiteParticipationAuthorizations(study));
        union.addAll(createTemplateManagementAuthorizations(study));
        return union;
    }

    private static Collection<ResourceAuthorization> createStudyUseAuthorizations(
        Study study, PscRoleUse use, PscRole... roles
    ) {
        Collection<Site> authorizedSites = getSitesToAuthorizeForStudyUse(use, study);
        Set<ResourceAuthorization> authorizations = new LinkedHashSet<ResourceAuthorization>();
        for (PscRole role : roles) {
            if (!role.getUses().contains(use)) {
                throw new StudyCalendarError(role + " is not used for " + use);
            }
            if (role.isScoped()) {
                for (Site site : authorizedSites) {
                    authorizations.add(ResourceAuthorization.
                        create(role, site, role.isStudyScoped() ? study : null));
                }
            } else {
                authorizations.add(ResourceAuthorization.create(role));
            }
        }
        return authorizations;
    }

    private ResourceAuthorization(PscRole role) {
        this.role = role;
        this.scopes = new LinkedHashSet<ScopeDescription>();
    }

    /**
     * Returns true if this authorization permits the given user to access
     * the resource.
     */
    public boolean permits(PscUser user) {
        SuiteRoleMembership membership = user.getMemberships().get(getRole().getSuiteRole());
        if (membership == null) return false;
        boolean acceptable = true;
        for (ScopeDescription scope : scopes) {
            acceptable &= (membership.isAll(scope.getScope()) ||
                membership.getIdentifiers(scope.getScope()).contains(scope.getIdentifier()));
        }
        return acceptable;
    }

    @SuppressWarnings({ "unchecked" })
    private void addScope(ScopeType scopeType, Object instance) {
        scopes.add(ScopeDescription.createForOne(
            scopeType, getMapping(scopeType).getSharedIdentity(instance)));
    }

    public String getScope(ScopeType scopeType) {
        for (ScopeDescription scope : scopes) {
            if (scope.getScope() == scopeType) return scope.getIdentifier();
        }
        return null;
    }

    public boolean isAllScoped(ScopeType scopeType) {
        for (ScopeDescription scope : scopes) {
            if (scope.getScope() == scopeType && scope.isAll()) return true;
        }
        return false;
    }

    ////// PROPERTIES

    public PscRole getRole() {
        return role;
    }

    ////// OBJECT METHODS

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceAuthorization)) return false;

        ResourceAuthorization that = (ResourceAuthorization) o;

        if (role != that.role) return false;
        if (scopes != null ? !scopes.equals(that.scopes) : that.scopes != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = role != null ? role.hashCode() : 0;
        result = 31 * result + (scopes != null ? scopes.hashCode() : 0);
        return result;
    }

    public String toString() {
        return new StringBuilder().
            append(getClass().getSimpleName()).
            append("[role=").append(getRole().getCsmName()).
            append("; scopes=").append(scopes).
            append(']').
            toString();
    }
}
