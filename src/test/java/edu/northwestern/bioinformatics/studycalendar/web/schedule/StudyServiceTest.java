package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Occurred;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.nwu.bioinformatics.commons.DateUtils;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import gov.nih.nci.cabig.ctms.lang.StaticNowFactory;
import static org.easymock.EasyMock.expect;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class StudyServiceTest extends StudyCalendarTestCase {
    private StudyService service;
    private StudyDao studyDao;
    private ActivityDao activityDao;
    private Study study;
    StudySubjectAssignment subjectAssignment;
    ScheduledCalendar calendar;
    StaticNowFactory staticNowFactory;

    protected void setUp() throws Exception {
        super.setUp();
        studyDao = registerMockFor(StudyDao.class);
        activityDao = registerMockFor(ActivityDao.class);
        service = new StudyService();
        service.setStudyDao(studyDao);
        service.setActivityDao(activityDao);
        study = setId(1 , new Study());

        calendar = new ScheduledCalendar();

        subjectAssignment = new StudySubjectAssignment();
        subjectAssignment.setSubject(createSubject("John", "Doe"));
        subjectAssignment.setScheduledCalendar(calendar);

        staticNowFactory = new StaticNowFactory();
    }

    public void testScheduleReconsentAfterScheduledActivityOnOccurredEvent() throws Exception{
        staticNowFactory.setNowTimestamp(DateTools.createTimestamp(2005, Calendar.JULY, 2));

        ScheduledArm arm0 = new ScheduledArm();
        arm0.addEvent(Fixtures.createScheduledActivity("AAA", 2005, Calendar.JULY, 1));
        arm0.addEvent(Fixtures.createScheduledActivity("BBB", 2005, Calendar.JULY, 2,
                new Occurred(null, DateUtils.createDate(2005, Calendar.JULY, 3))));
        arm0.addEvent(Fixtures.createScheduledActivity("CCC", 2005, Calendar.JULY, 4));
        arm0.addEvent(Fixtures.createScheduledActivity("DDD", 2005, Calendar.JULY, 8));
        calendar.addArm(arm0);

        ScheduledArm arm1 = new ScheduledArm();
        arm1.addEvent(Fixtures.createScheduledActivity("EEE", 2005, Calendar.AUGUST, 1,
                new Occurred(null, DateUtils.createDate(2005, Calendar.AUGUST, 2))));
        arm1.addEvent(Fixtures.createScheduledActivity("FFF", 2005, Calendar.AUGUST, 3));
        arm1.addEvent(Fixtures.createScheduledActivity("GGG", 2005, Calendar.AUGUST, 8));
        calendar.addArm(arm1);

        List<StudySubjectAssignment> assignments = Collections.singletonList(subjectAssignment);
        expect(studyDao.getAssignmentsForStudy(study.getId())).andReturn(assignments);

        Activity reconsent = setId(1, createNamedInstance("Reconsent", Activity.class));
        expect(activityDao.getByName("Reconsent")).andReturn(reconsent);

        studyDao.save(study);
        replayMocks();
        service.scheduleReconsent(study, staticNowFactory.getNow(), "Reconsent Details");
        verifyMocks();

        List<ScheduledActivity> list = arm0.getEventsByDate().get(DateTools.createTimestamp(2005, Calendar.JULY, 4));
        
        assertEquals("Wrong number of events on July 4th", 2, list.size());
        assertEquals("Reconsent Details should be destails", "Reconsent Details", list.get(1).getDetails());
        assertEquals("Reconsent should be activity name", "Reconsent", list.get(1).getActivity().getName());
    }

    public void testScheduleReconsentForSecondArmOnSameDayAsScheduledActivity() throws Exception{
        staticNowFactory.setNowTimestamp(DateTools.createTimestamp(2005, Calendar.AUGUST, 3));

        ScheduledArm arm0 = new ScheduledArm();
        arm0.addEvent(Fixtures.createScheduledActivity("AAA", 2005, Calendar.JULY, 1,
                new Occurred(null, DateUtils.createDate(2005, Calendar.JULY, 2))));
        arm0.addEvent(Fixtures.createScheduledActivity("BBB", 2005, Calendar.JULY, 3));
        arm0.addEvent(Fixtures.createScheduledActivity("CCC", 2005, Calendar.JULY, 8));
        calendar.addArm(arm0);

        ScheduledArm arm1 = new ScheduledArm();
        arm1.addEvent(Fixtures.createScheduledActivity("DDD", 2005, Calendar.AUGUST, 1,
                new Occurred(null, DateUtils.createDate(2005, Calendar.AUGUST, 2))));
        arm1.addEvent(Fixtures.createScheduledActivity("EEE", 2005, Calendar.AUGUST, 3));
        arm1.addEvent(Fixtures.createScheduledActivity("FFF", 2005, Calendar.AUGUST, 8));
        calendar.addArm(arm1);

        List<StudySubjectAssignment> assignments = Collections.singletonList(subjectAssignment);
        expect(studyDao.getAssignmentsForStudy(study.getId())).andReturn(assignments);

        Activity reconsent = setId(1, createNamedInstance("Reconsent", Activity.class));
        expect(activityDao.getByName("Reconsent")).andReturn(reconsent);

        studyDao.save(study);
        replayMocks();
        service.scheduleReconsent(study, staticNowFactory.getNow(), "Reconsent Details");
        verifyMocks();

        List<ScheduledActivity> list = arm1.getEventsByDate().get(DateTools.createTimestamp(2005, Calendar.AUGUST, 3));

        assertEquals("Wrong number of events on August 8th", 2, list.size());
        assertEquals("Reconsent Details should be destails", "Reconsent Details", list.get(1).getDetails());
        assertEquals("Reconsent should be activity name", "Reconsent", list.get(1).getActivity().getName());
    }
}
