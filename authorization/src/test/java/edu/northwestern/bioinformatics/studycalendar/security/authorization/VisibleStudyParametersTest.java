package edu.northwestern.bioinformatics.studycalendar.security.authorization;

import gov.nih.nci.cabig.ctms.suite.authorization.SuiteAuthorizationValidationException;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import junit.framework.TestCase;

import java.util.Collection;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Rhett Sutphin
 */
public class VisibleStudyParametersTest extends TestCase {
    /////// MANAGING

    public void testIsAllManagingSitesForAllInSiteScopedManagingRole() throws Exception {
        VisibleStudyParameters actual = actual(createMembership(STUDY_QA_MANAGER).forAllSites());
        assertAllManagingSites(actual);
        assertNoSpecificStudies(actual);
    }

    public void testParticularManagingSitesForSiteScopedManagingRole() throws Exception {
        VisibleStudyParameters actual = actual(createMembership(STUDY_QA_MANAGER).forSites("Z", "Y"));
        assertNoSpecificStudies(actual);
        assertManagingSites(actual, "Y", "Z");
    }

    public void testIsAllManagingSitesForAllInSiteAndStudyManagingRole() throws Exception {
        VisibleStudyParameters actual = actual(createMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forAllSites().forAllStudies());
        assertAllManagingSites(actual);
        assertNoSpecificStudies(actual);
    }

    public void testNoManagingSitesForAllInSiteAndSpecificStudyManagingRole() throws Exception {
        VisibleStudyParameters actual =
            actual(createMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forAllSites().forStudies("ABC"));
        assertNoManagingSites(actual);
        assertSpecificStudies(actual, "ABC");
    }

    public void testNoManagingSitesForParticularSiteAndSpecificStudyManagingRole() throws Exception {
        VisibleStudyParameters actual =
            actual(createMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites("Z").forStudies("ABC"));
        assertNoManagingSites(actual);
        assertSpecificStudies(actual, "ABC");
    }

    public void testAllManagingSitesTrumpsSomeManagingSites() throws Exception {
        VisibleStudyParameters actual = actual(
            createMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites("Z").forAllStudies(),
            createMembership(STUDY_QA_MANAGER).forAllSites());
        assertAllManagingSites(actual);
    }

    public void testManagingSitesListsMerged() throws Exception {
        VisibleStudyParameters actual = actual(
            createMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites("Z").forAllStudies(),
            createMembership(STUDY_QA_MANAGER).forSites("Y"));
        assertManagingSites(actual, "Z", "Y");
    }

    ////// PARTICIPATING

    public void testIsAllParticipatingSitesForAllInSiteScopedParticipatingRole() throws Exception {
        VisibleStudyParameters actual = actual(createMembership(STUDY_TEAM_ADMINISTRATOR).forAllSites());
        assertAllParticipatingSites(actual);
        assertNoSpecificStudies(actual);
    }

    public void testParticularParticipatingSitesForSiteScopedParticipatingRole() throws Exception {
        VisibleStudyParameters actual = actual(createMembership(STUDY_TEAM_ADMINISTRATOR).forSites("Z", "Y"));
        assertNoSpecificStudies(actual);
        assertParticipatingSites(actual, "Y", "Z");
    }

    public void testIsAllParticipatingSitesForAllInSiteAndStudyParticipatingRole() throws Exception {
        VisibleStudyParameters actual = actual(createMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forAllStudies());
        assertAllParticipatingSites(actual);
        assertNoSpecificStudies(actual);
    }

    public void testNoParticipatingSitesForAllInSiteAndSpecificStudyParticipatingRole() throws Exception {
        VisibleStudyParameters actual =
            actual(createMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forStudies("ABC"));
        assertNoParticipatingSites(actual);
        assertSpecificStudies(actual, "ABC");
    }

    public void testNoParticipatingSitesForParticularSiteAndSpecificStudyParticipatingRole() throws Exception {
        VisibleStudyParameters actual =
            actual(createMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forSites("Z").forStudies("ABC"));
        assertNoParticipatingSites(actual);
        assertSpecificStudies(actual, "ABC");
    }

    public void testAllParticipatingSitesTrumpsSomeManagingSites() throws Exception {
        VisibleStudyParameters actual = actual(
            createMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forSites("Z").forAllStudies(),
            createMembership(STUDY_QA_MANAGER).forAllSites());
        assertAllParticipatingSites(actual);
    }
    
    public void testParticipatingSitesListsMerged() throws Exception {
        VisibleStudyParameters actual = actual(
            createMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forSites("Z").forAllStudies(),
            createMembership(STUDY_TEAM_ADMINISTRATOR).forSites("Y"));
        assertParticipatingSites(actual, "Z", "Y");
    }

