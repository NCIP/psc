/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.presenter;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteAuthorizationValidationException;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import junit.framework.TestCase;

import java.util.Date;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationScopeMappings.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSite;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.createPscUser;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Rhett Sutphin
 */
public class UserRelationshipToolsTest extends TestCase {
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

    public void testDataImporterIsManaging() throws Exception {
        study.addManagingSite(nu);
        assertTrue(actual(createSuiteRoleMembership(DATA_IMPORTER)).
            isManagingAsOneOf(DATA_IMPORTER));
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

    ////// HELPERS

    private UserRelationshipTools actual(SuiteRoleMembership membership) {
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
        return new UserRelationshipTools(createPscUser("jo", membership), study);
    }
}
