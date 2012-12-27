/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.authorization;

import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteAuthorizationValidationException;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import junit.framework.TestCase;

import java.util.Collection;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Rhett Sutphin
 */
public class VisibleStudyParametersTest extends TestCase {
    private Site x, y, z;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        x = Fixtures.createSite("Ecks", "X");
        y = Fixtures.createSite("Why", "Y");
        z = Fixtures.createSite("Zed", "Z");
    }

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

    public void testNoManagingSitesForAllInSiteAndSpecificUnmanagedStudyManagingRole() throws Exception {
        VisibleStudyParameters actual =
            actual(createMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).
                forAllSites().forStudies(createManagedStudy("ABC")));
        assertNoManagingSites(actual);
        assertSpecificStudies(actual, "ABC");
    }

    public void testNoManagingSitesForParticularSiteAndSpecificApplicableStudyManagingRole() throws Exception {
        VisibleStudyParameters actual =
            actual(createMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).
                forSites(z).forStudies(createManagedStudy("ABC", z, y)));
        assertNoManagingSites(actual);
        assertSpecificStudies(actual, "ABC");
    }

    public void testNoManagingSitesForParticularSiteAndSpecificInapplicableStudyManagingRole() throws Exception {
        VisibleStudyParameters actual =
            actual(createMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).
                forSites(z).forStudies(createManagedStudy("ABC", y, x)));
        assertNoManagingSites(actual);
        assertNoSpecificStudies(actual);
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

    private Study createManagedStudy(String ident, Site... managingSites) {
        Study s = Fixtures.createNamedInstance(ident, Study.class);
        for (Site site : managingSites) {
            s.addManagingSite(site);
        }
        return s;
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
            actual(createMembership(STUDY_SUBJECT_CALENDAR_MANAGER).
                forAllSites().forStudies(createParticipatedInStudy("ABC", z, y)));
        assertNoParticipatingSites(actual);
        assertSpecificStudies(actual, "ABC");
    }

    public void testNoParticipatingSitesForParticularSiteAndSpecificApplicableStudyParticipatingRole() throws Exception {
        VisibleStudyParameters actual =
            actual(createMembership(STUDY_SUBJECT_CALENDAR_MANAGER).
                forSites(z).forStudies(createParticipatedInStudy("ABC", z, y)));
        assertNoParticipatingSites(actual);
        assertSpecificStudies(actual, "ABC");
    }

    public void testNoParticipatingSitesForParticularSiteAndSpecificInapplicableStudyParticipatingRole() throws Exception {
        VisibleStudyParameters actual =
            actual(createMembership(STUDY_SUBJECT_CALENDAR_MANAGER).
                forSites(z).forStudies(createParticipatedInStudy("ABC", y)));
        assertNoParticipatingSites(actual);
        assertNoSpecificStudies(actual);
    }

    public void testNoParticipatingSitesForParticularSiteAndSpecificNoParticipationStudyParticipatingRole() throws Exception {
        VisibleStudyParameters actual =
            actual(createMembership(STUDY_SUBJECT_CALENDAR_MANAGER).
                forSites(z).forStudies(createParticipatedInStudy("ABC")));
        assertNoParticipatingSites(actual);
        assertNoSpecificStudies(actual);
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

    private Study createParticipatedInStudy(String ident, Site... studySites) {
        Study s = Fixtures.createNamedInstance(ident, Study.class);
        for (Site site : studySites) {
            s.addSite(site);
        }
        return s;
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

    ////// SUBSETS

    public void testCreateForSubsetOnlyIncludesTheSubset() throws Exception {
        VisibleStudyParameters actual = VisibleStudyParameters.create(
            AuthorizationObjectFactory.createPscUser("tim",
                createMembership(DATA_IMPORTER),
                createMembership(STUDY_QA_MANAGER).forSites("A", "Q"),
                createMembership(PscRole.STUDY_TEAM_ADMINISTRATOR).forSites("Q", "J")),
            PscRole.STUDY_QA_MANAGER
        );
        assertManagingSites(actual, "A", "Q");
        assertParticipatingSites(actual, "A", "Q");
        assertNoSpecificStudies(actual);
    }

    public void testCreateForSubsetIgnoresRolesTheUserDoesNotHave() throws Exception {
        VisibleStudyParameters actual = VisibleStudyParameters.create(
            AuthorizationObjectFactory.createPscUser("tim",
                createMembership(DATA_IMPORTER),
                createMembership(STUDY_QA_MANAGER).forSites("A", "Q"),
                createMembership(PscRole.STUDY_TEAM_ADMINISTRATOR).forSites("Q", "J")),
            PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, PscRole.STUDY_TEAM_ADMINISTRATOR
        );
        assertNoManagingSites(actual);
        assertParticipatingSites(actual, "Q", "J");
        assertNoSpecificStudies(actual);
    }

    ////// toString

    public void testToStringWhenLimitedBySites() throws Exception {
        VisibleStudyParameters actual = actual(
            createMembership(STUDY_QA_MANAGER).forSites("T"),
            createMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites("K").forAllStudies());
        assertEquals("VisibleStudyParameters[participatingSites=[T]; managingSites=[K, T]; specificStudies=[]]",
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
        assertFalse("Should not be for all managing sites", actual.isAllManagingSites());
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
        return AuthorizationScopeMappings.createSuiteRoleMembership(role);
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
