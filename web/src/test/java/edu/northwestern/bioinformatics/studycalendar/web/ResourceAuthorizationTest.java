package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.AuthorizationScopeMappings;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.restlets.ResourceAuthorization;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.security.authorization.domainobjects.User;
import junit.framework.TestCase;

import java.util.Collections;

/**
 * @author Rhett Sutphin
 */
public class ResourceAuthorizationTest extends TestCase {
    private Site siteA, siteB;
    private Study studyA, studyB;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        siteA = Fixtures.createSite("A", "a!");
        siteB = Fixtures.createSite("B", "b!");

        studyA = Fixtures.createReleasedTemplate("A");
        studyB = Fixtures.createReleasedTemplate("B");
    }

    public void testRoleOnlyAuthorizationPermitsRoleOnlyMembership() throws Exception {
        assertTrue(ResourceAuthorization.create(PscRole.SYSTEM_ADMINISTRATOR).permits(
            createUser(PscRole.SYSTEM_ADMINISTRATOR)));
    }

    public void testRoleOnlyAuthorizationDoesNotPermitOtherRoleMembership() throws Exception {
        assertFalse(ResourceAuthorization.create(PscRole.DATA_IMPORTER).permits(
            createUser(PscRole.BUSINESS_ADMINISTRATOR)));
    }

    public void testRoleAndSiteAuthorizationPermitsMatchingSiteMembership() throws Exception {
        assertTrue(ResourceAuthorization.create(PscRole.USER_ADMINISTRATOR, siteA).permits(
            createUser(createMembership(PscRole.USER_ADMINISTRATOR).forSites(siteA))));
    }

    public void testRoleAndSiteAuthorizationPermitsAllSiteMembership() throws Exception {
        assertTrue(ResourceAuthorization.create(PscRole.USER_ADMINISTRATOR, siteA).permits(
            createUser(createMembership(PscRole.USER_ADMINISTRATOR).forAllSites())));
    }

    public void testRoleAndSiteAuthorizationDoesNotPermitNonMatchingMembership() throws Exception {
        assertFalse(ResourceAuthorization.create(PscRole.USER_ADMINISTRATOR, siteA).permits(
            createUser(createMembership(PscRole.USER_ADMINISTRATOR).forSites(siteB))));
    }

    public void testRoleAndSiteAuthorizationDoesNotPermitNoSiteMembership() throws Exception {
        assertFalse(ResourceAuthorization.create(PscRole.USER_ADMINISTRATOR, siteA).permits(
            createUser(createMembership(PscRole.USER_ADMINISTRATOR))));
    }

    public void testRoleSiteAndStudyAuthorizationPermitsMatchingSiteAndStudyMembership() throws Exception {
        assertTrue(ResourceAuthorization.create(PscRole.DATA_READER, siteA, studyB).permits(
            createUser(createMembership(PscRole.DATA_READER).forSites(siteA).forStudies(studyA, studyB))));
    }

    public void testRoleSiteAndStudyAuthorizationPermitsAllSiteMembership() throws Exception {
        assertTrue(ResourceAuthorization.create(PscRole.DATA_READER, siteA, studyB).permits(
            createUser(createMembership(PscRole.DATA_READER).forAllSites().forStudies(studyA, studyB))));
    }

    public void testRoleSiteAndStudyAuthorizationPermitsAllStudyMembership() throws Exception {
        assertTrue(ResourceAuthorization.create(PscRole.DATA_READER, siteA, studyB).permits(
            createUser(createMembership(PscRole.DATA_READER).forSites(siteA, siteB).forAllStudies())));
    }

    public void testRoleSiteAndStudyAuthorizationDoesNotPermitUnscopedMembership() throws Exception {
        assertFalse(ResourceAuthorization.create(PscRole.DATA_READER, siteA, studyB).permits(
            createUser(createMembership(PscRole.DATA_READER))));
    }

    public void testRoleSiteAndStudyAuthorizationDoesNotPermitSiteOnlyMembership() throws Exception {
        assertFalse(ResourceAuthorization.create(PscRole.DATA_READER, siteA, studyB).permits(
            createUser(createMembership(PscRole.DATA_READER).forAllSites())));
    }

    public void testRoleSiteAndStudyAuthorizationDoesNotPermitStudyOnlyMembership() throws Exception {
        assertFalse(ResourceAuthorization.create(PscRole.DATA_READER, siteA, studyB).permits(
            createUser(createMembership(PscRole.DATA_READER).forAllStudies())));
    }

    public void testRoleSiteAndStudyAuthorizationDoesNotPermitForMismatchedStudy() throws Exception {
        assertFalse(ResourceAuthorization.create(PscRole.DATA_READER, siteA, studyB).permits(
            createUser(createMembership(PscRole.DATA_READER).forAllSites().forStudies(studyA))));
    }

    public void testRoleSiteAndStudyAuthorizationDoesNotPermitForMismatchedSite() throws Exception {
        assertFalse(ResourceAuthorization.create(PscRole.DATA_READER, siteA, studyB).permits(
            createUser(createMembership(PscRole.DATA_READER).forSites(siteB).forStudies(studyB))));
    }

    private PscUser createUser(PscRole role) {
        User csmUser = new User();
        csmUser.setLoginName("josephine");
        SuiteRoleMembership srm = createMembership(role);
        return new PscUser(csmUser, Collections.singletonMap(role.getSuiteRole(), srm));
    }

    private PscUser createUser(SuiteRoleMembership membership) {
        User csmUser = new User();
        csmUser.setLoginName("josephine");
        return new PscUser(csmUser, Collections.singletonMap(membership.getRole(), membership));
    }

    private SuiteRoleMembership createMembership(PscRole role) {
        return new SuiteRoleMembership(
            role.getSuiteRole(), AuthorizationScopeMappings.SITE_MAPPING, AuthorizationScopeMappings.STUDY_MAPPING);
    }
}
