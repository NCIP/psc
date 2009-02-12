package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Occurred;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.NotificationService;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.*;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.nwu.bioinformatics.commons.DateUtils;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import gov.nih.nci.cabig.ctms.lang.StaticNowFactory;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import org.easymock.classextension.EasyMock;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class StudyServiceTest extends StudyCalendarTestCase {
    private static final Timestamp NOW = DateTools.createTimestamp(2001, Calendar.FEBRUARY, 4);

    private StudyService service;
    private StudyDao studyDao;
    private ActivityDao activityDao;
    private Study study;
    StudySubjectAssignment subjectAssignment;
    ScheduledCalendar calendar;
    StaticNowFactory staticNowFactory;
    private DeltaService deltaService;
    private ScheduledActivityDao scheduledActivityDao;
    private NotificationService notificationService;

    protected void setUp() throws Exception {
        super.setUp();

        studyDao = registerMockFor(StudyDao.class);
        deltaService = registerMockFor(DeltaService.class);
        scheduledActivityDao=registerDaoMockFor(ScheduledActivityDao.class);
        activityDao = registerMockFor(ActivityDao.class);
        notificationService=registerMockFor(NotificationService.class);
        staticNowFactory = new StaticNowFactory();
        staticNowFactory.setNowTimestamp(NOW);

        service = new StudyService();
        service.setStudyDao(studyDao);
        service.setActivityDao(activityDao);
        service.setDeltaService(deltaService);
        service.setNowFactory(staticNowFactory);
        service.setScheduledActivityDao(scheduledActivityDao);
        service.setNotificationService(notificationService);

        study = setId(1 , new Study());

        calendar = new ScheduledCalendar();

        subjectAssignment = new StudySubjectAssignment();
        subjectAssignment.setSubject(createSubject("John", "Doe"));
        subjectAssignment.setScheduledCalendar(calendar);
    }

    public void testScheduleReconsentAfterScheduledActivityOnOccurredEvent() throws Exception{
        staticNowFactory.setNowTimestamp(DateTools.createTimestamp(2005, Calendar.JULY, 2));

        ScheduledStudySegment studySegment0 = new ScheduledStudySegment();
        studySegment0.addEvent(Fixtures.createScheduledActivity("AAA", 2005, Calendar.JULY, 1));
        studySegment0.addEvent(Fixtures.createScheduledActivity("BBB", 2005, Calendar.JULY, 2,
                new Occurred(null, DateUtils.createDate(2005, Calendar.JULY, 3))));
        studySegment0.addEvent(Fixtures.createScheduledActivity("CCC", 2005, Calendar.JULY, 4));
        studySegment0.addEvent(Fixtures.createScheduledActivity("DDD", 2005, Calendar.JULY, 8));
        calendar.addStudySegment(studySegment0);

        ScheduledStudySegment studySegment1 = new ScheduledStudySegment();
        studySegment1.addEvent(Fixtures.createScheduledActivity("EEE", 2005, Calendar.AUGUST, 1,
                new Occurred(null, DateUtils.createDate(2005, Calendar.AUGUST, 2))));
        studySegment1.addEvent(edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createScheduledActivity("FFF", 2005, Calendar.AUGUST, 3));
        studySegment1.addEvent(Fixtures.createScheduledActivity("GGG", 2005, Calendar.AUGUST, 8));
        calendar.addStudySegment(studySegment1);

        List<StudySubjectAssignment> assignments = Collections.singletonList(subjectAssignment);
        expect(studyDao.getAssignmentsForStudy(study.getId())).andReturn(assignments);

        Activity reconsent = setId(1, createNamedInstance("Reconsent", Activity.class));
        expect(activityDao.getByName("Reconsent")).andReturn(reconsent);

        studyDao.save(study);
        scheduledActivityDao.save(isA(ScheduledActivity.class));
        notificationService.notifyUsersForNewScheduleNotifications(isA(Notification.class));

        replayMocks();
        service.scheduleReconsent(study, staticNowFactory.getNow(), "Reconsent Details");
        verifyMocks();

        List<ScheduledActivity> list = studySegment0.getActivitiesByDate().get(DateTools.createTimestamp(2005, Calendar.JULY, 4));
        
        assertEquals("Wrong number of events on July 4th", 2, list.size());
        assertEquals("Reconsent Details should be destails", "Reconsent Details", list.get(1).getDetails());
        assertEquals("Reconsent should be activity name", "Reconsent", list.get(1).getActivity().getName());
    }

    public void testScheduleReconsentForSecondArmOnSameDayAsScheduledActivity() throws Exception{
        staticNowFactory.setNowTimestamp(DateTools.createTimestamp(2005, Calendar.AUGUST, 3));

        ScheduledStudySegment studySegment0 = new ScheduledStudySegment();
        studySegment0.addEvent(Fixtures.createScheduledActivity("AAA", 2005, Calendar.JULY, 1,
                new Occurred(null, DateUtils.createDate(2005, Calendar.JULY, 2))));
        studySegment0.addEvent(Fixtures.createScheduledActivity("BBB", 2005, Calendar.JULY, 3));
        studySegment0.addEvent(Fixtures.createScheduledActivity("CCC", 2005, Calendar.JULY, 8));
        calendar.addStudySegment(studySegment0);

        ScheduledStudySegment studySegment1 = new ScheduledStudySegment();
        studySegment1.addEvent(Fixtures.createScheduledActivity("DDD", 2005, Calendar.AUGUST, 1,
                new Occurred(null, DateUtils.createDate(2005, Calendar.AUGUST, 2))));
        studySegment1.addEvent(Fixtures.createScheduledActivity("EEE", 2005, Calendar.AUGUST, 3));
        studySegment1.addEvent(Fixtures.createScheduledActivity("FFF", 2005, Calendar.AUGUST, 8));
        calendar.addStudySegment(studySegment1);

        List<StudySubjectAssignment> assignments = Collections.singletonList(subjectAssignment);
        expect(studyDao.getAssignmentsForStudy(study.getId())).andReturn(assignments);

        Activity reconsent = setId(1, createNamedInstance("Reconsent", Activity.class));
        expect(activityDao.getByName("Reconsent")).andReturn(reconsent);
        scheduledActivityDao.save(isA(ScheduledActivity.class));
        studyDao.save(study);
        notificationService.notifyUsersForNewScheduleNotifications(isA(Notification.class));
        
        replayMocks();
        service.scheduleReconsent(study, staticNowFactory.getNow(), "Reconsent Details");
        verifyMocks();

        List<ScheduledActivity> list = studySegment1.getActivitiesByDate().get(DateTools.createTimestamp(2005, Calendar.AUGUST, 3));

        assertEquals("Wrong number of events on August 8th", 2, list.size());
        assertEquals("Reconsent Details should be destails", "Reconsent Details", list.get(1).getDetails());
        assertEquals("Reconsent should be activity name", "Reconsent", list.get(1).getActivity().getName());
    }

    public void testSave() {
        Study study = createNamedInstance("Study A", Study.class);
        Amendment amend0 = Fixtures.createAmendments("Amendment A");
        Amendment amend1 = Fixtures.createAmendments("Amendment B");
        amend1.setPreviousAmendment(amend0);
        study.setAmendment(amend1);

        studyDao.save(study);
        deltaService.saveRevision(amend1);
        deltaService.saveRevision(amend0);
        replayMocks();

        service.save(study);
        verifyMocks();
    }

    public void testCreateInDesignStudyFromExamplePlanTree() throws Exception {
        Study example = Fixtures.createBasicTemplate();
        PlannedCalendar expectedPC = example.getPlannedCalendar();
        // copy the list since the one in the PC is destroyed in the tested method
        List<Epoch> expectedEpochs = new ArrayList<Epoch>(expectedPC.getEpochs());
        example.setAssignedIdentifier("nerf-herder");

        studyDao.save(example);
        deltaService.saveRevision((Revision) EasyMock.notNull());
        replayMocks();
        service.createInDesignStudyFromExamplePlanTree(example);
        verifyMocks();

        assertNotNull("Development amendment not added", example.getDevelopmentAmendment());
        assertEquals("Development amendment has wrong name", Amendment.INITIAL_TEMPLATE_AMENDMENT_NAME,
            example.getDevelopmentAmendment().getName());
        assertEquals("Wrong number of deltas in the new dev amendment",
            1, example.getDevelopmentAmendment().getDeltas().size());
        Delta<?> actualDelta = example.getDevelopmentAmendment().getDeltas().get(0);
        assertEquals("Wrong kind of delta in the new dev amendment",
            expectedPC, actualDelta.getNode());
        assertEquals("Wrong number of changes in created delta", expectedEpochs.size(),
            actualDelta.getChanges().size());
        for (int i = 0; i < expectedEpochs.size(); i++) {
            assertEquals(i + " change is not an add",
                ChangeAction.ADD, actualDelta.getChanges().get(i).getAction());
            assertSame(i + " change is not an add of the correct epoch",
                expectedEpochs.get(i), ((Add) actualDelta.getChanges().get(i)).getChild());
        }
    }
}
