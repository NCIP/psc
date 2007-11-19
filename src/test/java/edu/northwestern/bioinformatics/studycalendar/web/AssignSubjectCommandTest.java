package edu.northwestern.bioinformatics.studycalendar.web;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.classextension.EasyMock.expect;

import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class AssignSubjectCommandTest extends StudyCalendarTestCase {
    private AssignSubjectCommand command;
    private SubjectService subjectService;

    protected void setUp() throws Exception {
        super.setUp();

        subjectService = registerMockFor(SubjectService.class);

        command = new AssignSubjectCommand();
        command.setSubjectService(subjectService);
    }

    public void testAssignSubject() throws Exception {
        Subject subject = setId(11, createSubject("Fred", "Jones"));
        Study study = createNamedInstance("Study A", Study.class);
        Site site = createNamedInstance("Northwestern", Site.class);
        StudySite studySite = setId(14, createStudySite(study, site));
        StudySubjectAssignment assignment = new StudySubjectAssignment();

        command.setSubject(subject);
        command.setStartDate(new Date());
        command.setStudy(study);
        command.setSite(site);
        command.setArm(setId(17, Fixtures.createNamedInstance("Worcestershire", Arm.class)));

        expect(subjectService.assignSubject(subject, studySite, command.getArm(), command.getStartDate(), null)).andReturn(assignment);
        replayMocks();

        assertSame(assignment, command.assignSubject());
        verifyMocks();
    }
}
