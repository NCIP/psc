package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.PscUserBuilder;
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
import java.util.List;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.*;
import static org.easymock.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class SingleStudyStudyTeamMemberCommandTest extends WebTestCase {
    private StudyDao studyDao;
    private ProvisioningSessionFactory psFactory;

    private PscUser alice, barbara;
    private Study studyA, studyB;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        studyA = Fixtures.createBasicTemplate("A");
        studyB = Fixtures.createBasicTemplate("B");

        alice = new PscUserBuilder("alice").
            add(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forStudies(studyA).toUser();
        barbara = new PscUserBuilder("barbara").
            add(PscRole.DATA_READER).forAllSites().forStudies(studyB).toUser();

        studyDao = registerMockFor(StudyDao.class);
        expect(studyDao.getVisibleStudiesForSiteParticipation((VisibleStudyParameters) notNull())).
            andStubReturn(Arrays.asList(studyA, studyB));
        psFactory = registerMockFor(ProvisioningSessionFactory.class);
    }

    private SingleStudyStudyTeamMemberCommand create(Study study, List<PscUser> team) {
        return create(study, team, new PscUserBuilder().
            add(PscRole.STUDY_TEAM_ADMINISTRATOR).forAllSites().
            toUser());
    }

    private SingleStudyStudyTeamMemberCommand create(
        Study study, List<PscUser> target, PscUser teamAdmin
    ) {
        return SingleStudyStudyTeamMemberCommand.create(
            study, target, psFactory, applicationSecurityManager, studyDao, teamAdmin);
    }

    ////// CREATE

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
        SingleStudyStudyTeamMemberCommand command =
            create(studyA, Arrays.asList(alice), teamAdmin);

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

        assertFalse("Should not be able to provision all participating studies",
            command.getCanProvisionParticipationInAllStudies());
        assertEquals("Should be able to provision the specific requested study",
            1, command.getProvisionableParticipatingStudies().size());
        assertEquals("Should be able to provision the specific requested study",
            "A", command.getStudy().getAssignedIdentifier());

        verifyMocks();
    }

    public void testCreateForOtherRoleFails() throws Exception {
        try {
            create(studyB, Arrays.asList(barbara), createPscUser("jo", PscRole.USER_ADMINISTRATOR));
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException scse) {
            assertEquals("jo is not authorized for this operation", scse.getMessage());
        }
    }

    public void testCreateForUnadministerableStudy() throws Exception {
        VisibleStudyParameters expectedParams = new VisibleStudyParameters();
        PscUser teamAdmin = registerMockFor(PscUser.class);
        expect(teamAdmin.getMembership(PscRole.STUDY_TEAM_ADMINISTRATOR)).
            andReturn(new SuiteRoleMembership(SuiteRole.STUDY_TEAM_ADMINISTRATOR, null, null));
        expect(teamAdmin.getVisibleStudyParameters(PscRole.STUDY_TEAM_ADMINISTRATOR)).
            andReturn(expectedParams);
        expect(teamAdmin.getUsername()).andReturn("jj");
        expect(studyDao.getVisibleStudiesForSiteParticipation(expectedParams)).
            andReturn(Arrays.asList(studyB));

        replayMocks();
        try {
            create(studyA, Arrays.asList(alice), teamAdmin);
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException scse) {
            assertEquals("jj is not authorized for this operation", scse.getMessage());
        }
    }

    public void testBuildMemberships() throws Exception {
        replayMocks();
        SingleStudyStudyTeamMemberCommand command = create(studyA, Arrays.asList(alice, barbara));
        verifyMocks();

        assertEquals("Wrong number of rows", 2, command.getTeamMemberships().size());
        Iterator<Map.Entry<PscUser, Map<PscRole,StudyTeamRoleMembership>>> it =
            command.getTeamMemberships().entrySet().iterator();
        assertMembershipRow("Wrong first entry", it.next(), alice,
            new MembershipRowExpectation("Wrong entry 0, 0",
                PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, true, true, false),
            new MembershipRowExpectation("Wrong entry 0, 1",
                PscRole.DATA_READER, false, false, false));
        assertMembershipRow("Wrong second entry", it.next(), barbara,
            new MembershipRowExpectation("Wrong entry 1, 0",
                PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, false, false, false),
            new MembershipRowExpectation("Wrong entry 1, 1",
                PscRole.DATA_READER, true, false, false));
    }

    public void testBuildMembershipsForUserWithAllAccess() throws Exception {
        alice.getMembership(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forAllStudies();

        replayMocks();
        SingleStudyStudyTeamMemberCommand command = create(studyA, Arrays.asList(alice));
        verifyMocks();

        assertEquals("Wrong number of rows", 1, command.getTeamMemberships().size());
        Iterator<Map.Entry<PscUser, Map<PscRole,StudyTeamRoleMembership>>> it =
            command.getTeamMemberships().entrySet().iterator();
        assertMembershipRow("Wrong first entry", it.next(), alice,
            new MembershipRowExpectation("Wrong entry 0, 0",
                PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, true, false, true),
            new MembershipRowExpectation("Wrong entry 0, 1",
                PscRole.DATA_READER, false, false, false));
    }

    private void assertMembershipRow(
        String message,
        Map.Entry<PscUser, Map<PscRole, StudyTeamRoleMembership>> actual,
        PscUser expectedUser, MembershipRowExpectation... expectations
    ) {
        assertEquals(message + ": wrong user", expectedUser, actual.getKey());
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
        private boolean expectedAllStudiesForRole;

        private MembershipRowExpectation(
            String message, PscRole expectedRole,
            boolean expectedToHaveRole, boolean expectedScopeIncluded,
            boolean expectedAllStudiesForRole
        ) {
            this.message = message;
            this.expectedRole = expectedRole;
            this.expectedToHaveRole = expectedToHaveRole;
            this.expectedScopeIncluded = expectedScopeIncluded;
            this.expectedAllStudiesForRole = expectedAllStudiesForRole;
        }

        public void assertAgainst(
            Map.Entry<PscRole, StudyTeamRoleMembership> actual
        ) {
            assertEquals(message + ": Wrong role as key", actual.getKey(), expectedRole);
            assertEquals(message + ": Wrong role", actual.getValue().getRole(), expectedRole);
            assertEquals(message + ": Wrong havedness",
                expectedToHaveRole, actual.getValue().getHasRole());
            assertEquals(message + ": Wrong scope inclusion",
                expectedScopeIncluded, actual.getValue().isScopeIncluded());
            assertEquals(message + ": Wrong all scope status",
                expectedAllStudiesForRole, actual.getValue().isAllStudiesForRole());
        }
    }
}
