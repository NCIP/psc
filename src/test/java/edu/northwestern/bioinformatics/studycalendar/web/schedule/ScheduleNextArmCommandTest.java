package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.nwu.bioinformatics.commons.DateUtils;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.NextArmMode;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.classextension.EasyMock.expect;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class ScheduleNextArmCommandTest extends StudyCalendarTestCase {
    private ScheduleNextArmCommand command;
    private SubjectService subjectService;

    protected void setUp() throws Exception {
        super.setUp();
        subjectService = registerMockFor(SubjectService.class);
        command = new ScheduleNextArmCommand(subjectService);
    }

    public void testSchedule() throws Exception {
        ScheduledArm expectedScheduledArm = new ScheduledArm();
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        ScheduledCalendar cal = new ScheduledCalendar();
        assignment.setScheduledCalendar(cal);
        Arm arm = new Arm();
        Date start = DateUtils.createDate(2005, Calendar.APRIL, 9);
        command.setCalendar(cal);
        command.setArm(arm);
        command.setStartDate(start);
        command.setMode(NextArmMode.IMMEDIATE);

        expect(subjectService.scheduleArm(assignment, arm, start, NextArmMode.IMMEDIATE))
            .andReturn(expectedScheduledArm);
        replayMocks();

        assertSame(expectedScheduledArm, command.schedule());
        verifyMocks();
    }
}
