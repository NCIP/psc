package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.nwu.bioinformatics.commons.DateUtils;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.NextStudySegmentMode;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.classextension.EasyMock.expect;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class ScheduleNextStudySegmentCommandTest extends StudyCalendarTestCase {
    private ScheduleNextStudySegmentCommand command;
    private SubjectService subjectService;

    protected void setUp() throws Exception {
        super.setUp();
        subjectService = registerMockFor(SubjectService.class);
        command = new ScheduleNextStudySegmentCommand(subjectService);
    }

    public void testSchedule() throws Exception {
        ScheduledStudySegment expectedScheduledStudySegment = new ScheduledStudySegment();
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        ScheduledCalendar cal = new ScheduledCalendar();
        assignment.setScheduledCalendar(cal);
        StudySegment studySegment = new StudySegment();
        Date start = DateUtils.createDate(2005, Calendar.APRIL, 9);
        command.setCalendar(cal);
        command.setStudySegment(studySegment);
        command.setStartDate(start);
        command.setMode(NextStudySegmentMode.IMMEDIATE);

        expect(subjectService.scheduleStudySegment(assignment, studySegment, start, NextStudySegmentMode.IMMEDIATE))
            .andReturn(expectedScheduledStudySegment);
        replayMocks();

        assertSame(expectedScheduledStudySegment, command.schedule());
        verifyMocks();
    }
}
