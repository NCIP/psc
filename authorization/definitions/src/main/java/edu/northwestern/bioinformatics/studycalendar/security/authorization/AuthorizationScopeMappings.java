/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.authorization;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
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

    private static class StaticPscSiteMapping extends BasePscSiteMapping {
        @Override
        public List<Site> getApplicationInstances(List<String> ids) {
            throw new UnsupportedOperationException(
                "getApplicationInstances is not supported on static instances.  Use a spring-configured instance instead.");
        }
    }

    private static class StaticPscStudyMapping extends BasePscStudyMapping {
        @Override
        public List<Study> getApplicationInstances(List<String> ids) {
            throw new UnsupportedOperationException(
                "getApplicationInstances is not supported on static instances.  Use a spring-configured instance instead.");
        }
    }

    private AuthorizationScopeMappings() { }
}
