package edu.northwestern.bioinformatics.studycalendar.web;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.classextension.EasyMock.expect;

import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
 * @author Rhett Sutphin
 */
public class AssignSubjectCommandTest extends StudyCalendarTestCase {
    private AssignSubjectCommand command;
    private SubjectService subjectService;

    @Override
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
        Set<Population> populations = Collections.singleton(new Population());

        command.setSubject(subject);
        command.setStartDate(new Date());
        command.setStudy(study);
        command.setSite(site);
        command.setStudySegment(setId(17, Fixtures.createNamedInstance("Worcestershire", StudySegment.class)));
        command.setPopulations(populations);

        expect(subjectService.assignSubject(subject, studySite, command.getStudySegment(), command.getStartDate(), null)).andReturn(assignment);
        subjectService.updatePopulations(assignment, populations);
        replayMocks();

        assertSame(assignment, command.assignSubject());
        verifyMocks();
    }
}
