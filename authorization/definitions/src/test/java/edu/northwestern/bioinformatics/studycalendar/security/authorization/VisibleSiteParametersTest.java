package edu.northwestern.bioinformatics.studycalendar.security.authorization;

import gov.nih.nci.cabig.ctms.suite.authorization.SuiteAuthorizationValidationException;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import junit.framework.TestCase;

import java.util.Collection;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Rhett Sutphin
 */
public class VisibleSiteParametersTest extends TestCase {
    /////// MANAGING

    public void testIsAllManagingSitesForAllInSiteScopedManagingRole() throws Exception {
        VisibleSiteParameters actual = actual(createMembership(STUDY_QA_MANAGER).forAllSites());
        assertAllManagingSites(actual);
    }

    public void testParticularManagingSitesForSiteScopedManagingRole() throws Exception {
        VisibleSiteParameters actual = actual(createMembership(STUDY_QA_MANAGER).forSites("Z", "Y"));
        assertManagingSites(actual, "Y", "Z");
    }

    public void testIsAllManagingSitesForAllInSiteAndStudyManagingRole() throws Exception {
        VisibleSiteParameters actual = actual(createMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forAllSites().forAllStudies());
        assertAllManagingSites(actual);
    }

    public void testNoManagingSitesForAllInSiteAndSpecificStudyManagingRole() throws Exception {
        VisibleSiteParameters actual =
            actual(createMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forAllSites().forStudies("ABC"));
        assertAllManagingSites(actual);
    }

    public void testNoManagingSitesForParticularSiteAndSpecificStudyManagingRole() throws Exception {
        VisibleSiteParameters actual =
            actual(createMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites("Z").forStudies("ABC"));
        assertManagingSites(actual, "Z");
    }

    public void testAllManagingSitesTrumpsSomeManagingSites() throws Exception {
        VisibleSiteParameters actual = actual(
            createMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites("Z").forAllStudies(),
            createMembership(STUDY_QA_MANAGER).forAllSites());
        assertAllManagingSites(actual);
    }

    public void testManagingSitesListsMerged() throws Exception {
        VisibleSiteParameters actual = actual(
            createMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites("Z").forAllStudies(),
            createMembership(STUDY_QA_MANAGER).forSites("Y"));
        assertManagingSites(actual, "Z", "Y");
    }

    ////// PARTICIPATING

    public void testIsAllParticipatingSitesForAllInSiteScopedParticipatingRole() throws Exception {
        VisibleSiteParameters actual = actual(createMembership(STUDY_TEAM_ADMINISTRATOR).forAllSites());
        assertAllParticipatingSites(actual);
    }

    public void testParticularParticipatingSitesForSiteScopedParticipatingRole() throws Exception {
        VisibleSiteParameters actual = actual(createMembership(STUDY_TEAM_ADMINISTRATOR).forSites("Z", "Y"));
        assertParticipatingSites(actual, "Y", "Z");
    }

    public void testIsAllParticipatingSitesForAllInSiteAndStudyParticipatingRole() throws Exception {
        VisibleSiteParameters actual = actual(createMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forAllStudies());
        assertAllParticipatingSites(actual);
    }

    public void testNoParticipatingSitesForAllInSiteAndSpecificStudyParticipatingRole() throws Exception {
        VisibleSiteParameters actual =
            actual(createMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forStudies("ABC"));
        assertAllParticipatingSites(actual);
    }

    public void testNoParticipatingSitesForParticularSiteAndSpecificStudyParticipatingRole() throws Exception {
        VisibleSiteParameters actual =
            actual(createMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forSites("Z").forStudies("ABC"));
        assertParticipatingSites(actual, "Z");
    }

    public void testAllParticipatingSitesTrumpsSomeManagingSites() throws Exception {
        VisibleSiteParameters actual = actual(
            createMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forSites("Z").forAllStudies(),
            createMembership(STUDY_QA_MANAGER).forAllSites());
        assertAllParticipatingSites(actual);
    }

    public void testParticipatingSitesListsMerged() throws Exception {
        VisibleSiteParameters actual = actual(
            createMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forSites("Z").forAllStudies(),
            createMembership(STUDY_TEAM_ADMINISTRATOR).forSites("Y"));
        assertParticipatingSites(actual, "Z", "Y");
    }

    ////// GLOBAL

    public void testAllManagingSitesForDataImporter() throws Exception {
        VisibleSiteParameters actual =
            actual(createMembership(DATA_IMPORTER));
        assertAllManagingSites(actual);
        assertNoParticipatingSites(actual);
    }

