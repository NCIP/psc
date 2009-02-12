package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.nwu.bioinformatics.commons.DateUtils;
import static org.easymock.classextension.EasyMock.expect;

import java.util.Calendar;
import java.util.Date;

public class SubjectOffStudyCommandTest extends StudyCalendarTestCase{
    private SubjectOffStudyCommand command;
    private SubjectService subjectService;

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
        expectedAssignment.setEndDateEpoch(expectedEndDate);

        command.setAssignment(assignment);
        command.setExpectedEndDate(expectedEndDate);

        expect(subjectService.takeSubjectOffStudy(assignment, expectedEndDate)).andReturn(expectedAssignment);
        replayMocks();

        assertSame("Wrong end date", expectedAssignment.getEndDateEpoch(), command.takeSubjectOffStudy().getEndDateEpoch());
        verifyMocks();


    }
}
