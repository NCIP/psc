package edu.northwestern.bioinformatics.studycalendar.service.presenter;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteAuthorizationValidationException;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;

import java.util.Collection;
import java.util.Date;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.AuthorizationScopeMappings.createSuiteRoleMembership;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.createPscUser;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Rhett Sutphin
 */
public class UserTemplateRelationshipTest extends StudyCalendarTestCase {
    private Study study;
    private Site nu, mayo, vanderbilt;
    private Study otherStudy;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        /*
         * Study is released to NU and VU, approved at NU.
         */
        study = createBasicTemplate("ECT 3402");
        nu = createSite("RHLCCC", "IL036");
        mayo = createSite("Mayo", "MN003");
        vanderbilt = createSite("Vanderbilt", "TN008");

        StudySite nuSS = study.addSite(nu);
        nuSS.approveAmendment(study.getAmendment(), new Date());
        study.addSite(vanderbilt);

        otherStudy = createBasicTemplate("Boo");
    }

    ////// isManagingAs

    public void testStudyCalendarTemplateBuilderFromManagingSiteIsManaging() throws Exception {
        study.addManagingSite(nu);
        assertTrue(
            actual(createSuiteRoleMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites(nu).forAllStudies()).
                isManagingAsOneOf(STUDY_CALENDAR_TEMPLATE_BUILDER));
    }

    public void testStudyCalendarTemplateBuilderForAllSitesIsManagingForManagedTemplate() throws Exception {
        study.addManagingSite(nu);
        assertTrue(
            actual(createSuiteRoleMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forAllSites().forAllStudies()).
                isManagingAsOneOf(STUDY_CALENDAR_TEMPLATE_BUILDER));
    }

    public void testStudyCalendarTemplateBuilderForAnySiteIsManagingForUnmanagedTemplate() throws Exception {
        assertTrue(
            actual(createSuiteRoleMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites(mayo).forAllStudies()).
                isManagingAsOneOf(STUDY_CALENDAR_TEMPLATE_BUILDER));
    }

    public void testStudyCalendarTemplateBuilderForOtherSiteIsNotManagingForManagedTemplate() throws Exception {
        study.addManagingSite(nu);
        assertFalse(
            actual(createSuiteRoleMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites(mayo).forAllStudies()).
                isManagingAsOneOf(STUDY_CALENDAR_TEMPLATE_BUILDER));
    }

    public void testStudyCalendarTemplateBuilderWithSpecificStudyAccessIsNotManaging() throws Exception {
        assertTrue(
            actual(createSuiteRoleMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites(mayo).forStudies(study)).
                isManagingAsOneOf(STUDY_CALENDAR_TEMPLATE_BUILDER));
    }

    public void testStudyCalendarTemplateBuilderWithSpecificStudyAccessToAnotherStudyIsNotManaging() throws Exception {
        assertFalse(
            actual(createSuiteRoleMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites(mayo).forStudies(otherStudy)).
                isManagingAsOneOf(STUDY_CALENDAR_TEMPLATE_BUILDER));
    }

    public void testStudyCalendarTemplateBuilderWithSpecificStudyAccessButNotFromManagingSiteIsNotManaging() throws Exception {
        study.addManagingSite(nu);
        assertFalse(
            actual(createSuiteRoleMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites(mayo).forStudies(study)).
                isManagingAsOneOf(STUDY_CALENDAR_TEMPLATE_BUILDER));
    }

    public void testIsManagingAsIsTrueForAtLeastOneMatch() throws Exception {
        study.addManagingSite(nu);
        assertTrue(
            actual(createSuiteRoleMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites(nu).forAllStudies()).
                isManagingAsOneOf(STUDY_QA_MANAGER, STUDY_CALENDAR_TEMPLATE_BUILDER));
    }

    ////// isParticipatingAs

    public void testMembershipFromPureManagingSiteIsNotParticipating() throws Exception {
        study.addManagingSite(mayo);
        assertFalse(
            actual(createSuiteRoleMembership(DATA_READER).forSites(mayo).forAllStudies()).
                isParticipatingAsOneOf(DATA_READER));
    }

    public void testMembershipFromParticipatingSiteIsParticipating() throws Exception {
        study.addManagingSite(nu);
        assertTrue(
            actual(createSuiteRoleMembership(DATA_READER).forSites(vanderbilt).forAllStudies()).
                isParticipatingAsOneOf(DATA_READER));
    }

    public void testMembershipForTheSpecificStudyIsParticipating() throws Exception {
        study.addManagingSite(nu);
        assertTrue(
            actual(createSuiteRoleMembership(DATA_READER).forAllSites().forStudies(study)).
                isParticipatingAsOneOf(DATA_READER));
    }

    public void testMembershipForSomeOtherStudyIsParticipating() throws Exception {
        study.addManagingSite(nu);
        assertTrue(
            actual(createSuiteRoleMembership(DATA_READER).forAllSites().forStudies(study)).
                isParticipatingAsOneOf(DATA_READER));
    }

    public void testMembershipFromOtherSiteIsNotParticipating() throws Exception {
        study.addManagingSite(nu);
        assertFalse(
            actual(createSuiteRoleMembership(DATA_READER).forAllSites().forStudies(otherStudy)).
                isParticipatingAsOneOf(DATA_READER));
    }

    public void testMembershipFromOtherSiteButThisStudyIsNotParticipating() throws Exception {
        study.addManagingSite(nu);
        assertFalse(
            actual(createSuiteRoleMembership(DATA_READER).forSites(mayo).forStudies(study)).
                isParticipatingAsOneOf(DATA_READER));
    }

    public void testMembershipFromParticipatingSiteButOtherStudyIsNotParticipating() throws Exception {
        study.addManagingSite(nu);
        assertFalse(
            actual(createSuiteRoleMembership(DATA_READER).forSites(nu).forStudies(otherStudy)).
                isParticipatingAsOneOf(DATA_READER));
    }

    public void testIsParticipatingAsIsTrueForAtLeastOneMatch() throws Exception {
        assertTrue(
            actual(createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(nu)).
                isParticipatingAsOneOf(STUDY_QA_MANAGER, DATA_READER));
    }

    ////// canStartAmendment

    public void testCanStartAmendmentIfBuilderAndNoneExists() throws Exception {
        study.addManagingSite(nu);
        assertTrue(
            actual(createSuiteRoleMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites(nu).forAllStudies()).
                getCanStartAmendment());
    }

    public void testCannotStartAmendmentIfNotManaging() throws Exception {
        study.addManagingSite(mayo);
        assertFalse(
            actual(createSuiteRoleMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites(nu).forAllStudies()).
                getCanStartAmendment());
    }

    public void testCannotStartAmendmentIfNotBuilder() throws Exception {
        assertFalse(
            actual(createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(nu)).
                getCanStartAmendment());
    }

    public void testCannotStartAmendmentIfOneExists() throws Exception {
        study.setDevelopmentAmendment(new Amendment());
        assertFalse(
            actual(createSuiteRoleMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites(nu).forAllStudies()).
                getCanStartAmendment());
    }

    ////// canDevelop

    public void testCanDevelopIfBuilderAndInDev() throws Exception {
        study.addManagingSite(nu);
        study.setDevelopmentAmendment(new Amendment());
        assertTrue(
            actual(createSuiteRoleMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites(nu).forAllStudies()).
                getCanDevelop());
    }

    public void testCannotDevelopIfNotManaging() throws Exception {
        study.setDevelopmentAmendment(new Amendment());
        study.addManagingSite(mayo);
        assertFalse(
            actual(createSuiteRoleMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites(nu).forAllStudies()).
                getCanDevelop());
    }

    public void testCannotDevelopIfNotBuilder() throws Exception {
        study.setDevelopmentAmendment(new Amendment());
        study.addManagingSite(nu);
        assertFalse(
            actual(createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(nu)).
                getCanDevelop());
    }

    public void testCannotDevelopIfNotInDevelopment() throws Exception {
        assertFalse(
            actual(createSuiteRoleMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites(nu).forAllStudies()).
                getCanDevelop());
    }

    ////// canSeeDevelopmentVersion

    public void testCanSeeDevVersionWhenBuilder() throws Exception {
        study.setDevelopmentAmendment(new Amendment());
        study.addManagingSite(nu);
        assertTrue(
            actual(createSuiteRoleMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites(nu).forAllStudies()).
                getCanSeeDevelopmentVersion());
    }

    public void testCanSeeDevVersionWhenManagingQA() throws Exception {
        study.setDevelopmentAmendment(new Amendment());
        study.addManagingSite(nu);
        assertTrue(
            actual(createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(nu)).
                getCanSeeDevelopmentVersion());
    }

    public void testCannotSeeDevVersionWhenParticipatingQA() throws Exception {
        study.setDevelopmentAmendment(new Amendment());
        study.addManagingSite(nu);
        assertFalse(
            actual(createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(vanderbilt)).
                getCanSeeDevelopmentVersion());
    }

    public void testCannotSeeDevVersionWhenUnrelatedQA() throws Exception {
        study.setDevelopmentAmendment(new Amendment());
        study.addManagingSite(nu);
        assertFalse(
            actual(createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(mayo)).
                getCanSeeDevelopmentVersion());
    }

    public void testCanSeeDevVersionWhenManagingReader() throws Exception {
        study.setDevelopmentAmendment(new Amendment());
        study.addManagingSite(nu);
        assertTrue(
            actual(createSuiteRoleMembership(DATA_READER).forSites(nu).forAllStudies()).
                getCanSeeDevelopmentVersion());
    }

    public void testCannotSeeDevVersionWhenParticipatingReader() throws Exception {
        study.setDevelopmentAmendment(new Amendment());
        study.addManagingSite(nu);
        assertFalse(
            actual(createSuiteRoleMembership(DATA_READER).forSites(vanderbilt).forAllStudies()).
                getCanSeeDevelopmentVersion());
    }

    public void testCannotSeeDevVersionWhenOtherManagingRole() throws Exception {
        study.setDevelopmentAmendment(new Amendment());
        study.addManagingSite(nu);
        assertFalse(
            actual(createSuiteRoleMembership(STUDY_SITE_PARTICIPATION_ADMINISTRATOR).forSites(nu)).
                getCanSeeDevelopmentVersion());
    }

    public void testCannotSeeDevVersionWhenNotInDevelopment() throws Exception {
        study.addManagingSite(nu);
        assertFalse(
            actual(createSuiteRoleMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites(nu).forAllStudies()).
                getCanSeeDevelopmentVersion());
    }

    public void testDataImporterCanSeeDevelopmentVersion() throws Exception {
        study.setDevelopmentAmendment(new Amendment());
        assertTrue(
            actual(createSuiteRoleMembership(DATA_IMPORTER)).
                getCanSeeDevelopmentVersion());
    }

    public void testStudyCreatorCanSeeDevelopmentVersion() throws Exception {
        study.setDevelopmentAmendment(new Amendment());
        study.addManagingSite(nu);
        assertTrue(
            actual(createSuiteRoleMembership(STUDY_CREATOR).forSites(nu)).
                getCanSeeDevelopmentVersion());
    }

    ////// canChangeManagingSites

    public void testManagingQACanSetManagingSites() throws Exception {
        study.addManagingSite(nu);
        assertTrue(
            actual(createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(nu)).
                getCanChangeManagingSites());
    }

    public void testParticipatingQACannotSetManagingSites() throws Exception {
        study.addManagingSite(nu);
        assertFalse(
            actual(createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(vanderbilt)).
                getCanChangeManagingSites());
    }

    public void testManagingBuilderCanSetManagingSites() throws Exception {
        study.addManagingSite(nu);
        assertTrue(
            actual(createSuiteRoleMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites(nu).forAllStudies()).
                getCanChangeManagingSites());
    }

    public void testOtherManagingRolesCannotSetManagingSites() throws Exception {
        study.addManagingSite(nu);
        assertFalse(
            actual(createSuiteRoleMembership(DATA_READER).forSites(nu).forAllStudies()).
                getCanChangeManagingSites());
    }

    ////// canRelease

    public void testManagingQACanReleaseWhenInDev() throws Exception {
        study.setDevelopmentAmendment(new Amendment());
        study.addManagingSite(nu);
        assertTrue(
            actual(createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(nu)).
                getCanRelease());
    }

    public void testManagingQACannotReleaseWhenNotInDev() throws Exception {
        study.addManagingSite(nu);
        assertFalse(
            actual(createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(nu)).
                getCanRelease());
    }

    public void testOtherManagingRolesCannotRelease() throws Exception {
        study.setDevelopmentAmendment(new Amendment());
        study.addManagingSite(nu);
        assertFalse(
            actual(createSuiteRoleMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites(nu).forAllStudies()).
                getCanRelease());
    }

    public void testParticipatingQACannotRelease() throws Exception {
        study.setDevelopmentAmendment(new Amendment());
        study.addManagingSite(nu);
        assertFalse(
            actual(createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(vanderbilt)).
                getCanRelease());
    }

    ////// canSetParticipation

    public void testManagingParticipationAdminCanSetParticipationWhenReleased() throws Exception {
        study.addManagingSite(nu);
        assertTrue(
            actual(createSuiteRoleMembership(STUDY_SITE_PARTICIPATION_ADMINISTRATOR).forSites(nu)).
                getCanSetParticipation());
    }

    public void testManagingParticipationAdminCannotSetParticipationWhenNoReleasedVersion() throws Exception {
        study.addManagingSite(nu);
        study.setAmendment(null);
        assertFalse(
            actual(createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(nu)).
                getCanSetParticipation());
    }

    public void testOtherManagingRolesCannotSetParticipation() throws Exception {
        study.addManagingSite(nu);
        assertFalse(
            actual(createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(nu)).
                getCanSetParticipation());
    }

    public void testOtherParticipationAdminCannotSetParticipation() throws Exception {
        study.addManagingSite(nu);
        assertFalse(
            actual(createSuiteRoleMembership(STUDY_SITE_PARTICIPATION_ADMINISTRATOR).forSites(vanderbilt)).
                getCanSetParticipation());
    }

    ////// canApprove

    public void testParticipatingQACanApproveWhenUnapprovedAmendmentsAvailable() throws Exception {
        assertTrue(
            actual(createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(vanderbilt)).
                getCanApprove());
    }

    public void testParticipatingQACannotApproveWhenAllAmendmentsApproved() throws Exception {
        assertFalse(
            actual(createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(nu)).
                getCanApprove());
    }

    public void testManagingQACannotApprove() throws Exception {
        study.addManagingSite(mayo);
        assertFalse(
            actual(createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(mayo)).
                getCanApprove());
    }

    public void testOtherParticipatingRolesCannotApprove() throws Exception {
        assertFalse(
            actual(createSuiteRoleMembership(DATA_READER).forSites(vanderbilt).forAllStudies()).
                getCanApprove());
    }

    ////// canAssignSubjects

    public void testApprovedParticipatingSubjectCalendarManagerCanAssignSubjects() throws Exception {
        assertTrue(
            actual(createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forSites(nu).forAllStudies()).
                getCanAssignSubjects());
    }

    public void testApprovedParticipatingSubjectCalendarManagerForThisSpecificStudyCanAssignSubjects() throws Exception {
        assertTrue(
            actual(createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forSites(nu).forStudies(study)).
                getCanAssignSubjects());
    }

    public void testApprovedParticipatingSubjectCalendarManagerForOtherStudyCannotAssignSubjects() throws Exception {
        assertFalse(
            actual(createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forSites(nu).forStudies(otherStudy)).
                getCanAssignSubjects());
    }

    public void testUnapprovedParticipatingSubjectCalendarManagerCannotAssignSubjects() throws Exception {
        assertFalse(
            actual(createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forSites(vanderbilt).forAllStudies()).
                getCanAssignSubjects());
    }

    public void testNonparticipatingSubjectCalendarManagerCannotAssignSubjects() throws Exception {
        assertFalse(
            actual(createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forSites(mayo).forAllStudies()).
                getCanAssignSubjects());
    }

    public void testOtherRoleCannotAssignSubjects() throws Exception {
        assertFalse(
            actual(createSuiteRoleMembership(SUBJECT_MANAGER).forSites(nu)).
                getCanAssignSubjects());
    }

    ////// getSubjectAssignableStudySites

    public void testCanOnlyAssignSubjectsForApprovedTemplatesWhenHasAccessToSpecificSites() throws Exception {
        Collection<StudySite> actual =
            actual(createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forSites(vanderbilt, nu, mayo).forAllStudies()).
                getSubjectAssignableStudySites();
        assertEquals("Wrong number of study sites", 1, actual.size());
        assertEquals("Wrong study site", nu, actual.iterator().next().getSite());
    }

    public void testCanOnlyAssignSubjectsForApprovedTemplatesWhenHasAccessToAllSites() throws Exception {
        Collection<StudySite> actual =
            actual(createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forAllStudies()).
                getSubjectAssignableStudySites();
        assertEquals("Wrong number of study sites", 1, actual.size());
        assertEquals("Wrong study site", nu, actual.iterator().next().getSite());
    }

    public void testCannotAssignSubjectsWhenHasAccessOnlyToADifferentStudy() throws Exception {
        Collection<StudySite> actual =
            actual(createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forStudies(otherStudy)).
                getSubjectAssignableStudySites();
        assertEquals("Wrong number of study sites", 0, actual.size());
    }

    public void testAssignSubjectsWhenHasAccessOnlyThisSpecificStudy() throws Exception {
        Collection<StudySite> actual =
            actual(createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forStudies(study)).
                getSubjectAssignableStudySites();
        assertEquals("Wrong number of study sites", 1, actual.size());
        assertEquals("Wrong study site", nu, actual.iterator().next().getSite());
    }

    public void testCannotAssignSubjectsWhenInADifferentRole() throws Exception {
        Collection<StudySite> actual =
            actual(createSuiteRoleMembership(REGISTRAR).forSites(nu).forAllStudies()).
                getSubjectAssignableStudySites();
        assertEquals("Wrong number of study sites", 0, actual.size());
    }

    ////// getCanSeeReleasedVersions

    public void testCannotSeeReleasedVersionIfThereAreNone() throws Exception {
        study.setAmendment(null);
        assertFalse(
            actual(createSuiteRoleMembership(STUDY_QA_MANAGER).forAllSites()).
                getCanSeeReleasedVersions());
    }

    public void testTemplateManagerCanSeeReleasedVersions() throws Exception {
        study.addManagingSite(vanderbilt);
        assertTrue(
            actual(createSuiteRoleMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites(vanderbilt).forAllStudies()).
                getCanSeeReleasedVersions());
    }

    public void testManagingRoleForOtherSiteCannotSeeReleasedVersions() throws Exception {
        assertTrue(
            actual(createSuiteRoleMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites(mayo).forAllStudies()).
                getCanSeeReleasedVersions());
    }

    public void testDataImporterCanSeeReleasedVersions() throws Exception {
        assertTrue(
            actual(createSuiteRoleMembership(DATA_IMPORTER)).
                getCanSeeReleasedVersions());
    }

    public void testParticipatingQACanSeeReleasedVersions() throws Exception {
        assertTrue(
            actual(createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(nu)).
                getCanSeeReleasedVersions());
    }

    public void testManagingReaderCanSeeReleasedVersions() throws Exception {
        study.addManagingSite(mayo);
        assertTrue(
            actual(createSuiteRoleMembership(DATA_READER).forSites(mayo).forAllStudies()).
                getCanSeeReleasedVersions());
    }

    public void testParticipatingReaderCanSeeReleasedVersions() throws Exception {
        assertTrue(
            actual(createSuiteRoleMembership(DATA_READER).forSites(nu).forAllStudies()).
                getCanSeeReleasedVersions());
    }

    public void testNonParticipatingReaderCannotSeeReleasedVersions() throws Exception {
        assertTrue(
            actual(createSuiteRoleMembership(DATA_READER).forSites(mayo).forAllStudies()).
                getCanSeeReleasedVersions());
    }

    public void testParticipatingStudyTeamAdministratorCanSeeReleasedVersions() throws Exception {
        assertTrue(
            actual(createSuiteRoleMembership(STUDY_TEAM_ADMINISTRATOR).forSites(nu)).
                getCanSeeReleasedVersions());
    }

    public void testAssignerCanSeeReleasedVersions() throws Exception {
        assertTrue(
            actual(createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forSites(nu).forStudies(study)).
                getCanSeeReleasedVersions());
    }

    ////// HELPERS

    private UserTemplateRelationship actual(SuiteRoleMembership membership) {
        try {
            membership.checkComplete();
        } catch (SuiteAuthorizationValidationException save) {
            fail("Test membership is incomplete.  " + save.getMessage());
        }
        try {
            membership.validate();
        } catch (SuiteAuthorizationValidationException save) {
            fail("Test membership is invalid.  " + save.getMessage());
        }
        return new UserTemplateRelationship(createPscUser("jo", membership), study);
    }
}
