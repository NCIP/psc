package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import org.apache.commons.lang.StringUtils;
import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.expect;
import org.springframework.validation.BindException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
public class AssignSubjectCommandTest extends StudyCalendarTestCase {
    private AssignSubjectCommand command;
    private SubjectService subjectService;
    private SubjectDao subjectDao;
    private Subject subject;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        subjectService = registerMockFor(SubjectService.class);
        subjectDao = registerDaoMockFor(SubjectDao.class);
        command = new AssignSubjectCommand();
        command.setSubjectService(subjectService);
        command.setSubjectDao(subjectDao);

        subject = createSubject("11", "Fred", "Jones", createDate(2008, 1, 12),Gender.MALE);

        command.setFirstName(subject.getFirstName());
        command.setLastName(subject.getLastName());
        command.setPersonId(subject.getPersonId());
        command.setDateOfBirth(subject.getDateOfBirth());
        command.setStartDate(createDate(2008, 2, 1));
    }

    public void testAssignSubject() throws Exception {
        Study study = createNamedInstance("Study A", Study.class);
        Site site = createNamedInstance("Northwestern", Site.class);
        StudySite studySite = setId(14, createStudySite(study, site));
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        Set<Population> populations = Collections.singleton(new Population());

        command.setStartDate(new Date());
        command.setStudy(study);
        command.setSite(site);
        command.setStudySegment(setId(17, Fixtures.createNamedInstance("Worcestershire", StudySegment.class)));
        command.setPopulations(populations);
        assignment.setSubject(subject);
        subjectDao.save(subjectEq(subject));


        expect(subjectService.assignSubject(subjectEq(subject), EasyMock.eq(studySite), EasyMock.eq(command.getStudySegment()), EasyMock.eq(command.getStartDate()), EasyMock.eq((User)null))).andReturn(assignment);
        subjectService.updatePopulations(assignment, populations);
        replayMocks();

        StudySubjectAssignment actual = command.assignSubject();
        verifyMocks();

        assertSame("Assignment should be the same", assignment, actual);
    }

    public void testValidate() throws Exception {
        expect(subjectService.findSubjects(subjectEq(subject))).andReturn(Collections.singletonList(subject));
        replayMocks();

        BindException errors = new BindException(subject, StringUtils.EMPTY);
        command.validate(new BindException(subject, StringUtils.EMPTY));
        verifyMocks();

        assertFalse(errors.hasErrors());
    }

    public void testValidateWithExistingSubject() throws Exception {
        expect(subjectService.findSubjects(subjectEq(subject))).andReturn(Arrays.asList(subject, new Subject()));
        replayMocks();

        BindException errors = new BindException(subject, StringUtils.EMPTY);
        command.validate(errors);
        verifyMocks();

        assertTrue(errors.hasErrors());
        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.person.id.already.exists", errors.getFieldError().getCode());
    }

    public void testValidateWithExistingSubjectNoPersonId() throws Exception {
        subject.setPersonId(null);
        command.setPersonId(null);

        expect(subjectService.findSubjects(subjectEq(subject))).andReturn(Arrays.asList(subject, new Subject()));
        replayMocks();

        BindException errors = new BindException(subject, StringUtils.EMPTY);
        command.validate(errors);
        verifyMocks();

        assertTrue(errors.hasErrors());
        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.person.last.name.already.exists", errors.getFieldError().getCode());
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
            return (expectedSubject.getPersonId() != null && actual.getPersonId() != null && expectedSubject.getPersonId().equals(actual.getPersonId())) ||
                    (expectedSubject.getFirstName().equals(actual.getFirstName()) &&
                            (expectedSubject.getLastName().equals(actual.getLastName())) &&
                            (expectedSubject.getDateOfBirth().equals(actual.getDateOfBirth())));

        }
        public void appendTo(StringBuffer sb) {
            sb.append("Subject=").append(expectedSubject);
        }
    }
}
