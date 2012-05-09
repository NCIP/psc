package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.PscUserBuilder;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySubjectAssignmentRelationship;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static org.easymock.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class ResponsibleUserForSubjectAssignmentCommandTest extends WebTestCase {
    private ResponsibleUserForSubjectAssignmentCommand command;

    private PscUserService pscUserService;
    private StudySubjectAssignmentDao assignmentDao;

    private PscUser jo, alice, barbara;
    private Site nu, vanderbilt, mayo;
    private Study a, b, c;
    private StudySite a_nu, c_vu;
    private StudySubjectAssignment a_nu_1, a_nu_2, c_vu_1, c_m_1, c_m_2;
    private Errors errors;
    private Configuration configuration;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        nu = createSite("NU", "IL036");
        vanderbilt = createSite("VU", "TN054");
        mayo = createSite("Mayo", "MN729");

        a = createBasicTemplate("A");
        b = createBasicTemplate("B");
        c = createBasicTemplate("C");

        a_nu = createStudySite(a, nu);
        a_nu.approveAmendment(a.getAmendment(), new Date());
        createStudySite(b, nu).approveAmendment(b.getAmendment(), new Date());

        createStudySite(b, vanderbilt).approveAmendment(b.getAmendment(), new Date());
        c_vu = createStudySite(c, vanderbilt);
        c_vu.approveAmendment(c.getAmendment(), new Date());

        StudySite c_mayo = createStudySite(c, mayo);
        c_mayo.approveAmendment(c.getAmendment(), new Date());
        createStudySite(a, mayo).approveAmendment(a.getAmendment(), new Date());

        a_nu_1 = createAssignment(a_nu, createSubject("One", "Eins"));
        a_nu_2 = createAssignment(a_nu, createSubject("Two", "Zwei"));
        a_nu_1.setManagerCsmUserId(1);
        a_nu_2.setManagerCsmUserId(null);

        c_vu_1 = createAssignment(c_vu, createSubject("One", "Eins"));
        c_vu_1.setManagerCsmUserId(2);

        c_m_1 = createAssignment(c_mayo, createSubject("One", "Eins"));
        c_m_2 = createAssignment(c_mayo, createSubject("Two", "Zwei"));
        c_m_1.setManagerCsmUserId(2);
        c_m_2.setManagerCsmUserId(null);

        jo = new PscUserBuilder("jo").setCsmUserId(100).
            add(STUDY_TEAM_ADMINISTRATOR).forSites(nu, vanderbilt).
            toUser();
        alice = new PscUserBuilder("alice").setCsmUserId(1).
            add(STUDY_SUBJECT_CALENDAR_MANAGER).forStudies(a, c).forSites(nu, vanderbilt).
            toUser();
        barbara = new PscUserBuilder("barbara").setCsmUserId(2).
            add(STUDY_SUBJECT_CALENDAR_MANAGER).forStudies(b, c).forSites(vanderbilt, mayo).
            toUser();

        pscUserService = registerMockFor(PscUserService.class);
        expect(pscUserService.getColleaguesOf(jo, STUDY_SUBJECT_CALENDAR_MANAGER, STUDY_TEAM_ADMINISTRATOR)).
            andStubReturn(Arrays.asList(alice, barbara));
        expectStubGetManagedAssignments(alice, a_nu_1);
        expectStubGetManagedAssignments(barbara, c_vu_1, c_m_1);

        assignmentDao = registerMockFor(StudySubjectAssignmentDao.class);
        expect(assignmentDao.getAssignmentsWithoutManagerCsmUserId()).
            andStubReturn(Arrays.asList(a_nu_2, c_m_2));

        configuration = registerMockFor(Configuration.class);
        expect(configuration.get(Configuration.ENABLE_ASSIGNING_SUBJECT)).andStubReturn(true);

        command = new ResponsibleUserForSubjectAssignmentCommand(jo, pscUserService, assignmentDao, configuration);
        errors = new BindException(command, "command");
    }

    private void expectStubGetManagedAssignments(
        PscUser sscm, StudySubjectAssignment... assignments
    ) {
        expect(pscUserService.getDesignatedManagedAssignments(sscm)).
            andStubReturn(createUSSARs(sscm, assignments));
    }

    private List<UserStudySubjectAssignmentRelationship> createUSSARs(
        PscUser user, StudySubjectAssignment... assignments
    ) {
        List<UserStudySubjectAssignmentRelationship> wrapped =
            new ArrayList<UserStudySubjectAssignmentRelationship>(assignments.length);
        for (StudySubjectAssignment assignment : assignments) {
            wrapped.add(new UserStudySubjectAssignmentRelationship(user, assignment));
        }
        return wrapped;
    }

    ////// reassignables

    public void testReassignablesIncludesAssignmentsForSelectedUserOnly() throws Exception {
        command.setResponsible(alice);

        replayMocks();
        Map<Site, Map<Study, ResponsibleUserForSubjectAssignmentCommand.ReassignableAssignments>> actual =
            command.getReassignables();
        verifyMocks();

        assertEquals("Wrong number of sites", 1, actual.size());
        assertSame("Wrong site", nu, actual.keySet().iterator().next());
        assertEquals("Wrong number of studies", 1, actual.get(nu).size());
        assertSame("Wrong study", a, actual.get(nu).keySet().iterator().next());

        ResponsibleUserForSubjectAssignmentCommand.ReassignableAssignments actualRa =
            actual.get(nu).get(a);
        assertEquals("Wrong study site", a_nu, actualRa.getStudySite());
        assertEquals("Wrong eligible users", Arrays.<PscUser>asList(), actualRa.getEligibleUsers());
        assertEquals("Wrong assignments", Arrays.asList(a_nu_1), actualRa.getAssignments());
    }

    public void testReassignablesIncludesAllReassignableUnassignedWhenNoResponsibleUser() throws Exception {
        command.setResponsible(null);

        replayMocks();
        Map<Site, Map<Study, ResponsibleUserForSubjectAssignmentCommand.ReassignableAssignments>> actual =
            command.getReassignables();
        verifyMocks();

        assertEquals("Wrong number of sites", 1, actual.size());
        assertSame("Wrong site", nu, actual.keySet().iterator().next());
        assertEquals("Wrong number of studies", 1, actual.get(nu).size());
        assertSame("Wrong study", a, actual.get(nu).keySet().iterator().next());

        ResponsibleUserForSubjectAssignmentCommand.ReassignableAssignments actualRa =
            actual.get(nu).get(a);
        assertEquals("Wrong study site", a_nu, actualRa.getStudySite());
        assertEquals("Wrong eligible users", Arrays.asList(alice), actualRa.getEligibleUsers());
        assertEquals("Wrong assignments", Arrays.asList(a_nu_2), actualRa.getAssignments());
    }

    public void testReassignablesExcludesNonassignableAssignmentsForSelectedUser() throws Exception {
        command.setResponsible(barbara);

        replayMocks();
        Map<Site, Map<Study, ResponsibleUserForSubjectAssignmentCommand.ReassignableAssignments>> actual =
            command.getReassignables();
        verifyMocks();

        // excluding the mayo assignments which are invisible to jo
        assertEquals("Wrong number of sites", 1, actual.size());
        assertSame("Wrong site", vanderbilt, actual.keySet().iterator().next());
        assertEquals("Wrong number of studies", 1, actual.get(vanderbilt).size());
        assertSame("Wrong study", c, actual.get(vanderbilt).keySet().iterator().next());

        ResponsibleUserForSubjectAssignmentCommand.ReassignableAssignments actualRa =
            actual.get(vanderbilt).get(c);
        assertEquals("Wrong study site", c_vu, actualRa.getStudySite());
        assertEquals("Wrong eligible users", Arrays.asList(alice), actualRa.getEligibleUsers());
        assertEquals("Wrong assignments", Arrays.asList(c_vu_1), actualRa.getAssignments());
    }

    // #2049
    public void testReassignablesIncludesAllReassignableUsersWhenSubjectAssignmentIsDisabled() throws Exception {
        expect(configuration.get(Configuration.ENABLE_ASSIGNING_SUBJECT)).
            andReturn(false).anyTimes();

        replayMocks();
        Map<Site, Map<Study, ResponsibleUserForSubjectAssignmentCommand.ReassignableAssignments>> actual =
            command.getReassignables();
        verifyMocks();

        ResponsibleUserForSubjectAssignmentCommand.ReassignableAssignments actualRa =
            actual.get(nu).get(a);
        assertEquals("Wrong eligible users", Arrays.asList(alice), actualRa.getEligibleUsers());
    }

    ////// validate

    public void testInvalidIfSettingAssignmentNotVisible() throws Exception {
        command.setResponsible(null);
        command.setNewResponsible(barbara);
        command.setTargetAssignments(Arrays.asList(c_m_2));

        replayMocks();
        command.validate(errors);
        verifyMocks();

        assertFieldErrorCount(1, "targetAssignments");
    }

    public void testInvalidIfNoTargetAssignments() throws Exception {
        command.setResponsible(null);
        command.setNewResponsible(barbara);
        command.setTargetAssignments(Collections.<StudySubjectAssignment>emptyList());

        replayMocks();
        command.validate(errors);
        verifyMocks();

        assertFieldErrorCount(1, "targetAssignments");
    }

    public void testInvalidIfSettingUserIneligible() throws Exception {
        command.setResponsible(alice);
        command.setNewResponsible(barbara);
        command.setTargetAssignments(Arrays.asList(a_nu_1));

        replayMocks();
        command.validate(errors);
        verifyMocks();

        assertFieldErrorCount(1, "newResponsible");
    }

    public void testInvalidIfSettingUserBlank() throws Exception {
        command.setResponsible(alice);
        command.setNewResponsible(null);
        command.setTargetAssignments(Arrays.asList(a_nu_1));

        replayMocks();
        command.validate(errors);
        verifyMocks();

        assertFieldErrorCount(1, "newResponsible");
    }

    public void testValidForValidRequest() throws Exception {
        command.setResponsible(barbara);
        command.setNewResponsible(alice);
        command.setTargetAssignments(Arrays.asList(c_vu_1));

        replayMocks();
        command.validate(errors);
        verifyMocks();

        assertFalse("Should be no errors: " + errors, errors.hasErrors());
    }

    private void assertFieldErrorCount(int expectedCount, String field) {
        assertEquals("Wrong number of errors in " + field + ": " + errors.getFieldErrors(field),
            expectedCount, errors.getFieldErrorCount(field));
    }

    ////// apply

    public void testApply() throws Exception {
        command.setResponsible(barbara);
        command.setNewResponsible(alice);
        command.setTargetAssignments(Arrays.asList(c_vu_1));

        assignmentDao.save(c_vu_1);

        replayMocks();
        command.apply();
        verifyMocks();

        assertEquals("Responsible user not changed",
            alice.getCsmUser().getUserId().longValue(),
            c_vu_1.getManagerCsmUserId().longValue());
    }
}
