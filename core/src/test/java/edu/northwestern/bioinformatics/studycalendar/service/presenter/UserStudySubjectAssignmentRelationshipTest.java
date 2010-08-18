package edu.northwestern.bioinformatics.studycalendar.service.presenter;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteAuthorizationValidationException;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import junit.framework.TestCase;

import java.util.Date;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.AuthorizationScopeMappings.*;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.*;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Rhett Sutphin
 */
public class UserStudySubjectAssignmentRelationshipTest extends TestCase {
    private static final long CSM_USER_ID = 14L;

    private Study study, otherStudy;
    private Site nu, mayo;
    private StudySubjectAssignment assignment;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        study = createBasicTemplate("F");
        otherStudy = createBasicTemplate("O");

        nu = createSite("NU", "IL099");
        mayo = createSite("Mayo", "MN008");

        StudySite mayoF = study.addSite(mayo);
        mayoF.approveAmendment(study.getAmendment(), new Date());
        StudySite nuF = study.addSite(nu);
        nuF.approveAmendment(study.getAmendment(), new Date());

        assignment = createAssignment(nuF, createSubject("A", "B"));
    }

    ////// isVisible

    public void testVisibleToExactMatchingStudyTeamAdministrator() throws Exception {
        assertTrue(actual(
            createSuiteRoleMembership(STUDY_TEAM_ADMINISTRATOR).forSites(nu)).
            isVisible());
    }

    public void testNotVisibleToWrongSiteStudyTeamAdministrator() throws Exception {
        assertFalse(actual(
            createSuiteRoleMembership(STUDY_TEAM_ADMINISTRATOR).forSites(mayo)).
            isVisible());
    }

    public void testVisibleToExactMatchingStudySubjectCalendarManager() throws Exception {
        assertTrue(actual(
            createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forSites(nu).forStudies(study)).
            isVisible());
    }

    public void testNotVisibleToWrongStudyStudySubjectCalendarManager() throws Exception {
        assertFalse(actual(
            createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forStudies(otherStudy)).
            isVisible());
    }

    public void testNotVisibleToWrongSiteStudySubjectCalendarManager() throws Exception {
        assertFalse(actual(
            createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forSites(mayo).forAllStudies()).
            isVisible());
    }

    public void testVisibleToExactMatchingDataReader() throws Exception {
        assertTrue(actual(
            createSuiteRoleMembership(DATA_READER).forSites(nu).forStudies(study)).
            isVisible());
    }

    public void testNotVisibleToWrongStudyDataReader() throws Exception {
        assertFalse(actual(
            createSuiteRoleMembership(DATA_READER).forAllSites().forStudies(otherStudy)).
            isVisible());
    }

    public void testNotVisibleToWrongSiteDataReader() throws Exception {
        assertFalse(actual(
            createSuiteRoleMembership(DATA_READER).forSites(mayo).forAllStudies()).
            isVisible());
    }

    public void testNotVisibleToOtherRoles() throws Exception {
        assertFalse(actual(createSuiteRoleMembership(STUDY_QA_MANAGER).forAllSites()).isVisible());
    }

    ////// canUpdateSchedule

    public void testMatchingStudySubjectCalendarManagerCanUpdateSchedule() throws Exception {
        assertTrue(actual(
            createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forSites(nu).forStudies(study)).
            getCanUpdateSchedule());
    }

    public void testNonMatchingStudySubjectCalendarManagerCannotUpdateSchedule() throws Exception {
        assertFalse(actual(
            createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forStudies(otherStudy)).
            getCanUpdateSchedule());
    }

    public void testOtherRolesCannotUpdateSchedule() throws Exception {
        assertFalse(actual(
            createSuiteRoleMembership(STUDY_TEAM_ADMINISTRATOR).forAllSites()).
            getCanUpdateSchedule());
    }

    ////// isCalendarManager

    public void testIsCalendarManagerWhenIs() throws Exception {
        assignment.setManagerCsmUserId((int) CSM_USER_ID);
        assertTrue(actual(
            createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forAllStudies()).
            isCalendarManager());
    }

    public void testIsCalendarManagerWhenIsNot() throws Exception {
        assignment.setManagerCsmUserId((int) CSM_USER_ID - 7);
        assertFalse(actual(
            createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forAllStudies()).
            isCalendarManager());
    }

    public void testIsCalendarManagerWhenNone() throws Exception {
        assignment.setManagerCsmUserId(null);
        assertFalse(actual(
            createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forAllStudies()).
            isCalendarManager());
    }

    ////// canSetCalendarManager

    public void testCanSetCalendarManagerWhenMatchingTeamAdmin() throws Exception {
        assertTrue(actual(
            createSuiteRoleMembership(STUDY_TEAM_ADMINISTRATOR).forSites(nu)).
            getCanSetCalendarManager());
    }

    public void testCanSetCalendarManagerWhenTeamAdminForAllSites() throws Exception {
        assertTrue(actual(
            createSuiteRoleMembership(STUDY_TEAM_ADMINISTRATOR).forAllSites()).
            getCanSetCalendarManager());
    }

    public void testCannotSetCalendarManagerWhenTeamAdminForOtherSite() throws Exception {
        assertFalse(actual(
            createSuiteRoleMembership(STUDY_TEAM_ADMINISTRATOR).forSites(mayo)).
            getCanSetCalendarManager());
    }

    public void testCannotSetCalendarManagerWhenOtherRole() throws Exception {
        assertFalse(actual(
            createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forAllStudies().forSites(nu)).
            getCanSetCalendarManager());
    }

    ////// HELPERS

    private UserStudySubjectAssignmentRelationship actual(SuiteRoleMembership membership) {
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
        return new UserStudySubjectAssignmentRelationship(
            createPscUser("jo", CSM_USER_ID, membership), assignment);
    }
}