    ////// GLOBAL

    public void testAllManagingSitesForDataImporter() throws Exception {
        VisibleStudyParameters actual =
            actual(createMembership(DATA_IMPORTER));
        assertAllManagingSites(actual);
        assertNoParticipatingSites(actual);
        assertNoSpecificStudies(actual);
    }

    public void testDataImporterTrumpsSomeManagingSites() throws Exception {
        VisibleStudyParameters actual = actual(
            createMembership(DATA_IMPORTER),
            createMembership(STUDY_QA_MANAGER).forSites("T", "K"));
        assertAllManagingSites(actual);
    }

    ////// toString

    public void testToStringWhenLimitedBySites() throws Exception {
        VisibleStudyParameters actual = actual(
            createMembership(STUDY_QA_MANAGER).forSites("T"),
            createMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites("K").forAllStudies());
        assertEquals("VisibleStudyParameters[participatingSites=[T]; managingSites=[T, K]; specificStudies=[]]",
            actual.toString());
    }

    public void testToStringWhenUnlimited() throws Exception {
        VisibleStudyParameters actual = actual(
            createMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forAllSites().forAllStudies());
        assertEquals("VisibleStudyParameters[participatingSites=[]; managingSites=all; specificStudies=[]]",
            actual.toString());
    }

    public void testToStringForSpecificStudies() throws Exception {
        VisibleStudyParameters actual = actual(
            createMembership(STUDY_TEAM_ADMINISTRATOR).forAllSites(),
            createMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forAllSites().forStudies("ABC", "FOO 15"));
        assertEquals("VisibleStudyParameters[participatingSites=all; managingSites=[]; specificStudies=[ABC, FOO 15]]",
            actual.toString());
    }

    ////// HELPERS

    private void assertNoSpecificStudies(VisibleStudyParameters actual) {
        assertSpecificStudies(actual);
    }

    private void assertSpecificStudies(VisibleStudyParameters actual, String... expectedStudies) {
        Collection<String> actualIdentifiers = actual.getSpecificStudyIdentifiers();
        assertEquals("Wrong number of study identifiers",
            expectedStudies.length, actualIdentifiers.size());
        for (String expectedStudy : expectedStudies) {
            assertTrue("Missing " + expectedStudy + " from " + actualIdentifiers,
                actualIdentifiers.contains(expectedStudy));
        }
    }

    private void assertNoManagingSites(VisibleStudyParameters actual) {
        assertManagingSites(actual);
    }

    private void assertManagingSites(VisibleStudyParameters actual, String... expectedSites) {
        Collection<String> actualIdentifiers = actual.getManagingSiteIdentifiers();
        assertEquals("Wrong number of managing site identifiers",
            expectedSites.length, actualIdentifiers.size());
        for (String expectedSite : expectedSites) {
            assertTrue("Missing " + expectedSite + " from " + actualIdentifiers,
                actualIdentifiers.contains(expectedSite));
        }
    }

    private void assertAllManagingSites(VisibleStudyParameters actual) {
        assertTrue("Should be for all managing sites", actual.isAllManagingSites());
    }

    private void assertNoParticipatingSites(VisibleStudyParameters actual) {
        assertParticipatingSites(actual);
    }

    private void assertParticipatingSites(VisibleStudyParameters actual, String... expectedSites) {
        Collection<String> actualIdentifiers = actual.getParticipatingSiteIdentifiers();
        assertEquals("Wrong number of participating site identifiers",
            expectedSites.length, actualIdentifiers.size());
        for (String expectedSite : expectedSites) {
            assertTrue("Missing " + expectedSite + " from " + actualIdentifiers,
                actualIdentifiers.contains(expectedSite));
        }
    }

    private void assertAllParticipatingSites(VisibleStudyParameters actual) {
        assertTrue("Should be for all participating sites", actual.isAllParticipatingSites());
    }

    private SuiteRoleMembership createMembership(PscRole role) {
        return new SuiteRoleMembership(role.getSuiteRole(), null, null);
    }

    private VisibleStudyParameters actual(SuiteRoleMembership... memberships) {
        for (SuiteRoleMembership membership : memberships) {
            try {
                membership.checkComplete();
                membership.validate();
            } catch (SuiteAuthorizationValidationException save) {
                fail("Test setup failure: " + save.getMessage());
            }
        }
        return VisibleStudyParameters.create(
            AuthorizationObjectFactory.createPscUser("jo", memberships));
    }
}
