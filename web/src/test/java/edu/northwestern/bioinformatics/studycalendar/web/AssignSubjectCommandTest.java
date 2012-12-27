/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Gender;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.Registration;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import org.apache.commons.lang.StringUtils;
import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;
import org.springframework.validation.BindException;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.createPscUser;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class AssignSubjectCommandTest extends StudyCalendarTestCase {
    private static final String STUDY_SUBJECT_ID = "SSId1";
    private static final Date BIRTH_DATE = DateTools.createDate(2008, Calendar.DECEMBER,  1, 0, 0, 0);
    private static final String BIRTH_DATE_S = "12/01/2008";
    private static final Date START_DATE = DateTools.createDate(2012, Calendar.FEBRUARY, 12, 0, 0, 0);
    private static final String START_DATE_S = "02/12/2012";

    private AssignSubjectCommand command;
    private SubjectService subjectService;
    private SubjectDao subjectDao;
    private Subject subject;
    // TODO: these should be constants on the command
    private String EXISTING = "existing";
    private String NEW = "new";
    private  StudySite studySite;
    private  StudySubjectAssignment assignment;
    private  Set<Population> populations;
    private StudySegment studySegment;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        subjectService = registerMockFor(SubjectService.class);
        subjectDao = registerDaoMockFor(SubjectDao.class);

        subject = createSubject("11", "Fred", "Jones", BIRTH_DATE, Gender.MALE);
        studySegment = setId(17,
            edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance("Worcestershire", StudySegment.class));
        Study study = createNamedInstance("Study A", Study.class);
        Site site = createNamedInstance("Northwestern", Site.class);
        studySite = setId(14, createStudySite(study, site));
        populations = Collections.singleton(new Population());

        command = new AssignSubjectCommand();
        command.setSubjectService(subjectService);
        command.setSubjectDao(subjectDao);
        command.setFirstName(subject.getFirstName());
        command.setLastName(subject.getLastName());
        command.setPersonId(subject.getPersonId());
        command.setGender(subject.getGender().getCode());
        command.setDateOfBirth(BIRTH_DATE_S);
        command.setRadioButton(NEW);
        command.setIdentifier(subject.getPersonId());
        command.setStartDate(START_DATE_S);
        command.setStudySubjectId(STUDY_SUBJECT_ID);
        command.setStudy(study);
        command.setSite(site);
        command.setStudySegment(studySegment);
        command.setPopulations(populations);

        assignment = new StudySubjectAssignment();
        assignment.setSubject(subject);
    }

    public void testAssignSubjectWhenUserCanCreateNewSubject() throws Exception {
        PscUser expectedManager = createPscUser("test_sm", PscRole.SUBJECT_MANAGER);
        command.setStudySubjectCalendarManager(expectedManager);

        subjectDao.save(subjectEq(subject));
        expect(subjectService.assignSubject(studySite,
            new Registration.Builder().manager(expectedManager).
                subject(subject).firstStudySegment(studySegment).date(START_DATE).
                studySubjectId(STUDY_SUBJECT_ID).populations(populations).
                toRegistration())
        ).andReturn(assignment);
        subjectService.updatePopulations(assignment, populations);
        replayMocks();

        StudySubjectAssignment actual = command.assignSubject();
        verifyMocks();

        assertSame("Assignment should be the same", assignment, actual);
    }

    public void testAssignSubjectWhenUserCanNotCreateNewSubject() throws Exception {
        PscUser expectedManager = createPscUser("test_sscm", PscRole.STUDY_SUBJECT_CALENDAR_MANAGER);
        command.setStudySubjectCalendarManager(expectedManager);

        replayMocks();
        try {
            command.assignSubject();
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException e) {
            assertEquals("test_sscm has insufficient privilege to create new subject.", e.getMessage());
        }
    }

    public void testAssignSubjectWhenExistingSubject() throws Exception {
        command.setRadioButton(EXISTING);
        expect(subjectDao.findSubjectByGridOrPersonId(subject.getPersonId())).andReturn(subject);
        expect(subjectService.assignSubject(studySite,
            new Registration.Builder().
                subject(subject).firstStudySegment(studySegment).date(START_DATE).
                studySubjectId(STUDY_SUBJECT_ID).populations(populations).
                toRegistration())
        ).andReturn(assignment);
        subjectService.updatePopulations(assignment, populations);
        replayMocks();
        StudySubjectAssignment actual = command.assignSubject();
        verifyMocks();
        assertSame("Assignment should be the same", assignment, actual);
    }

    public void testValidateNew() throws Exception {
        command.setRadioButton(NEW);
        expect(subjectService.findSubjects(subjectEq(subject))).andReturn(Collections.singletonList(subject));
        replayMocks();

        BindException errors = new BindException(subject, StringUtils.EMPTY);
        command.validate(new BindException(subject, StringUtils.EMPTY));
        verifyMocks();

        assertFalse(errors.hasErrors());
    }

    public void testValidateNewWithoutFirstName() throws Exception {
        command.setRadioButton(NEW);
        command.setPersonId(null);
        command.setFirstName(null);
        BindException errors = validateAndReturnErrors();
        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.subject.assignment.please.enter.person.id.and.or.first.last.birthdate", errors.getFieldError().getCode());
    }

    public void testValidateNewWithoutLastName() throws Exception {
        command.setRadioButton(NEW);
        command.setPersonId(null);
        command.setLastName(null);
        BindException errors = validateAndReturnErrors();
        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.subject.assignment.please.enter.person.id.and.or.first.last.birthdate", errors.getFieldError().getCode());
    }

    public void testValidateNewWithoutDoB() throws Exception {
        command.setRadioButton(NEW);
        command.setDateOfBirth(null);
        BindException errors = validateAndReturnErrors();
        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.subject.assignment.please.enter.date.of.birth", errors.getFieldError().getCode());
    }

    public void testValidateNewWithInvaidDoB() throws Exception {
        command.setRadioButton(NEW);
        command.setPersonId(null);
        command.setDateOfBirth("02/03");
        BindException errors = validateAndReturnErrors();
        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.subject.assignment.please.enter.date.of.birth", errors.getFieldError().getCode());
    }

    public void testValidateNewWithoutStartDate() throws Exception {
        command.setRadioButton(NEW);
        command.setStartDate(null);
        BindException errors = validateAndReturnErrors();
        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.subject.assignment.please.enter.a.start.date", errors.getFieldError().getCode());
    }

    ////// Helper Methods
    private BindException validateAndReturnErrors() {
        replayMocks();
        BindException errors = new BindException(command, StringUtils.EMPTY);
        command.validate(errors);
        verifyMocks();
        return errors;
    }


    public void testValidateExisting() throws Exception {
        command.setRadioButton(EXISTING);
        command.setIdentifier(subject.getPersonId());
        expect(subjectDao.findSubjectByGridOrPersonId(subject.getPersonId())).andReturn(subject);
        replayMocks();

        BindException errors = new BindException(subject, StringUtils.EMPTY);
        command.validate(new BindException(subject, StringUtils.EMPTY));
        verifyMocks();

        assertFalse(errors.hasErrors());
    }


    public void testValidateExistingByGridId() throws Exception {
        command.setRadioButton(EXISTING);
        command.setIdentifier("187");
        expect(subjectDao.findSubjectByGridOrPersonId("187")).andReturn(subject);
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

    public void testValidateWithEmptyRadioButtonSelected() throws Exception {
        command.setRadioButton(null);
        replayMocks();

        BindException errors = new BindException(command, StringUtils.EMPTY);
        command.validate(errors);
        verifyMocks();
        assertTrue(errors.hasErrors());
        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.subject.please.select.a.subject", errors.getFieldError().getCode());
    }

    public void testValidateWithRadioButtonExistingAndNullId() throws Exception {
        command.setRadioButton("existing");
        command.setIdentifier(null);
        replayMocks();

        BindException errors = new BindException(command, StringUtils.EMPTY);
        command.validate(errors);
        verifyMocks();
        assertTrue(errors.hasErrors());
        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.subject.please.select.a.subject", errors.getFieldError().getCode());
    }

    public void testValidateWithRadioButtonExistingAndEmptyId() throws Exception {
        command.setRadioButton("existing");
        command.setIdentifier("");
        replayMocks();

        BindException errors = new BindException(command, StringUtils.EMPTY);
        command.validate(errors);
        verifyMocks();
        assertTrue(errors.hasErrors());
        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.subject.please.select.a.subject", errors.getFieldError().getCode());
    }

    public void testValidateWithRadioButtonExistingAndEmptyStartDate() throws Exception {
        command.setRadioButton("existing");
        command.setIdentifier("123");
        command.setStartDate(null);
        replayMocks();

        BindException errors = new BindException(command, StringUtils.EMPTY);
        command.validate(errors);
        verifyMocks();
        assertTrue(errors.hasErrors());
        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.subject.assignment.please.enter.a.start.date", errors.getFieldError().getCode());
    }

    public void testValidateNewWithEmptyPersonIdAndFirstName() throws Exception {
        command.setRadioButton("new");
        command.setPersonId("");
        command.setFirstName("");
        replayMocks();

        BindException errors = new BindException(command, StringUtils.EMPTY);
        command.validate(errors);
        verifyMocks();
        assertTrue(errors.hasErrors());
        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.subject.assignment.please.enter.person.id.and.or.first.last.birthdate", errors.getFieldError().getCode());

    }

    public void testValidateNewWithEmptyPersonIdAndLastName() throws Exception {
        command.setRadioButton("new");
        command.setPersonId("");
        command.setLastName("");
        replayMocks();

        BindException errors = new BindException(command, StringUtils.EMPTY);
        command.validate(errors);
        verifyMocks();
        assertTrue(errors.hasErrors());
        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.subject.assignment.please.enter.person.id.and.or.first.last.birthdate", errors.getFieldError().getCode());

    }

    public void testValidateNewWithEmptyPersonIdAndDateOfBirth() throws Exception {
        command.setRadioButton("new");
        command.setPersonId("");
        command.setDateOfBirth(null);
        replayMocks();

        BindException errors = new BindException(command, StringUtils.EMPTY);
        command.validate(errors);
        verifyMocks();
        assertTrue(errors.hasErrors());
        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.subject.assignment.please.enter.person.id.and.or.first.last.birthdate", errors.getFieldError().getCode());

    }

    public void testValidateNewWithEmptyPersonIdAndStartDate() throws Exception {
        command.setRadioButton("new");
        command.setPersonId("123");
        command.setStartDate(null);
        replayMocks();

        BindException errors = new BindException(command, StringUtils.EMPTY);
        command.validate(errors);
        verifyMocks();
        assertTrue(errors.hasErrors());
        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.subject.assignment.please.enter.a.start.date", errors.getFieldError().getCode());

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
