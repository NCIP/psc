package edu.northwestern.bioinformatics.studycalendar.api.impl;

import edu.nwu.bioinformatics.commons.DateUtils;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.service.ParticipantService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreator;
import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ArmDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledEventDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyParticipantAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;
import edu.northwestern.bioinformatics.studycalendar.domain.NextArmMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.AdverseEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Canceled;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;

import java.util.Date;
import java.util.LinkedList;
import java.util.Collection;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class DefaultScheduledCalendarServiceTest extends StudyCalendarTestCase {
    private static final String STUDY_BIG_ID = "STUDY-GUID";
    private static final String SITE_BIG_ID = "SITE-GUID";
    private static final String PARTICIPANT_BIG_ID = "PARTICPANT-GUID";
    private static final String ARM_BIG_ID = "ARM-GUID";
    private static final String SCHEDULED_EVENT_BIG_ID = "EVENT-GUID";
    private static final String ASSIGNMENT_BIG_ID = "ASSIGNMENT-GUID";
    private static final Date START_DATE = new Date();

    private DefaultScheduledCalendarService service;
    private SiteDao siteDao;
    private StudyDao studyDao;
    private ParticipantDao participantDao;
    private ParticipantService participantService;
    private ArmDao armDao;
    private ScheduledCalendarDao scheduledCalendarDao;
    private ScheduledEventDao scheduledEventDao;
    private StudyParticipantAssignmentDao assignmentDao;

    private Study parameterStudy;
    private Site parameterSite;
    private Participant parameterParticipant;
    private Arm parameterArm;
    private ScheduledEvent parameterEvent;
    private StudyParticipantAssignment parameterAssignment;

    private Study loadedStudy;
    private Site loadedSite;
    private Participant loadedParticipant;
    private Arm loadedArm;
    private ScheduledEvent loadedEvent;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        armDao = registerDaoMockFor(ArmDao.class);
        siteDao = registerDaoMockFor(SiteDao.class);
        studyDao = registerDaoMockFor(StudyDao.class);
        participantDao = registerDaoMockFor(ParticipantDao.class);
        participantService = registerMockFor(ParticipantService.class);
        scheduledCalendarDao = registerDaoMockFor(ScheduledCalendarDao.class);
        scheduledEventDao = registerDaoMockFor(ScheduledEventDao.class);
        assignmentDao = registerDaoMockFor(StudyParticipantAssignmentDao.class);

        service = new DefaultScheduledCalendarService();
        service.setParticipantDao(participantDao);
        service.setParticipantService(participantService);
        service.setStudyDao(studyDao);
        service.setSiteDao(siteDao);
        service.setArmDao(armDao);
        service.setScheduledCalendarDao(scheduledCalendarDao);
        service.setScheduledEventDao(scheduledEventDao);
        service.setStudyParticipantAssignmentDao(assignmentDao);

        parameterStudy = setGridId(STUDY_BIG_ID, new Study());
        parameterSite = setGridId(SITE_BIG_ID, new Site());
        parameterParticipant = setGridId(PARTICIPANT_BIG_ID, new Participant());
        parameterArm = setGridId(ARM_BIG_ID, new Arm());
        parameterEvent = setGridId(SCHEDULED_EVENT_BIG_ID, new ScheduledEvent());
        parameterAssignment = setGridId(ASSIGNMENT_BIG_ID, new StudyParticipantAssignment());

        loadedStudy = setGridId(STUDY_BIG_ID, TemplateSkeletonCreator.BASIC.create());
        loadedSite = setGridId(SITE_BIG_ID, createNamedInstance("NU", Site.class));
        loadedStudy.addSite(loadedSite);
        loadedArm = setGridId(ARM_BIG_ID, loadedStudy.getPlannedCalendar().getEpochs().get(1).getArms().get(0));
        loadedEvent = setGridId(SCHEDULED_EVENT_BIG_ID,
            Fixtures.createScheduledEvent("Zeppo", 2003, 12, 1, new Scheduled("Now", DateUtils.createDate(2003, 12, 4))));

        loadedParticipant = setGridId(PARTICIPANT_BIG_ID, createParticipant("Edward", "Armor-o"));

        expect(studyDao.getByGridId(parameterStudy)).andReturn(loadedStudy).times(0, 1);
        expect(siteDao.getByGridId(parameterSite)).andReturn(loadedSite).times(0, 1);
        expect(participantDao.getByGridId(parameterParticipant)).andReturn(loadedParticipant).times(0, 1);
        expect(armDao.getByGridId(parameterArm)).andReturn(loadedArm).times(0, 1);
    }

    private void setAssigned() {
        StudyParticipantAssignment assignment = setGridId(ASSIGNMENT_BIG_ID, new StudyParticipantAssignment());
        StudySite studySite = loadedStudy.getStudySites().get(0);
        assignment.setParticipant(loadedParticipant);
        assignment.setStudySite(studySite);
        assignment.setStartDateEpoch(new Date());
        studySite.getStudyParticipantAssignments().add(assignment);
        loadedParticipant.addAssignment(assignment);
        assignment.setScheduledCalendar(new ScheduledCalendar());

        expect(assignmentDao.getByGridId(parameterAssignment)).andReturn(assignment).times(0, 1);
    }

    ////// TESTS FOR getScheduledCalendar

    public void testBasicGet() throws Exception {
        setAssigned();
        StudyParticipantAssignment expectedAssignment = loadedParticipant.getAssignments().get(0);

        expect(participantDao.getAssignment(loadedParticipant, loadedStudy, loadedSite)).andReturn(expectedAssignment);
        scheduledCalendarDao.initialize(expectedAssignment.getScheduledCalendar());

        replayMocks();
        assertSame(expectedAssignment.getScheduledCalendar(),
            service.getScheduledCalendar(parameterStudy, parameterParticipant, parameterSite));
        verifyMocks();
    }

    public void testBasicGetWithNoSchedule() throws Exception {
        expect(participantDao.getAssignment(loadedParticipant, loadedStudy, loadedSite)).andReturn(null);

        replayMocks();
        assertNull(service.getScheduledCalendar(parameterStudy, parameterParticipant, parameterSite));
        verifyMocks();
    }

    public void testGetInvalidStudy() throws Exception {
        reset(studyDao);
        expect(studyDao.getByGridId(parameterStudy)).andReturn(null);

        replayMocks();
        try {
            service.getScheduledCalendar(parameterStudy, parameterParticipant, parameterSite);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("No study with gridId " + STUDY_BIG_ID, iae.getMessage());
        }
        verifyMocks();
    }

    public void testGetInvalidSite() throws Exception {
        reset(siteDao);
        expect(siteDao.getByGridId(parameterSite)).andReturn(null);

        replayMocks();
        try {
            service.getScheduledCalendar(parameterStudy, parameterParticipant, parameterSite);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("No site with gridId " + SITE_BIG_ID, iae.getMessage());
        }
        verifyMocks();
    }

    public void testGetInvalidParticipant() throws Exception {
        reset(participantDao);
        expect(participantDao.getByGridId(parameterParticipant)).andReturn(null);

        replayMocks();
        try {
            service.getScheduledCalendar(parameterStudy, parameterParticipant, parameterSite);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("No participant with gridId " + PARTICIPANT_BIG_ID, iae.getMessage());
        }
        verifyMocks();
    }

    public void testGetWithNoStudyBigId() throws Exception {
        parameterStudy.setGridId(null);

        replayMocks();
        try {
            service.getScheduledCalendar(parameterStudy, parameterParticipant, parameterSite);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("No gridId on study parameter", iae.getMessage());
        }
        verifyMocks();
    }

    public void testGetWithNoParticpantBigId() throws Exception {
        parameterParticipant.setGridId(null);

        replayMocks();
        try {
            service.getScheduledCalendar(parameterStudy, parameterParticipant, parameterSite);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("No gridId on participant parameter", iae.getMessage());
        }
        verifyMocks();
    }

    public void testGetWithNoSiteBigId() throws Exception {
        parameterSite.setGridId(null);

        replayMocks();
        try {
            service.getScheduledCalendar(parameterStudy, parameterParticipant, parameterSite);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("No gridId on site parameter", iae.getMessage());
        }
        verifyMocks();
    }

    ////// TESTS FOR assignParticipant

    public void testAssignKnownParticipant() throws Exception {
        StudyParticipantAssignment newAssignment = new StudyParticipantAssignment();
        newAssignment.setScheduledCalendar(new ScheduledCalendar());

        expect(participantDao.getAssignment(loadedParticipant, loadedStudy, loadedSite)).andReturn(null);
        expect(participantService.assignParticipant(
            loadedParticipant, loadedStudy.getStudySites().get(0), loadedArm, START_DATE, ASSIGNMENT_BIG_ID)).andReturn(newAssignment);

        replayMocks();
        assertSame(newAssignment.getScheduledCalendar(),
            service.assignParticipant(parameterStudy, parameterParticipant, parameterSite, parameterArm, START_DATE, ASSIGNMENT_BIG_ID));
        verifyMocks();
    }

    public void testAssignUnknownParticipant() throws Exception {
        StudyParticipantAssignment newAssignment = new StudyParticipantAssignment();
        newAssignment.setScheduledCalendar(new ScheduledCalendar());

        reset(participantDao);
        expect(participantDao.getByGridId(parameterParticipant)).andReturn(null);

        participantDao.save(parameterParticipant);
        expect(participantService.assignParticipant(
            parameterParticipant, loadedStudy.getStudySites().get(0), loadedArm, START_DATE, ASSIGNMENT_BIG_ID)
            ).andReturn(newAssignment);

        replayMocks();
        assertSame(newAssignment.getScheduledCalendar(),
            service.assignParticipant(parameterStudy, parameterParticipant, parameterSite, parameterArm, START_DATE, ASSIGNMENT_BIG_ID));
        verifyMocks();
    }

    public void testAssignAgain() throws Exception {
        setAssigned();

        StudyParticipantAssignment expectedAssignment = loadedParticipant.getAssignments().get(0);
        expect(participantDao.getAssignment(loadedParticipant, loadedStudy, loadedSite))
            .andReturn(expectedAssignment);

        replayMocks();
        try {
            service.assignParticipant(parameterStudy, parameterParticipant, parameterSite, parameterArm, START_DATE, ASSIGNMENT_BIG_ID);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("Participant already assigned to this study.  Use scheduleNextArm to change to the next arm.", iae.getMessage());
        }
        verifyMocks();
    }

    public void testAssignFromSiteNotAssociatedWithStudy() throws Exception {
        loadedStudy.getStudySites().clear();
        loadedSite.getStudySites().clear();

        expect(participantDao.getAssignment(loadedParticipant, loadedStudy, loadedSite)).andReturn(null);

        replayMocks();
        try {
            service.assignParticipant(parameterStudy, parameterParticipant, parameterSite, parameterArm, START_DATE, ASSIGNMENT_BIG_ID);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("Site " + loadedSite.getGridId() + " not associated with study " + loadedStudy.getGridId(),
                iae.getMessage());
        }
        verifyMocks();
    }

    public void testAssignFromArmNotInStudy() throws Exception {
        Arm badParameterArm = setGridId("BAD-NEWS", new Arm());
        Arm badLoadedArm = setGridId("BAD-NEWS", new Arm());

        reset(armDao);
        expect(armDao.getByGridId(badParameterArm)).andReturn(badLoadedArm);

        expect(participantDao.getAssignment(loadedParticipant, loadedStudy, loadedSite)).andReturn(null);

        replayMocks();
        try {
            service.assignParticipant(parameterStudy, parameterParticipant, parameterSite, badParameterArm, START_DATE, ASSIGNMENT_BIG_ID);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("Arm BAD-NEWS not part of template for study " + STUDY_BIG_ID, iae.getMessage());
        }
        verifyMocks();
    }

    public void testAssignNullArm() throws Exception {
        StudyParticipantAssignment newAssignment = new StudyParticipantAssignment();
        newAssignment.setScheduledCalendar(new ScheduledCalendar());

        Arm defaultArm = loadedStudy.getPlannedCalendar().getEpochs().get(0).getArms().get(0);

        reset(armDao);

        expect(participantDao.getAssignment(loadedParticipant, loadedStudy, loadedSite)).andReturn(null);
        expect(participantService.assignParticipant(
            loadedParticipant, loadedStudy.getStudySites().get(0), defaultArm, START_DATE, ASSIGNMENT_BIG_ID)).andReturn(newAssignment);

        replayMocks();
        service.assignParticipant(parameterStudy, parameterParticipant, parameterSite, null, START_DATE, ASSIGNMENT_BIG_ID);
        verifyMocks();
    }

    ////// TESTS FOR getScheduledEvents

    public void testGetEventsByDateRange() throws Exception {
        setAssigned();
        Date stop = new Date();
        Collection<ScheduledEvent> expectedMatches = new LinkedList<ScheduledEvent>();
        StudyParticipantAssignment expectedAssignment
            = loadedStudy.getStudySites().get(0).getStudyParticipantAssignments().get(0);

        expect(participantDao.getAssignment(loadedParticipant, loadedStudy, loadedSite))
            .andReturn(expectedAssignment);
        expect(scheduledEventDao.getEventsByDate(expectedAssignment.getScheduledCalendar(), START_DATE, stop))
            .andReturn(expectedMatches);

        replayMocks();
        assertSame(expectedMatches,
            service.getScheduledEvents(parameterStudy, parameterParticipant, parameterSite, START_DATE, stop));
        verifyMocks();
    }

    ////// TESTS FOR changeEventState

    public void testChange() throws Exception {
        Canceled newState = new Canceled();
        expect(scheduledEventDao.getByGridId(parameterEvent)).andReturn(loadedEvent);
        scheduledEventDao.save(loadedEvent);

        replayMocks();
        service.changeEventState(parameterEvent, newState);
        verifyMocks();

        assertEquals("New state not applied", 3, loadedEvent.getAllStates().size());
        assertEquals("New state not applied", ScheduledEventMode.CANCELED,
            loadedEvent.getCurrentState().getMode());
    }

    ////// TESTS FOR scheduleNextArm

    public void testScheduleNext() throws Exception {
        NextArmMode expectedMode = NextArmMode.IMMEDIATE;
        setAssigned();
        StudyParticipantAssignment expectedAssignment
            = loadedStudy.getStudySites().get(0).getStudyParticipantAssignments().get(0);
        ScheduledArm expectedScheduledArm = new ScheduledArm();

        expect(participantDao.getAssignment(loadedParticipant, loadedStudy, loadedSite))
            .andReturn(expectedAssignment);
        expect(participantService.scheduleArm(expectedAssignment, loadedArm, START_DATE, expectedMode))
            .andReturn(expectedScheduledArm);

        replayMocks();
        service.scheduleNextArm(parameterStudy, parameterParticipant, parameterSite, parameterArm, expectedMode, START_DATE);
        verifyMocks();
    }

    ////// TESTS FOR registerSevereAdverseEvent

    public void testBasic() throws Exception {
        setAssigned();
        StudyParticipantAssignment expectedAssignment
            = loadedStudy.getStudySites().get(0).getStudyParticipantAssignments().get(0);
        AdverseEvent expectedAe = new AdverseEvent();

        expect(participantDao.getAssignment(loadedParticipant, loadedStudy, loadedSite))
            .andReturn(expectedAssignment);
        participantDao.save(loadedParticipant);

        replayMocks();
        service.registerSevereAdverseEvent(
            parameterStudy, parameterParticipant, parameterSite, expectedAe);
        verifyMocks();

        assertEquals("Notification not added", 1, expectedAssignment.getAeNotifications().size());
        assertSame("Notification is for wrong AE", expectedAe, expectedAssignment.getAeNotifications().get(0).getAdverseEvent());
    }

    public void testRegisterAeByAssignment() throws Exception {
        setAssigned();
        StudyParticipantAssignment expectedAssignment
            = loadedStudy.getStudySites().get(0).getStudyParticipantAssignments().get(0);
        AdverseEvent expectedAe = new AdverseEvent();

        participantDao.save(loadedParticipant);

        replayMocks();
        service.registerSevereAdverseEvent(parameterAssignment, expectedAe);
        verifyMocks();

        assertEquals("Notification not added", 1, expectedAssignment.getAeNotifications().size());
        assertSame("Notification is for wrong AE", expectedAe, expectedAssignment.getAeNotifications().get(0).getAdverseEvent());
    }
}
