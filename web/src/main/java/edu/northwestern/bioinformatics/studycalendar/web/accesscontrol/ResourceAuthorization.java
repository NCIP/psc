package edu.northwestern.bioinformatics.studycalendar.web.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.cabig.ctms.suite.authorization.ScopeDescription;
import gov.nih.nci.cabig.ctms.suite.authorization.ScopeType;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

import static edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.AuthorizationScopeMappings.getMapping;

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

    ////// PROPERTIES

    public PscRole getRole() {
        return role;
    }

    ////// OBJECT METHODS

    public String toString() {
        return new StringBuilder().
            append(getClass().getSimpleName()).
            append("[role=").append(getRole().getCsmName()).
            append("; scopes=").append(scopes).
            append(']').
            toString();
    }
}
