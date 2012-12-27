/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.nwu.bioinformatics.commons.DateUtils;
import org.apache.commons.lang.StringUtils;
import org.easymock.classextension.EasyMock;
import org.springframework.validation.BindException;

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
        String expectedEndDate = "09/01/2007";
        StudySubjectAssignment expectedAssignment = new StudySubjectAssignment();
        expectedAssignment.setEndDate(command.convertStringToDate(expectedEndDate));

        command.setAssignment(assignment);
        
        command.setExpectedEndDate(expectedEndDate);

        expect(subjectService.takeSubjectOffStudy(assignment, command.convertStringToDate(expectedEndDate))).andReturn(expectedAssignment);
        replayMocks();

        assertSame("Wrong end date", expectedAssignment.getEndDate(), command.takeSubjectOffStudy().getEndDate());
        verifyMocks();
    }

    public void testValidateWithIncorrectEndDate() throws Exception {
        command.setExpectedEndDate("02/03");
        BindException errors = validateAndReturnErrors();
        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.expected.end.date.is.not.correct", errors.getFieldError().getCode());
    }

    public void testValidateWithNullEndDate() throws Exception {
        command.setExpectedEndDate(null);
        BindException errors = validateAndReturnErrors();
        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.expected.end.date.is.not.selected", errors.getFieldError().getCode());
    }

    ////// Helper Methods
    private BindException validateAndReturnErrors() {
        replayMocks();
        BindException errors = new BindException(command, StringUtils.EMPTY);
        command.validate(errors);
        verifyMocks();
        return errors;
    }    
}
