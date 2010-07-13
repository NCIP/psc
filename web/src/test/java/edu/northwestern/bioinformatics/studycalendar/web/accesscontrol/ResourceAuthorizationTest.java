package edu.northwestern.bioinformatics.studycalendar.web.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.AuthorizationScopeMappings;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.cabig.ctms.suite.authorization.ScopeType;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import junit.framework.TestCase;

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

    public void testCreateMultipleAuthorizationsByRoleOnly() throws Exception {
        ResourceAuthorization[] actual = ResourceAuthorization.createSeveral(
            PscRole.DATA_READER, PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER);
        assertEquals("Wrong number of RAs", 2, actual.length);
        assertResourceAuthorization("Wrong 1st RA", PscRole.DATA_READER, null, null, actual[0]);
        assertResourceAuthorization("Wrong 2nd RA",
            PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER, null, null, actual[1]);
    }

    public void testCreateMultipleAuthorizationsByRoleAndSite() throws Exception {
        ResourceAuthorization[] actual = ResourceAuthorization.createSeveral(
            siteA, PscRole.DATA_READER, PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER);
        assertEquals("Wrong number of RAs", 2, actual.length);
        assertResourceAuthorization("Wrong 1st RA", PscRole.DATA_READER, "a!", null, actual[0]);
        assertResourceAuthorization("Wrong 2nd RA",
            PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER, "a!", null, actual[1]);
    }

    public void testCreateMultipleAuthorizationsByRoleSiteAndStudy() throws Exception {
        ResourceAuthorization[] actual = ResourceAuthorization.createSeveral(
            siteB, studyB, PscRole.DATA_READER, PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER);
        assertEquals("Wrong number of RAs", 2, actual.length);
        assertResourceAuthorization("Wrong 1st RA", PscRole.DATA_READER, "b!", "B", actual[0]);
        assertResourceAuthorization("Wrong 2nd RA",
            PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER, "b!", "B", actual[1]);
    }

    private void assertResourceAuthorization(
        String message,
        PscRole expectedRole, String expectedSiteIdent, String expectedStudyIdent,
        ResourceAuthorization actual
    ) {
        assertNotNull(message + ": RA not present", actual);
        assertEquals(message + ": wrong role", expectedRole, actual.getRole());
        if (expectedSiteIdent == null) {
            assertNull(message + ": expected no site", actual.getScope(ScopeType.SITE));
        } else {
            assertEquals(message + ": wrong site", expectedSiteIdent, actual.getScope(ScopeType.SITE));
        }
        if (expectedStudyIdent == null) {
            assertNull(message + ": expected no study", actual.getScope(ScopeType.STUDY));
        } else {
            assertEquals(message + ": wrong study", expectedStudyIdent, actual.getScope(ScopeType.STUDY));
        }
    }

    private PscUser createUser(PscRole role) {
        return AuthorizationObjectFactory.createPscUser("josephine", role);
    }

    private PscUser createUser(SuiteRoleMembership membership) {
        return AuthorizationObjectFactory.createPscUser("josephine", membership);
    }

    private SuiteRoleMembership createMembership(PscRole role) {
        return new SuiteRoleMembership(
            role.getSuiteRole(), AuthorizationScopeMappings.SITE_MAPPING, AuthorizationScopeMappings.STUDY_MAPPING);
    }
}
