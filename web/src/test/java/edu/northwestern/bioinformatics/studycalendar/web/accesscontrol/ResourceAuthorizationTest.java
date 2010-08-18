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

import java.util.Collection;
import java.util.Iterator;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Rhett Sutphin
 */
public class ResourceAuthorizationTest extends TestCase {
    private static final String SITE_A_IDENT = "a!";
    private static final String SITE_B_IDENT = "b!";

    private Site siteA, siteB;
    private Study studyA, studyB;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        siteA = Fixtures.createSite("A", SITE_A_IDENT);
        siteB = Fixtures.createSite("B", SITE_B_IDENT);

        studyA = Fixtures.createReleasedTemplate("A");
        studyB = Fixtures.createReleasedTemplate("B");
    }

    public void testRoleOnlyAuthorizationPermitsRoleOnlyMembership() throws Exception {
        assertTrue(ResourceAuthorization.create(SYSTEM_ADMINISTRATOR).permits(
            createUser(SYSTEM_ADMINISTRATOR)));
    }

    public void testRoleOnlyAuthorizationDoesNotPermitOtherRoleMembership() throws Exception {
        assertFalse(ResourceAuthorization.create(DATA_IMPORTER).permits(
            createUser(BUSINESS_ADMINISTRATOR)));
    }

    public void testRoleAndSiteAuthorizationPermitsMatchingSiteMembership() throws Exception {
        assertTrue(ResourceAuthorization.create(USER_ADMINISTRATOR, siteA).permits(
            createUser(createMembership(USER_ADMINISTRATOR).forSites(siteA))));
    }

    public void testRoleAndSiteAuthorizationPermitsAllSiteMembership() throws Exception {
        assertTrue(ResourceAuthorization.create(USER_ADMINISTRATOR, siteA).permits(
            createUser(createMembership(USER_ADMINISTRATOR).forAllSites())));
    }

    public void testRoleAndSiteAuthorizationDoesNotPermitNonMatchingMembership() throws Exception {
        assertFalse(ResourceAuthorization.create(USER_ADMINISTRATOR, siteA).permits(
            createUser(createMembership(USER_ADMINISTRATOR).forSites(siteB))));
    }

    public void testRoleAndSiteAuthorizationDoesNotPermitNoSiteMembership() throws Exception {
        assertFalse(ResourceAuthorization.create(USER_ADMINISTRATOR, siteA).permits(
            createUser(createMembership(USER_ADMINISTRATOR))));
    }

    public void testRoleSiteAndStudyAuthorizationPermitsMatchingSiteAndStudyMembership() throws Exception {
        assertTrue(ResourceAuthorization.create(DATA_READER, siteA, studyB).permits(
            createUser(createMembership(DATA_READER).forSites(siteA).forStudies(studyA, studyB))));
    }

    public void testRoleSiteAndStudyAuthorizationPermitsAllSiteMembership() throws Exception {
        assertTrue(ResourceAuthorization.create(DATA_READER, siteA, studyB).permits(
            createUser(createMembership(DATA_READER).forAllSites().forStudies(studyA, studyB))));
    }

    public void testRoleSiteAndStudyAuthorizationPermitsAllStudyMembership() throws Exception {
        assertTrue(ResourceAuthorization.create(DATA_READER, siteA, studyB).permits(
            createUser(createMembership(DATA_READER).forSites(siteA, siteB).forAllStudies())));
    }

    public void testRoleSiteAndStudyAuthorizationDoesNotPermitUnscopedMembership() throws Exception {
        assertFalse(ResourceAuthorization.create(DATA_READER, siteA, studyB).permits(
            createUser(createMembership(DATA_READER))));
    }

    public void testRoleSiteAndStudyAuthorizationDoesNotPermitSiteOnlyMembership() throws Exception {
        assertFalse(ResourceAuthorization.create(DATA_READER, siteA, studyB).permits(
            createUser(createMembership(DATA_READER).forAllSites())));
    }

    public void testRoleSiteAndStudyAuthorizationDoesNotPermitStudyOnlyMembership() throws Exception {
        assertFalse(ResourceAuthorization.create(DATA_READER, siteA, studyB).permits(
            createUser(createMembership(DATA_READER).forAllStudies())));
    }

    public void testRoleSiteAndStudyAuthorizationDoesNotPermitForMismatchedStudy() throws Exception {
        assertFalse(ResourceAuthorization.create(DATA_READER, siteA, studyB).permits(
            createUser(createMembership(DATA_READER).forAllSites().forStudies(studyA))));
    }

    public void testRoleSiteAndStudyAuthorizationDoesNotPermitForMismatchedSite() throws Exception {
        assertFalse(ResourceAuthorization.create(DATA_READER, siteA, studyB).permits(
            createUser(createMembership(DATA_READER).forSites(siteB).forStudies(studyB))));
    }

    public void testCreateMultipleAuthorizationsByRoleOnly() throws Exception {
        ResourceAuthorization[] actual = ResourceAuthorization.createSeveral(
            DATA_READER, STUDY_CALENDAR_TEMPLATE_BUILDER);
        assertEquals("Wrong number of RAs", 2, actual.length);
        assertResourceAuthorization("Wrong 1st RA", DATA_READER, null, null, actual[0]);
        assertResourceAuthorization("Wrong 2nd RA",
            STUDY_CALENDAR_TEMPLATE_BUILDER, null, null, actual[1]);
    }

    public void testCreateMultipleAuthorizationsByRoleAndSite() throws Exception {
        ResourceAuthorization[] actual = ResourceAuthorization.createSeveral(
            siteA, DATA_READER, STUDY_CALENDAR_TEMPLATE_BUILDER);
        assertEquals("Wrong number of RAs", 2, actual.length);
        assertResourceAuthorization("Wrong 1st RA", DATA_READER, SITE_A_IDENT, null, actual[0]);
        assertResourceAuthorization("Wrong 2nd RA",
            STUDY_CALENDAR_TEMPLATE_BUILDER, SITE_A_IDENT, null, actual[1]);
    }

    public void testCreateMultipleAuthorizationsByRoleSiteAndStudy() throws Exception {
        ResourceAuthorization[] actual = ResourceAuthorization.createSeveral(
            siteB, studyB, DATA_READER, STUDY_CALENDAR_TEMPLATE_BUILDER);
        assertEquals("Wrong number of RAs", 2, actual.length);
        assertResourceAuthorization("Wrong 1st RA", DATA_READER, SITE_B_IDENT, "B", actual[0]);
        assertResourceAuthorization("Wrong 2nd RA",
            STUDY_CALENDAR_TEMPLATE_BUILDER, SITE_B_IDENT, "B", actual[1]);
    }

    public void testCreateManagingStudyAuthorizationsForManagedStudy() throws Exception {
        studyA.addManagingSite(siteA);
        studyA.addManagingSite(siteB);

        Collection<ResourceAuthorization> actual =
            ResourceAuthorization.createTemplateManagementAuthorizations(studyA);
        assertEquals("Wrong number of authorizations", 1 + 2 * 5, actual.size());

        Iterator<ResourceAuthorization> it = actual.iterator();
        assertResourceAuthorization("Missing creator for site A", STUDY_CREATOR, SITE_A_IDENT, null, it.next());
        assertResourceAuthorization("Missing creator for site B", STUDY_CREATOR, SITE_B_IDENT, null, it.next());
        assertResourceAuthorization("Missing builder for site A", STUDY_CALENDAR_TEMPLATE_BUILDER, SITE_A_IDENT, "A", it.next());
        assertResourceAuthorization("Missing builder for site B", STUDY_CALENDAR_TEMPLATE_BUILDER, SITE_B_IDENT, "A", it.next());
        assertResourceAuthorization("Missing SQM for site A", STUDY_QA_MANAGER, SITE_A_IDENT, null, it.next());
        assertResourceAuthorization("Missing SQM for site B", STUDY_QA_MANAGER, SITE_B_IDENT, null, it.next());
        assertResourceAuthorization("Missing SSPA for site A", STUDY_SITE_PARTICIPATION_ADMINISTRATOR, SITE_A_IDENT, null, it.next());
        assertResourceAuthorization("Missing SSPA for site B", STUDY_SITE_PARTICIPATION_ADMINISTRATOR, SITE_B_IDENT, null, it.next());
        assertResourceAuthorization("Missing reader for site A", DATA_READER, SITE_A_IDENT, "A", it.next());
        assertResourceAuthorization("Missing reader for site B", DATA_READER, SITE_B_IDENT, "A", it.next());
        assertResourceAuthorization("Missing importer", DATA_IMPORTER, null, null, it.next());
    }

    public void testCreateManagingStudyAuthorizationsForNullStudy() throws Exception {
        Collection<ResourceAuthorization> actual =
            ResourceAuthorization.createTemplateManagementAuthorizations(null);
        assertEquals("Wrong number of authorizations", 6, actual.size());

        Iterator<ResourceAuthorization> it = actual.iterator();
        assertResourceAuthorization("Missing creator", STUDY_CREATOR, null, null, it.next());
        assertResourceAuthorization("Missing builder", STUDY_CALENDAR_TEMPLATE_BUILDER, null, null, it.next());
        assertResourceAuthorization("Missing SQM", STUDY_QA_MANAGER, null, null, it.next());
        assertResourceAuthorization("Missing SSPA", STUDY_SITE_PARTICIPATION_ADMINISTRATOR, null, null, it.next());
        assertResourceAuthorization("Missing reader", DATA_READER, null, null, it.next());
        assertResourceAuthorization("Missing importer", DATA_IMPORTER, null, null, it.next());
    }

    public void testCreateManagingStudyAuthorizationsForUnmanagedStudy() throws Exception {
        assertFalse("Test setup failure", studyA.isManaged());
        Collection<ResourceAuthorization> actual =
            ResourceAuthorization.createTemplateManagementAuthorizations(studyA);
        assertEquals("Wrong number of authorizations", 6, actual.size());

        Iterator<ResourceAuthorization> it = actual.iterator();
        assertResourceAuthorization("Missing creator", STUDY_CREATOR, null, null, it.next());
        assertResourceAuthorization("Missing builder", STUDY_CALENDAR_TEMPLATE_BUILDER, null, "A", it.next());
        assertResourceAuthorization("Missing SQM", STUDY_QA_MANAGER, null, null, it.next());
        assertResourceAuthorization("Missing SSPA", STUDY_SITE_PARTICIPATION_ADMINISTRATOR, null, null, it.next());
        assertResourceAuthorization("Missing reader", DATA_READER, null, "A", it.next());
        assertResourceAuthorization("Missing importer", DATA_IMPORTER, null, null, it.next());
    }

    public void testCreateSpecificRoleManagingStudyAuthorizationsForManagedStudy() throws Exception {
        studyA.addManagingSite(siteA);
        studyA.addManagingSite(siteB);

        Collection<ResourceAuthorization> actual =
            ResourceAuthorization.createTemplateManagementAuthorizations(studyA, STUDY_QA_MANAGER);
        assertEquals("Wrong number of authorizations", 2, actual.size());

        Iterator<ResourceAuthorization> it = actual.iterator();
        assertResourceAuthorization("Missing SQM for site A", STUDY_QA_MANAGER, SITE_A_IDENT, null, it.next());
        assertResourceAuthorization("Missing SQM for site B", STUDY_QA_MANAGER, SITE_B_IDENT, null, it.next());
    }

    public void testCreateSpecificRoleManagingStudyAuthorizationsForUnmanagedStudy() throws Exception {
        assertFalse("Test setup failure", studyA.isManaged());
        Collection<ResourceAuthorization> actual =
            ResourceAuthorization.createTemplateManagementAuthorizations(studyA, STUDY_CALENDAR_TEMPLATE_BUILDER);
        assertEquals("Wrong number of authorizations", 1, actual.size());
        assertResourceAuthorization("Missing builder", STUDY_CALENDAR_TEMPLATE_BUILDER, null, "A", actual.iterator().next());
    }

    public void testCreateParticipatingAuthorizationsForStudyWithNoParticipation() throws Exception {
        assertEquals("Test setup failure", 0, studyA.getStudySites().size());
        Collection<ResourceAuthorization> actual =
            ResourceAuthorization.createSiteParticipationAuthorizations(studyA);
        assertEquals("Wrong number of authorizations", 0, actual.size());
    }

    public void testCreateParticipatingAuthorizationsForStudy() throws Exception {
        studyA.addSite(siteA);
        studyA.addSite(siteB);

        Collection<ResourceAuthorization> actual =
            ResourceAuthorization.createSiteParticipationAuthorizations(studyA);
        assertEquals("Wrong number of authorizations", 2 * 4, actual.size());

        Iterator<ResourceAuthorization> it = actual.iterator();
        assertResourceAuthorization("Missing SQM for site A", STUDY_QA_MANAGER, SITE_A_IDENT, null, it.next());
        assertResourceAuthorization("Missing SQM for site B", STUDY_QA_MANAGER, SITE_B_IDENT, null, it.next());
        assertResourceAuthorization("Missing STA for site A", STUDY_TEAM_ADMINISTRATOR, SITE_A_IDENT, null, it.next());
        assertResourceAuthorization("Missing STA for site B", STUDY_TEAM_ADMINISTRATOR, SITE_B_IDENT, null, it.next());
        assertResourceAuthorization("Missing SSCM for site A", STUDY_SUBJECT_CALENDAR_MANAGER, SITE_A_IDENT, "A", it.next());
        assertResourceAuthorization("Missing SSCM for site B", STUDY_SUBJECT_CALENDAR_MANAGER, SITE_B_IDENT, "A", it.next());
        assertResourceAuthorization("Missing reader for site A", DATA_READER, SITE_A_IDENT, "A", it.next());
        assertResourceAuthorization("Missing reader for site B", DATA_READER, SITE_B_IDENT, "A", it.next());
    }

    public void testCreateParticipatingAuthorizationsForNullStudy() throws Exception {
        Collection<ResourceAuthorization> actual =
            ResourceAuthorization.createSiteParticipationAuthorizations(null);
        assertEquals("Wrong number of authorizations", 4, actual.size());

        Iterator<ResourceAuthorization> it = actual.iterator();
        assertResourceAuthorization("Missing SQM", STUDY_QA_MANAGER, null, null, it.next());
        assertResourceAuthorization("Missing STA", STUDY_TEAM_ADMINISTRATOR, null, null, it.next());
        assertResourceAuthorization("Missing SSCM", STUDY_SUBJECT_CALENDAR_MANAGER, null, null, it.next());
        assertResourceAuthorization("Missing reader", DATA_READER, null, null, it.next());
    }

    public void testCreateAllAuthorizationsForStudy() throws Exception {
        studyB.addSite(siteA);
        studyB.addSite(siteB);
        studyB.addManagingSite(siteA);

        Collection<ResourceAuthorization> actual =
            ResourceAuthorization.createAllStudyAuthorizations(studyB);
        assertEquals("Wrong number of authorizations", 12, actual.size());

        Iterator<ResourceAuthorization> it = actual.iterator();

        // participation
        assertResourceAuthorization("Missing SQM for site A", STUDY_QA_MANAGER, SITE_A_IDENT, null, it.next());
        assertResourceAuthorization("Missing SQM for site B", STUDY_QA_MANAGER, SITE_B_IDENT, null, it.next());
        assertResourceAuthorization("Missing STA for site A", STUDY_TEAM_ADMINISTRATOR, SITE_A_IDENT, null, it.next());
        assertResourceAuthorization("Missing STA for site B", STUDY_TEAM_ADMINISTRATOR, SITE_B_IDENT, null, it.next());
        assertResourceAuthorization("Missing SSCM for site A", STUDY_SUBJECT_CALENDAR_MANAGER, SITE_A_IDENT, "B", it.next());
        assertResourceAuthorization("Missing SSCM for site B", STUDY_SUBJECT_CALENDAR_MANAGER, SITE_B_IDENT, "B", it.next());
        assertResourceAuthorization("Missing reader for site A", DATA_READER, SITE_A_IDENT, "B", it.next());
        assertResourceAuthorization("Missing reader for site B", DATA_READER, SITE_B_IDENT, "B", it.next());

        // management (but only the non-repeated ones)
        assertResourceAuthorization("Missing creator", STUDY_CREATOR, SITE_A_IDENT, null, it.next());
        assertResourceAuthorization("Missing builder", STUDY_CALENDAR_TEMPLATE_BUILDER, SITE_A_IDENT, "B", it.next());
        assertResourceAuthorization("Missing SSPA", STUDY_SITE_PARTICIPATION_ADMINISTRATOR, SITE_A_IDENT, null, it.next());
        assertResourceAuthorization("Missing importer", DATA_IMPORTER, null, null, it.next());
    }

    public void testCreateAllScopedCollectionForSite() {
        Collection<ResourceAuthorization> actual =
            ResourceAuthorization.createAllScopedCollection(ScopeType.SITE, STUDY_QA_MANAGER, PERSON_AND_ORGANIZATION_INFORMATION_MANAGER);
        assertEquals("Wrong number of authorizations", 2, actual.size());
        Iterator<ResourceAuthorization> it = actual.iterator();
        assertTrue("Should be all-site scoped", it.next().isAllScoped(ScopeType.SITE));
        assertTrue("Should be all-site scoped", it.next().isAllScoped(ScopeType.SITE));
    }

    ////// HELPERS

    public static void assertResourceAuthorization(
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