    public void testDataImporterTrumpsSomeManagingSites() throws Exception {
        VisibleSiteParameters actual = actual(
            createMembership(DATA_IMPORTER),
            createMembership(STUDY_QA_MANAGER).forSites("T", "K"));
        assertAllManagingSites(actual);
    }

    ////// SUBSETS

    public void testCreateForSubsetOnlyIncludesTheSubset() throws Exception {
        VisibleSiteParameters actual = VisibleSiteParameters.create(
            AuthorizationObjectFactory.createPscUser("tim",
                createMembership(DATA_IMPORTER),
                createMembership(STUDY_QA_MANAGER).forSites("A", "Q"),
                createMembership(PscRole.STUDY_TEAM_ADMINISTRATOR).forSites("Q", "J")),
            PscRole.STUDY_QA_MANAGER
        );
        assertManagingSites(actual, "A", "Q");
        assertParticipatingSites(actual, "A", "Q");
    }

    public void testCreateForSubsetIgnoresRolesTheUserDoesNotHave() throws Exception {
        VisibleSiteParameters actual = VisibleSiteParameters.create(
            AuthorizationObjectFactory.createPscUser("tim",
                createMembership(DATA_IMPORTER),
                createMembership(STUDY_QA_MANAGER).forSites("A", "Q"),
                createMembership(PscRole.STUDY_TEAM_ADMINISTRATOR).forSites("Q", "J")),
            PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, PscRole.STUDY_TEAM_ADMINISTRATOR
        );
        assertNoManagingSites(actual);
        assertParticipatingSites(actual, "Q", "J");
    }

    ////// toString

    public void testToStringWhenLimitedBySites() throws Exception {
        VisibleSiteParameters actual = actual(
            createMembership(STUDY_QA_MANAGER).forSites("T"),
            createMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites("K").forAllStudies());
        assertEquals("VisibleSiteParameters[participatingSites=[T]; managingSites=[K, T]]",
            actual.toString());
    }

    public void testToStringWhenUnlimited() throws Exception {
        VisibleSiteParameters actual = actual(
            createMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forAllSites().forAllStudies());
        assertEquals("VisibleSiteParameters[participatingSites=[]; managingSites=all]",
            actual.toString());
    }

    ////// HELPERS

    private void assertNoManagingSites(VisibleSiteParameters actual) {
        assertManagingSites(actual);
    }

    private void assertManagingSites(VisibleSiteParameters actual, String... expectedSites) {
        Collection<String> actualIdentifiers = actual.getManagingSiteIdentifiers();
        assertFalse("Should not be for all managing sites", actual.isAllManagingSites());
        assertEquals("Wrong number of managing site identifiers",
            expectedSites.length, actualIdentifiers.size());
        for (String expectedSite : expectedSites) {
            assertTrue("Missing " + expectedSite + " from " + actualIdentifiers,
                actualIdentifiers.contains(expectedSite));
        }
    }

    private void assertAllManagingSites(VisibleSiteParameters actual) {
        assertTrue("Should be for all managing sites", actual.isAllManagingSites());
    }

    private void assertNoParticipatingSites(VisibleSiteParameters actual) {
        assertParticipatingSites(actual);
    }

    private void assertParticipatingSites(VisibleSiteParameters actual, String... expectedSites) {
        Collection<String> actualIdentifiers = actual.getParticipatingSiteIdentifiers();
        assertEquals("Wrong number of participating site identifiers",
            expectedSites.length, actualIdentifiers.size());
        for (String expectedSite : expectedSites) {
            assertTrue("Missing " + expectedSite + " from " + actualIdentifiers,
                actualIdentifiers.contains(expectedSite));
        }
    }

    private void assertAllParticipatingSites(VisibleSiteParameters actual) {
        assertTrue("Should be for all participating sites", actual.isAllParticipatingSites());
    }

    private SuiteRoleMembership createMembership(PscRole role) {
        return new SuiteRoleMembership(role.getSuiteRole(), null, null);
    }

    private VisibleSiteParameters actual(SuiteRoleMembership... memberships) {
        for (SuiteRoleMembership membership : memberships) {
            try {
                membership.checkComplete();
                membership.validate();
            } catch (SuiteAuthorizationValidationException save) {
                fail("Test setup failure: " + save.getMessage());
            }
        }
        return VisibleSiteParameters.create(
            AuthorizationObjectFactory.createPscUser("jo", memberships));
    }
}