package edu.northwestern.bioinformatics.studycalendar.api.impl;

import edu.nwu.bioinformatics.commons.DateUtils;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Canceled;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;

import java.util.*;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class DefaultScheduledCalendarServiceTest extends StudyCalendarTestCase {
    private static final String STUDY_BIG_ID = "STUDY-GUID";
    private static final String SITE_BIG_ID = "SITE-GUID";
    private static final String PARTICIPANT_BIG_ID = "PARTICPANT-GUID";
    private static final String ARM_BIG_ID = "ARM-GUID";
    private static final String SCHEDULED_ACTIVITY_BIG_ID = "EVENT-GUID";
    private static final String ASSIGNMENT_BIG_ID = "ASSIGNMENT-GUID";
    private static final Date START_DATE = new Date();

    private DefaultScheduledCalendarService service;
    private SiteDao siteDao;
    private StudyDao studyDao;
    private SubjectDao subjectDao;
    private SubjectService subjectService;
    private ArmDao armDao;
    private ScheduledCalendarDao scheduledCalendarDao;
    private ScheduledActivityDao scheduledActivityDao;
    private StudySubjectAssignmentDao assignmentDao;
    private UserDao userDao;

    private Study parameterStudy;
    private Site parameterSite;
    private Subject parameterSubject;
    private Arm parameterArm;
    private ScheduledActivity parameterEvent;
    private StudySubjectAssignment parameterAssignment;

    private Study loadedStudy;
    private Site loadedSite;
    private Subject loadedSubject;
    private Arm loadedArm;
    private ScheduledActivity loadedEvent;
    private User user;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        armDao = registerDaoMockFor(ArmDao.class);
        siteDao = registerDaoMockFor(SiteDao.class);
        studyDao = registerDaoMockFor(StudyDao.class);
        subjectDao = registerDaoMockFor(SubjectDao.class);
        subjectService = registerMockFor(SubjectService.class);
        scheduledCalendarDao = registerDaoMockFor(ScheduledCalendarDao.class);
        scheduledActivityDao = registerDaoMockFor(ScheduledActivityDao.class);
        assignmentDao = registerDaoMockFor(StudySubjectAssignmentDao.class);
        userDao = registerDaoMockFor(UserDao.class);

        service = new DefaultScheduledCalendarService();
        service.setSubjectDao(subjectDao);
        service.setSubjectService(subjectService);
        service.setStudyDao(studyDao);
        service.setSiteDao(siteDao);
        service.setArmDao(armDao);
        service.setScheduledCalendarDao(scheduledCalendarDao);
        service.setScheduledActivityDao(scheduledActivityDao);
        service.setStudySubjectAssignmentDao (assignmentDao);
        service.setUserDao(userDao);

        parameterStudy = setGridId(STUDY_BIG_ID, new Study());
        parameterSite = setGridId(SITE_BIG_ID, new Site());
        parameterSubject = setGridId(PARTICIPANT_BIG_ID, new Subject());
        parameterArm = setGridId(ARM_BIG_ID, new Arm());
        parameterEvent = setGridId(SCHEDULED_ACTIVITY_BIG_ID, new ScheduledActivity());
        parameterAssignment = setGridId(ASSIGNMENT_BIG_ID, new StudySubjectAssignment());

        loadedStudy = setGridId(STUDY_BIG_ID, Fixtures.createBasicTemplate());
        loadedSite = setGridId(SITE_BIG_ID, createNamedInstance("NU", Site.class));
        loadedStudy.addSite(loadedSite);
        loadedArm = setGridId(ARM_BIG_ID, loadedStudy.getPlannedCalendar().getEpochs().get(1).getArms().get(0));
        loadedEvent = setGridId(SCHEDULED_ACTIVITY_BIG_ID,
            Fixtures.createScheduledActivity("Zeppo", 2003, 12, 1, new Scheduled("Now", DateUtils.createDate(2003, 12, 4))));

        loadedSubject = setGridId(PARTICIPANT_BIG_ID, createSubject("Edward", "Armor-o"));

        user = new User();
        Set<UserRole> userRoles = new HashSet<UserRole>();
        UserRole userRole = new UserRole();
        userRole.setRole(Role.SUBJECT_COORDINATOR);
        userRoles.add(userRole);
        
        user.setUserRoles(userRoles);

        expect(studyDao.getByGridId(parameterStudy)).andReturn(loadedStudy).times(0, 1);
        expect(siteDao.getByGridId(parameterSite)).andReturn(loadedSite).times(0, 1);
        expect(subjectDao.getByGridId(parameterSubject)).andReturn(loadedSubject).times(0, 1);
        expect(armDao.getByGridId(parameterArm)).andReturn(loadedArm).times(0, 1);
    }

    private void setAssigned() {
        StudySubjectAssignment assignment = setGridId(ASSIGNMENT_BIG_ID, new StudySubjectAssignment());
        StudySite studySite = loadedStudy.getStudySites().get(0);
        assignment.setSubject(loadedSubject);
        assignment.setStudySite(studySite);
        assignment.setStartDateEpoch(new Date());
        studySite.getStudySubjectAssignments().add(assignment);
        loadedSubject.addAssignment(assignment);
        assignment.setScheduledCalendar(new ScheduledCalendar());

        expect(assignmentDao.getByGridId(parameterAssignment)).andReturn(assignment).times(0, 1);
    }

    ////// TESTS FOR getScheduledCalendar

    public void testBasicGet() throws Exception {
        setAssigned();
        StudySubjectAssignment expectedAssignment = loadedSubject.getAssignments().get(0);

        expect(subjectDao.getAssignment(loadedSubject, loadedStudy, loadedSite)).andReturn(expectedAssignment);
        scheduledCalendarDao.initialize(expectedAssignment.getScheduledCalendar());

        replayMocks();
        assertSame(expectedAssignment.getScheduledCalendar(),
            service.getScheduledCalendar(parameterStudy, parameterSubject, parameterSite));
        verifyMocks();
    }

    public void testBasicGetWithNoSchedule() throws Exception {
        expect(subjectDao.getAssignment(loadedSubject, loadedStudy, loadedSite)).andReturn(null);

        replayMocks();
        assertNull(service.getScheduledCalendar(parameterStudy, parameterSubject, parameterSite));
        verifyMocks();
    }

    public void testGetInvalidStudy() throws Exception {
        reset(studyDao);
        expect(studyDao.getByGridId(parameterStudy)).andReturn(null);

        replayMocks();
        try {
            service.getScheduledCalendar(parameterStudy, parameterSubject, parameterSite);
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
            service.getScheduledCalendar(parameterStudy, parameterSubject, parameterSite);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("No site with gridId " + SITE_BIG_ID, iae.getMessage());
        }
        verifyMocks();
    }

    public void testGetInvalidSubject() throws Exception {
        reset(subjectDao);
        expect(subjectDao.getByGridId(parameterSubject)).andReturn(null);

        replayMocks();
        try {
            service.getScheduledCalendar(parameterStudy, parameterSubject, parameterSite);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("No subject with gridId " + PARTICIPANT_BIG_ID, iae.getMessage());
        }
        verifyMocks();
    }

    public void testGetWithNoStudyGridId() throws Exception {
        parameterStudy.setGridId(null);

        replayMocks();
        try {
            service.getScheduledCalendar(parameterStudy, parameterSubject, parameterSite);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("No gridId on study parameter", iae.getMessage());
        }
        verifyMocks();
    }

    public void testGetWithNoParticpantGridId() throws Exception {
        parameterSubject.setGridId(null);

        replayMocks();
        try {
            service.getScheduledCalendar(parameterStudy, parameterSubject, parameterSite);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("No gridId on subject parameter", iae.getMessage());
        }
        verifyMocks();
    }

    public void testGetWithNoSiteGridId() throws Exception {
        parameterSite.setGridId(null);

        replayMocks();
        try {
            service.getScheduledCalendar(parameterStudy, parameterSubject, parameterSite);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("No gridId on site parameter", iae.getMessage());
        }
        verifyMocks();
    }

    ////// TESTS FOR assignSubject

    public void testAssignKnownSubject() throws Exception {
        StudySubjectAssignment newAssignment = new StudySubjectAssignment();
        newAssignment.setScheduledCalendar(new ScheduledCalendar());

        expect(subjectDao.getAssignment(loadedSubject, loadedStudy, loadedSite)).andReturn(null);
        expect(subjectService.assignSubject(
            loadedSubject, loadedStudy.getStudySites().get(0), loadedArm, START_DATE, ASSIGNMENT_BIG_ID, user)).andReturn(newAssignment);
        expect(userDao.getByName(user.getName())).andReturn(user).anyTimes();
        replayMocks();
        assertSame(newAssignment.getScheduledCalendar(),
            service.assignSubject(parameterStudy, parameterSubject, parameterSite, parameterArm, START_DATE, ASSIGNMENT_BIG_ID));
        verifyMocks();
    }

    public void testAssignUnknownSubject() throws Exception {
        StudySubjectAssignment newAssignment = new StudySubjectAssignment();
        newAssignment.setScheduledCalendar(new ScheduledCalendar());

        reset(subjectDao);
        expect(subjectDao.getByGridId(parameterSubject)).andReturn(null);

        subjectDao.save(parameterSubject);
        expect(subjectService.assignSubject(
            parameterSubject, loadedStudy.getStudySites().get(0), loadedArm, START_DATE, ASSIGNMENT_BIG_ID, user)
            ).andReturn(newAssignment);
        expect(userDao.getByName(null)).andReturn(user).anyTimes();
        replayMocks();
        assertSame(newAssignment.getScheduledCalendar(),
            service.assignSubject(parameterStudy, parameterSubject, parameterSite, parameterArm, START_DATE, ASSIGNMENT_BIG_ID));
        verifyMocks();
    }

    public void testAssignAgain() throws Exception {
        setAssigned();

        StudySubjectAssignment expectedAssignment = loadedSubject.getAssignments().get(0);
        expect(subjectDao.getAssignment(loadedSubject, loadedStudy, loadedSite))
            .andReturn(expectedAssignment);

        replayMocks();
        try {
            service.assignSubject(parameterStudy, parameterSubject, parameterSite, parameterArm, START_DATE, ASSIGNMENT_BIG_ID);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("Subject already assigned to this study.  Use scheduleNextArm to change to the next arm.", iae.getMessage());
        }
        verifyMocks();
    }

    public void testAssignFromSiteNotAssociatedWithStudy() throws Exception {
        loadedStudy.getStudySites().clear();
        loadedSite.getStudySites().clear();

        expect(subjectDao.getAssignment(loadedSubject, loadedStudy, loadedSite)).andReturn(null);

        replayMocks();
        try {
            service.assignSubject(parameterStudy, parameterSubject, parameterSite, parameterArm, START_DATE, ASSIGNMENT_BIG_ID);
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

        expect(subjectDao.getAssignment(loadedSubject, loadedStudy, loadedSite)).andReturn(null);

        replayMocks();
        try {
            service.assignSubject(parameterStudy, parameterSubject, parameterSite, badParameterArm, START_DATE, ASSIGNMENT_BIG_ID);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("Arm BAD-NEWS not part of template for study " + STUDY_BIG_ID, iae.getMessage());
        }
        verifyMocks();
    }

    public void testAssignNullArm() throws Exception {
        StudySubjectAssignment newAssignment = new StudySubjectAssignment();
        newAssignment.setScheduledCalendar(new ScheduledCalendar());

        Arm defaultArm = loadedStudy.getPlannedCalendar().getEpochs().get(0).getArms().get(0);

        reset(armDao);

        expect(subjectDao.getAssignment(loadedSubject, loadedStudy, loadedSite)).andReturn(null);
        expect(subjectService.assignSubject(
            loadedSubject, loadedStudy.getStudySites().get(0), defaultArm, START_DATE, ASSIGNMENT_BIG_ID, user)).andReturn(newAssignment);
        expect(userDao.getByName(null)).andReturn(user).anyTimes();
        replayMocks();
        service.assignSubject(parameterStudy, parameterSubject, parameterSite, null, START_DATE, ASSIGNMENT_BIG_ID);
        verifyMocks();
    }

    ////// TESTS FOR getScheduledActivities

    public void testGetEventsByDateRange() throws Exception {
        setAssigned();
        Date stop = new Date();
        Collection<ScheduledActivity> expectedMatches = new LinkedList<ScheduledActivity>();
        StudySubjectAssignment expectedAssignment
            = loadedStudy.getStudySites().get(0).getStudySubjectAssignments().get(0);

        expect(subjectDao.getAssignment(loadedSubject, loadedStudy, loadedSite))
            .andReturn(expectedAssignment);
        expect(scheduledActivityDao.getEventsByDate(expectedAssignment.getScheduledCalendar(), START_DATE, stop))
            .andReturn(expectedMatches);

        replayMocks();
        assertSame(expectedMatches,
            service.getScheduledActivities(parameterStudy, parameterSubject, parameterSite, START_DATE, stop));
        verifyMocks();
    }

    ////// TESTS FOR changeEventState

    public void testChange() throws Exception {
        Canceled newState = new Canceled();
        expect(scheduledActivityDao.getByGridId(parameterEvent)).andReturn(loadedEvent);
        scheduledActivityDao.save(loadedEvent);

        replayMocks();
        service.changeEventState(parameterEvent, newState);
        verifyMocks();

        assertEquals("New state not applied", 3, loadedEvent.getAllStates().size());
        assertEquals("New state not applied", ScheduledActivityMode.CANCELED,
            loadedEvent.getCurrentState().getMode());
    }

    ////// TESTS FOR scheduleNextArm

    public void testScheduleNext() throws Exception {
        NextArmMode expectedMode = NextArmMode.IMMEDIATE;
        setAssigned();
        StudySubjectAssignment expectedAssignment
            = loadedStudy.getStudySites().get(0).getStudySubjectAssignments().get(0);
        ScheduledArm expectedScheduledArm = new ScheduledArm();

        expect(subjectDao.getAssignment(loadedSubject, loadedStudy, loadedSite))
            .andReturn(expectedAssignment);
        expect(subjectService.scheduleArm(expectedAssignment, loadedArm, START_DATE, expectedMode))
            .andReturn(expectedScheduledArm);

        replayMocks();
        service.scheduleNextArm(parameterStudy, parameterSubject, parameterSite, parameterArm, expectedMode, START_DATE);
        verifyMocks();
    }

    ////// TESTS FOR registerSevereAdverseEvent

    public void testBasic() throws Exception {
        setAssigned();
        StudySubjectAssignment expectedAssignment
            = loadedStudy.getStudySites().get(0).getStudySubjectAssignments().get(0);
        AdverseEvent expectedAe = new AdverseEvent();

        expect(subjectDao.getAssignment(loadedSubject, loadedStudy, loadedSite))
            .andReturn(expectedAssignment);
        subjectDao.save(loadedSubject);

        replayMocks();
        service.registerSevereAdverseEvent(
            parameterStudy, parameterSubject, parameterSite, expectedAe);
        verifyMocks();

        assertEquals("Notification not added", 1, expectedAssignment.getAeNotifications().size());
        assertSame("Notification is for wrong AE", expectedAe, expectedAssignment.getAeNotifications().get(0).getAdverseEvent());
    }

    public void testRegisterAeByAssignment() throws Exception {
        setAssigned();
        StudySubjectAssignment expectedAssignment
            = loadedStudy.getStudySites().get(0).getStudySubjectAssignments().get(0);
        AdverseEvent expectedAe = new AdverseEvent();

        subjectDao.save(loadedSubject);

        replayMocks();
        service.registerSevereAdverseEvent(parameterAssignment, expectedAe);
        verifyMocks();

        assertEquals("Notification not added", 1, expectedAssignment.getAeNotifications().size());
        assertSame("Notification is for wrong AE", expectedAe, expectedAssignment.getAeNotifications().get(0).getAdverseEvent());
    }
}
