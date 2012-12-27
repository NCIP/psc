/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.PscUserBuilder;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.VisibleStudyParameters;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSessionFactory;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.*;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class SingleMemberStudyTeamMemberCommandTest extends WebTestCase {
    private ProvisioningSessionFactory psFactory;
    private StudyDao studyDao;

    private PscUser teamMember;
    private Study studyA, studyB, studyC;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        teamMember = createPscUser("sam", 15L);

        studyA = Fixtures.createBasicTemplate("A");
        studyB = Fixtures.createBasicTemplate("B");
        studyC = Fixtures.createBasicTemplate("C");

        psFactory = registerMockFor(ProvisioningSessionFactory.class);
        studyDao = registerMockFor(StudyDao.class);
        expect(studyDao.getVisibleStudiesForSiteParticipation((VisibleStudyParameters) notNull())).
            andStubReturn(Arrays.asList(studyA, studyB));

        PscUser principal = new PscUserBuilder("zelda").setCsmUserId(99L).
            add(PscRole.STUDY_TEAM_ADMINISTRATOR).forAllSites().toUser();
        SecurityContextHolderTestHelper.setSecurityContext(principal);
    }

    private SingleMemberStudyTeamMemberCommand create(PscUser target) {
        return create(target, new PscUserBuilder().
            add(PscRole.STUDY_TEAM_ADMINISTRATOR).forAllSites().
            toUser());
    }

    private SingleMemberStudyTeamMemberCommand create(PscUser target, PscUser teamAdmin) {
        return SingleMemberStudyTeamMemberCommand.create(
            target, psFactory, applicationSecurityManager, studyDao, teamAdmin);
    }

    ////// create

    public void testCreateForStudyTeamAdmin() throws Exception {
        VisibleStudyParameters expectedParams = new VisibleStudyParameters();
        PscUser teamAdmin = registerMockFor(PscUser.class);
        expect(teamAdmin.getMembership(PscRole.STUDY_TEAM_ADMINISTRATOR)).
            andReturn(new SuiteRoleMembership(SuiteRole.STUDY_TEAM_ADMINISTRATOR, null, null));
        expect(teamAdmin.getVisibleStudyParameters(PscRole.STUDY_TEAM_ADMINISTRATOR)).
            andReturn(expectedParams);
        expect(studyDao.getVisibleStudiesForSiteParticipation(expectedParams)).
            andReturn(Arrays.asList(studyB, studyA));

        replayMocks();
        SingleMemberStudyTeamMemberCommand command = create(teamMember, teamAdmin);

        assertEquals("Wrong number of provisionable roles: " + command.getProvisionableRoles(), 2,
            command.getProvisionableRoles().size());
        assertContains("Missing SSCM", command.getProvisionableRoles(),
            new ProvisioningRole(SuiteRole.STUDY_SUBJECT_CALENDAR_MANAGER));
        assertContains("Missing Data Reader", command.getProvisionableRoles(),
            new ProvisioningRole(SuiteRole.DATA_READER));

        assertFalse("Should not be able to provision all sites", command.getCanProvisionAllSites());
        assertEquals("Should not be able to provision any sites",
            0, command.getProvisionableSites().size());
        assertFalse("Should not be able to provision managed studies",
            command.getCanProvisionManagementOfAllStudies());
        assertEquals("Should not be able to provision any managed studies",
            0, command.getProvisionableManagedStudies().size());

        assertTrue("Should be able to provision all participating studies",
            command.getCanProvisionParticipationInAllStudies());
        assertEquals("Should be able to provision all participating studies, specifically",
            2, command.getProvisionableParticipatingStudies().size());

        verifyMocks();
    }

    public void testCreateForOtherRoleFails() throws Exception {
        try {
            create(teamMember, createPscUser("jo", PscRole.USER_ADMINISTRATOR));
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException scse) {
            assertEquals("jo is not authorized for this operation", scse.getMessage());
        }
    }

    public void testBuildsMembershipsWithCorrectScope() throws Exception {
        PscUser member = new PscUserBuilder().
            add(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forStudies(studyA).
            add(PscRole.DATA_READER).forAllSites().forStudies(studyB).
            toUser();

        replayMocks();
        SingleMemberStudyTeamMemberCommand actual = create(member);
        verifyMocks();

        assertEquals("Wrong number of entries", 3, actual.getTeamMemberships().size());
        Iterator<Map.Entry<String, Map<PscRole, StudyTeamRoleMembership>>> it =
            actual.getTeamMemberships().entrySet().iterator();
        assertMembershipRow("Wrong all entry", it.next(), "__ALL__",
            new MembershipRowExpectation("Wrong entry 0, 0", PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, true, false),
            new MembershipRowExpectation("Wrong entry 0, 1", PscRole.DATA_READER, true, false));
        assertMembershipRow("Wrong first entry", it.next(), "A",
            new MembershipRowExpectation("Wrong entry 1, 0", PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, true, true),
            new MembershipRowExpectation("Wrong entry 1, 1", PscRole.DATA_READER, true, false));
        assertMembershipRow("Wrong second entry", it.next(), "B",
            new MembershipRowExpectation("Wrong entry 2, 0", PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, true, false),
            new MembershipRowExpectation("Wrong entry 2, 1", PscRole.DATA_READER, true, true));
    }

    public void testBuildMembershipsIgnoresOtherRoles() throws Exception {
        PscUser member = new PscUserBuilder().
            add(PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER).forAllSites().forStudies(studyA).
            add(PscRole.DATA_READER).forAllSites().forStudies(studyB).
            toUser();

        replayMocks();
        SingleMemberStudyTeamMemberCommand actual = create(member);
        verifyMocks();

        assertEquals("Wrong number of entries: " + actual.getTeamMemberships(),
            3, actual.getTeamMemberships().size());
        Iterator<Map.Entry<String, Map<PscRole, StudyTeamRoleMembership>>> it =
            actual.getTeamMemberships().entrySet().iterator();
        assertMembershipRow("Wrong all entry", it.next(), "__ALL__",
            new MembershipRowExpectation("Wrong entry 0, 0", PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, false, false),
            new MembershipRowExpectation("Wrong entry 0, 1", PscRole.DATA_READER, true, false));
        assertMembershipRow("Wrong first entry", it.next(), "A",
            new MembershipRowExpectation("Wrong entry 1, 0", PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, false, false),
            new MembershipRowExpectation("Wrong entry 1, 1", PscRole.DATA_READER, true, false));
        assertMembershipRow("Wrong second entry", it.next(), "B",
            new MembershipRowExpectation("Wrong entry 2, 0", PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, false, false),
            new MembershipRowExpectation("Wrong entry 2, 1", PscRole.DATA_READER, true, true));
    }

    public void testBuildMembershipsIgnoresOtherStudies() throws Exception {
        // provisioner doesn't have access to C
        PscUser member = new PscUserBuilder().
            add(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forStudies(studyA, studyC).
            toUser();

        replayMocks();
        SingleMemberStudyTeamMemberCommand actual = create(member);
        verifyMocks();

        assertEquals("Wrong number of entries", 3, actual.getTeamMemberships().size());
        Iterator<Map.Entry<String, Map<PscRole, StudyTeamRoleMembership>>> it
            = actual.getTeamMemberships().entrySet().iterator();
        assertMembershipRow("Wrong all entry", it.next(), "__ALL__",
            new MembershipRowExpectation("Wrong entry 0, 0", PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, true, false),
            new MembershipRowExpectation("Wrong entry 0, 1", PscRole.DATA_READER, false, false));
        assertMembershipRow("Wrong first entry", it.next(), "A",
            new MembershipRowExpectation("Wrong entry 1, 0", PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, true, true),
            new MembershipRowExpectation("Wrong entry 1, 1", PscRole.DATA_READER, false, false));
        assertMembershipRow("Wrong first entry", it.next(), "B",
            new MembershipRowExpectation("Wrong entry 2, 0", PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, true, false),
            new MembershipRowExpectation("Wrong entry 2, 1", PscRole.DATA_READER, false, false));
    }

    public void testBuildMembershipForAll() throws Exception {
        // provisioner doesn't have access to C
        PscUser member = new PscUserBuilder().
            add(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forAllStudies().
            toUser();

        replayMocks();
        SingleMemberStudyTeamMemberCommand actual = create(member);
        verifyMocks();

        assertEquals("Wrong number of entries", 3, actual.getTeamMemberships().size());
        Iterator<Map.Entry<String, Map<PscRole, StudyTeamRoleMembership>>> it
            = actual.getTeamMemberships().entrySet().iterator();
        assertMembershipRow("Wrong all entry", it.next(), "__ALL__",
            new MembershipRowExpectation("Wrong entry 0, 0", PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, true, true),
            new MembershipRowExpectation("Wrong entry 0, 1", PscRole.DATA_READER, false, false));
        assertMembershipRow("Wrong first entry", it.next(), "A",
            new MembershipRowExpectation("Wrong entry 1, 0", PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, true, false),
            new MembershipRowExpectation("Wrong entry 1, 1", PscRole.DATA_READER, false, false));
        assertMembershipRow("Wrong first entry", it.next(), "B",
            new MembershipRowExpectation("Wrong entry 2, 0", PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, true, false),
            new MembershipRowExpectation("Wrong entry 2, 1", PscRole.DATA_READER, false, false));
    }

    private void assertMembershipRow(
        String message,
        Map.Entry<String, Map<PscRole, StudyTeamRoleMembership>> actual,
        String expectedStudy, MembershipRowExpectation... expectations
    ) {
        assertEquals(message + ": wrong study", expectedStudy, actual.getKey());
        assertEquals("Wrong number of rows: " + actual.getValue(),
            expectations.length, actual.getValue().size());
        Iterator<Map.Entry<PscRole, StudyTeamRoleMembership>> it =
            actual.getValue().entrySet().iterator();
        for (MembershipRowExpectation expectation : expectations) {
            expectation.assertAgainst(it.next());
        }
    }

    private static class MembershipRowExpectation {
        private String message;
        private PscRole expectedRole;
        private boolean expectedToHaveRole;
        private boolean expectedScopeIncluded;

        private MembershipRowExpectation(
            String message, PscRole expectedRole,
            boolean expectedToHaveRole, boolean expectedScopeIncluded
        ) {
            this.message = message;
            this.expectedRole = expectedRole;
            this.expectedToHaveRole = expectedToHaveRole;
            this.expectedScopeIncluded = expectedScopeIncluded;
        }

        public void assertAgainst(
            Map.Entry<PscRole, StudyTeamRoleMembership> actual
        ) {
            assertEquals(message + ": Wrong role as key", actual.getKey(), expectedRole);
            assertEquals(message + ": Wrong role", actual.getValue().getRole(), expectedRole);
            assertEquals(message + ": Wrong havedness",
                actual.getValue().getHasRole(), expectedToHaveRole);
            assertEquals(message + ": Wrong scope inclusion",
                actual.getValue().isScopeIncluded(), expectedScopeIncluded);
        }
    }
}
