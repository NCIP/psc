package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.service.ParticipantService;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.nwu.bioinformatics.commons.DateUtils;
import static org.easymock.classextension.EasyMock.expect;

import java.util.Calendar;
import java.util.Date;

public class ParticipantOffStudyCommandTest extends StudyCalendarTestCase{
    private ParticipantOffStudyCommand command;
    private ParticipantService participantService;

    protected void setUp() throws Exception {
        super.setUp();

        participantService = registerMockFor(ParticipantService.class);

        command = new ParticipantOffStudyCommand();
        command.setParticipantService(participantService);
    }

    public void testTakeParticipantOffStudy() throws Exception {
        StudyParticipantAssignment assignment = new StudyParticipantAssignment();
        Date expectedEndDate = DateUtils.createDate(2007, Calendar.SEPTEMBER, 1);

        StudyParticipantAssignment expectedAssignment = new StudyParticipantAssignment();
        expectedAssignment.setEndDateEpoch(expectedEndDate);

        command.setAssignment(assignment);
        command.setExpectedEndDate(expectedEndDate);

        expect(participantService.takeParticipantOffStudy(assignment, expectedEndDate)).andReturn(expectedAssignment);
        replayMocks();

        assertSame("Wrong end date", expectedAssignment.getEndDateEpoch(), command.takeParticipantOffStudy().getEndDateEpoch());
        verifyMocks();


    }
}
