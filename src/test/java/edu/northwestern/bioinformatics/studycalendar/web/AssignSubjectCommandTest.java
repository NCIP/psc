package edu.northwestern.bioinformatics.studycalendar.web;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.nwu.bioinformatics.commons.DateUtils;
import static org.easymock.classextension.EasyMock.expect;
import org.easymock.classextension.EasyMock;
import org.easymock.IArgumentMatcher;

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
    private SubjectDao subjectDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        subjectService = registerMockFor(SubjectService.class);
        subjectDao = registerDaoMockFor(SubjectDao.class);
        command = new AssignSubjectCommand();
        command.setSubjectService(subjectService);
        command.setSubjectDao(subjectDao);
    }

    public void testAssignSubject() throws Exception {
        Subject subject = setId(11, createSubject("Fred", "Jones"));
        subject.setDateOfBirth(DateUtils.createDate(2008, 1, 12));
        Study study = createNamedInstance("Study A", Study.class);
        Site site = createNamedInstance("Northwestern", Site.class);
        StudySite studySite = setId(14, createStudySite(study, site));
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        Set<Population> populations = Collections.singleton(new Population());

        command.setStartDate(new Date());
        command.setStudy(study);
        command.setSite(site);
        command.setFirstName(subject.getFirstName());
        command.setLastName(subject.getLastName());
        command.setPersonId(subject.getPersonId());
        command.setDateOfBirth(subject.getDateOfBirth());
        command.setStudySegment(setId(17, Fixtures.createNamedInstance("Worcestershire", StudySegment.class)));
        command.setPopulations(populations);
        assignment.setSubject(subject);
        subjectDao.save(subjectEq(subject));


        expect(subjectService.assignSubject(subjectEq(subject), EasyMock.eq(studySite), EasyMock.eq(command.getStudySegment()), EasyMock.eq(command.getStartDate()), EasyMock.eq((User)null))).andReturn(assignment);
        subjectService.updatePopulations(assignment, populations);
        replayMocks();
        StudySubjectAssignment s = command.assignSubject();
        assertSame(assignment.getSubject().getFirstName(), s.getSubject().getFirstName());
        assertSame(assignment.getSubject().getLastName(), s.getSubject().getLastName());
        assertSame(assignment.getSubject().getDateOfBirth(), s.getSubject().getDateOfBirth());
        assertSame(assignment.getSubject().getPersonId(), s.getSubject().getPersonId());

        verifyMocks();
    }



    ////// CUSTOM MATCHERS

    private static Subject subjectEq(Subject expectedSubject) {
        EasyMock.reportMatcher(new SubjectMatcher(expectedSubject));
        return null;
    }

    private static class SubjectMatcher implements IArgumentMatcher {
        private Subject expectedSubject;

        public SubjectMatcher(Subject expectedSubject) {
            this.expectedSubject = expectedSubject;
        }

        public boolean matches(Object object) {
            Subject actual = (Subject) object;
            if ((expectedSubject.getPersonId() != null && actual.getPersonId() !=null && expectedSubject.getPersonId().equals(actual.getPersonId())) ||
                (expectedSubject.getFirstName().equals(actual.getFirstName()) &&
                 (expectedSubject.getLastName().equals(actual.getLastName())) &&
                    (expectedSubject.getDateOfBirth().equals(actual.getDateOfBirth())))) {
                        return true;
                    }
                return false;

            }
        public void appendTo(StringBuffer sb) {
            sb.append("Subject=").append(expectedSubject);
        }
    }
}
