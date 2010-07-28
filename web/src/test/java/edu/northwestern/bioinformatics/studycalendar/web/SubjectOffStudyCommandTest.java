package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.nwu.bioinformatics.commons.DateUtils;

import java.util.Calendar;
import java.util.Date;

import static org.easymock.classextension.EasyMock.expect;

public class SubjectOffStudyCommandTest extends StudyCalendarTestCase{
    private SubjectOffStudyCommand command;
    private SubjectService subjectService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        subjectService = registerMockFor(SubjectService.class);

        command = new SubjectOffStudyCommand();
        command.setSubjectService(subjectService);
    }

    public void testTakeSubjectOffStudy() throws Exception {
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        Date expectedEndDate = DateUtils.createDate(2007, Calendar.SEPTEMBER, 1);

        StudySubjectAssignment expectedAssignment = new StudySubjectAssignment();
        expectedAssignment.setEndDate(expectedEndDate);

        command.setAssignment(assignment);
        command.setExpectedEndDate(expectedEndDate);

        expect(subjectService.takeSubjectOffStudy(assignment, expectedEndDate)).andReturn(expectedAssignment);
        replayMocks();

        assertSame("Wrong end date", expectedAssignment.getEndDate(), command.takeSubjectOffStudy().getEndDate());
        verifyMocks();
    }
}
