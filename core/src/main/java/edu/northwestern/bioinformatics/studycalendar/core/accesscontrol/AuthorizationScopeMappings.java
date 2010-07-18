package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import gov.nih.nci.cabig.ctms.suite.authorization.IdentifiableInstanceMapping;
import gov.nih.nci.cabig.ctms.suite.authorization.ScopeType;
import gov.nih.nci.cabig.ctms.suite.authorization.SiteMapping;
import gov.nih.nci.cabig.ctms.suite.authorization.StudyMapping;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class AuthorizationScopeMappings {
    public static final SiteMapping<Site> SITE_MAPPING = new StaticPscSiteMapping();
    public static final StudyMapping<Study> STUDY_MAPPING = new StaticPscStudyMapping();

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public static IdentifiableInstanceMapping getMapping(ScopeType scopeType) {
        switch (scopeType) {
            case SITE: return SITE_MAPPING;
            case STUDY: return STUDY_MAPPING;
            default: throw new IllegalArgumentException("No mapping available for " + scopeType);
        }
    }

    public static SuiteRoleMembership createSuiteRoleMembership(PscRole role) {
        return new SuiteRoleMembership(role.getSuiteRole(), SITE_MAPPING, STUDY_MAPPING);
    }

    private static class StaticIdentifiableInstanceMapping<T> implements IdentifiableInstanceMapping<T> {
        private IdentifiableInstanceMapping<T> delegate;

        private StaticIdentifiableInstanceMapping(IdentifiableInstanceMapping<T> delegate) {
            this.delegate = delegate;
        }

        public String getSharedIdentity(T instance) {
            return delegate.getSharedIdentity(instance);
        }

        public boolean isInstance(Object o) {
            return delegate.isInstance(o);
        }

        public List<T> getApplicationInstances(List<String> ids) {
            throw new UnsupportedOperationException(
                "getApplicationInstances is not supported on static instances.  Use a spring-configured instance instead.");
        }
    }

    private static class StaticPscSiteMapping
        extends StaticIdentifiableInstanceMapping<Site>
        implements SiteMapping<Site>
    {
        private StaticPscSiteMapping() { super(new PscSiteMapping()); }
    }

    private static class StaticPscStudyMapping
        extends StaticIdentifiableInstanceMapping<Study>
        implements StudyMapping<Study>
    {
        private StaticPscStudyMapping() { super(new PscStudyMapping()); }
    }

    private AuthorizationScopeMappings() { }
}
