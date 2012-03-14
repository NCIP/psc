package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.PscUserBuilder;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.nwu.bioinformatics.commons.DateUtils;
import gov.nih.nci.cabig.ctms.suite.authorization.ScopeType;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Rhett Sutphin
 */
public class ScheduleActivityCommandTest extends WebTestCase {
    private static final String NEW_REASON = "New Reason";
    private static final Date NEW_DATE = DateUtils.createDate(2003, Calendar.MARCH, 14);

    private ScheduleActivityCommand command;

    private ScheduledCalendarDao scheduledCalendarDao;

    private Site site;
    private Study study;
    private ScheduledActivity event;

    private PscUser user;

    protected void setUp() throws Exception {
        super.setUp();
        user = new PscUserBuilder("eileen").
            add(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forAllStudies().toUser();

        scheduledCalendarDao = registerMockFor(ScheduledCalendarDao.class);
        command = new ScheduleActivityCommand(scheduledCalendarDao, user);

        site = Fixtures.createSite("NU");
        study = Fixtures.createSingleEpochStudy("ABC 4678", "Follow up");
        StudySite studySite = Fixtures.createStudySite(study, site);
        StudySubjectAssignment assignment = Fixtures.createAssignment(
            studySite, Fixtures.createSubject("Suzy", "Subject"));

        event = Fixtures.createScheduledActivity("ABC", 2003, Calendar.MARCH, 13);
        event.setScheduledStudySegment(new ScheduledStudySegment());
        event.getScheduledStudySegment().setScheduledCalendar(new ScheduledCalendar());
        event.getScheduledStudySegment().getScheduledCalendar().setAssignment(assignment);

        command.setEvent(event);
        command.setNewReason(NEW_REASON);
        command.setNewDate(NEW_DATE);
    }

    public void testCreateCanceledState() throws Exception {
        command.setNewMode(ScheduledActivityMode.CANCELED);
        replayMocks();

        ScheduledActivityState created = command.createState();
        assertEquals(ScheduledActivityMode.CANCELED, created.getMode());
        assertEquals(NEW_REASON, created.getReason());
    }

    public void testCreateScheduledState() throws Exception {
        command.setNewMode(ScheduledActivityMode.SCHEDULED);
        replayMocks();

        ScheduledActivityState created = command.createState();
        assertEquals(ScheduledActivityMode.SCHEDULED, created.getMode());
        assertEquals(NEW_REASON, created.getReason());
        assertEquals(NEW_DATE, (created).getDate());
    }
    
    public void testCreateOccurredState() throws Exception {
        command.setNewMode(ScheduledActivityMode.OCCURRED);
        replayMocks();

        ScheduledActivityState created = command.createState();
        assertEquals(ScheduledActivityMode.OCCURRED, created.getMode());
        assertEquals(NEW_REASON, created.getReason());
        assertEquals(NEW_DATE, (created).getDate());
    }

    public void testChangeState() throws Exception {
        assertEquals("Unexpected number of initial states", 1, event.getAllStates().size());
        assertNull(event.getNotes());

        command.setNewMode(ScheduledActivityMode.OCCURRED);
        command.setNewNotes("Change-o");
        scheduledCalendarDao.save(event.getScheduledStudySegment().getScheduledCalendar());

        replayMocks();
        command.apply();
        verifyMocks();
        assertEquals("Wrong number of states", 2, event.getAllStates().size());
        assertEquals("Wrong mode for current state", ScheduledActivityMode.OCCURRED, event.getCurrentState().getMode());
        assertEquals("Wrong reason for current state", NEW_REASON, event.getCurrentState().getReason());
        assertEquals("Wrong notes", "Change-o", event.getNotes());
    }

    public void testEventSpecificModesForNonConditionalEvent() throws Exception {
        command.setNewMode(ScheduledActivityMode.SCHEDULED);
        replayMocks();
        Collection<ScheduledActivityMode> collection = command.getEventSpecificMode();
        System.out.println("collection " + collection);
        assertEquals("Wrong number of modes", 4, collection.size());
        assertNotContains("Wrong state available", collection, ScheduledActivityMode.NOT_APPLICABLE);
        assertNotContains("Wrong state available", collection, ScheduledActivityMode.CONDITIONAL);
    }


    public void testEventSpecificModesForConditionalEvent() throws Exception {
        ScheduledActivity conditionalEvent = Fixtures.createConditionalEvent("ABC", 2003, Calendar.MARCH, 13);
        conditionalEvent.changeState(ScheduledActivityMode.SCHEDULED.createStateInstance(DateUtils.createDate(2003, Calendar.MARCH, 13), "Schedule"));
        command.setEvent(conditionalEvent);
        command.setNewMode(ScheduledActivityMode.SCHEDULED);
        replayMocks();
        Collection<ScheduledActivityMode> collection = command.getEventSpecificMode();
        System.out.println("collection " + collection);
        assertEquals("Wrong number of modes", 4, collection.size());
        assertNotContains("Wrong state available", collection, ScheduledActivityMode.CANCELED);
        assertNotContains("Wrong state available", collection, ScheduledActivityMode.CONDITIONAL);
    }

    public void testChangeTimeWith24HrFormat() throws Exception {
        assertEquals("Unexpected number of initial states", 1, event.getAllStates().size());
        assertNull(event.getNotes());

        command.setNewMode(ScheduledActivityMode.SCHEDULED);
        command.setNewTime("19:25");
        scheduledCalendarDao.save(event.getScheduledStudySegment().getScheduledCalendar());

        replayMocks();
        command.apply();
        verifyMocks();

        assertEquals("Wrong number of states", 2, event.getAllStates().size());
        assertEquals("Wrong mode for current state", ScheduledActivityMode.SCHEDULED, event.getCurrentState().getMode());
        assertEquals("Wrong withTime for current state", (Object) true, event.getCurrentState().getWithTime());
        assertEquals("Wrong Date for current state", DateUtils.createDate(2003, Calendar.MARCH, 14, 19, 25, 00), event.getCurrentState().getDate());
    }

    public void testChangeTimeWitAmPmFormat() throws Exception {
        assertEquals("Unexpected number of initial states", 1, event.getAllStates().size());
        assertNull(event.getNotes());

        command.setNewMode(ScheduledActivityMode.SCHEDULED);
        command.setNewTime("5:15 AM");
        scheduledCalendarDao.save(event.getScheduledStudySegment().getScheduledCalendar());

        replayMocks();
        command.apply();
        verifyMocks();

        assertEquals("Wrong number of states", 2, event.getAllStates().size());
        assertEquals("Wrong mode for current state", ScheduledActivityMode.SCHEDULED, event.getCurrentState().getMode());
        assertEquals("Wrong withTime for current state", (Object) true, event.getCurrentState().getWithTime());
        assertEquals("Wrong Date for current state", DateUtils.createDate(2003, Calendar.MARCH, 14, 5, 15, 00), event.getCurrentState().getDate());
    }

    public void testValidateTimeInWrongFormat() throws Exception {
        command.setNewMode(ScheduledActivityMode.SCHEDULED);
        command.setNewTime("515 AM");

        BindException errors = validateAndReturnErrors();
        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.time.not.valid.format", errors.getGlobalError().getCode());
    }

    public void testAuthorizationLimitsByAssignmentSiteWhenActivityAvailable() throws Exception {
        ResourceAuthorization example = command.authorizations(null).iterator().next();
        assertSame(site.getAssignedIdentifier(), example.getScope(ScopeType.SITE));
    }

    public void testAuthorizationLimitsByAssignmentStudyWhenActivityAvailable() throws Exception {
        ResourceAuthorization example = command.authorizations(null).iterator().next();
        assertSame(study.getAssignedIdentifier(), example.getScope(ScopeType.STUDY));
    }

    public void testAuthorizationsReturnsUnqualifiedAuthorizationsWhenUnresolvableActivity() throws Exception {
        command.setEvent(null);
        ResourceAuthorization example = command.authorizations(null).iterator().next();
        assertNull(example.getScope(ScopeType.SITE));
        assertNull(example.getScope(ScopeType.STUDY));
    }

    public void testAuthorizationAllowsAppropriateRoles() throws Exception {
        Collection<ResourceAuthorization> actual = command.authorizations(null);
        assertRolesAllowed(actual, STUDY_TEAM_ADMINISTRATOR, STUDY_SUBJECT_CALENDAR_MANAGER, DATA_READER);
    }

    public void testIsReadOnlyWithoutSscmRole() throws Exception {
        user.getMemberships().clear();
        user.getMemberships().put(SuiteRole.DATA_READER,
            new SuiteRoleMembership(SuiteRole.DATA_READER, null, null).forAllSites().forAllStudies());

        assertTrue(command.isReadOnly());
    }

    public void testCanWriteWithSscmRole() throws Exception {
        assertFalse(command.isReadOnly());
    }

    public void testApplyDoesNothingWhenReadOnly() throws Exception {
        user.getMemberships().clear();
        user.getMemberships().put(SuiteRole.DATA_READER,
            new SuiteRoleMembership(SuiteRole.DATA_READER, null, null).forAllSites().forAllStudies());

        replayMocks();
        command.apply();
        verifyMocks();
    }

    private BindException validateAndReturnErrors() {
        replayMocks();
        BindException errors = new BindException(command, StringUtils.EMPTY);
        command.validate(errors);
        verifyMocks();
        return errors;
    }
}
