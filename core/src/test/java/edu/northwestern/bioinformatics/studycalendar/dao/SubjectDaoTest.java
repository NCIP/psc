package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase.*;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.nwu.bioinformatics.commons.DateUtils;

import java.util.Date;
import java.util.List;

/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
public class SubjectDaoTest extends ContextDaoTestCase<SubjectDao> {
    private SiteDao siteDao = (SiteDao) getApplicationContext().getBean("siteDao");
    private StudyDao studyDao = (StudyDao) getApplicationContext().getBean("studyDao");
    private AmendmentDao amendmentDao = (AmendmentDao) getApplicationContext().getBean("amendmentDao");
    private StudySubjectAssignmentDao ssaDao = (StudySubjectAssignmentDao) getApplicationContext().getBean("studySubjectAssignmentDao");

    public void testGetAll() throws Exception {
        List<Subject> actual = getDao().getAll();
        assertEquals("Wrong size", 2, actual.size());
        assertEquals("Wrong first subject", "Ng", actual.get(0).getLastName());
        assertEquals("Wrong second subject", "Scott", actual.get(1).getLastName());
    }

    public void testGetById() throws Exception {
        Subject subject = getDao().getById(-100);
        assertNotNull("Subject not found", subject);
        assertEquals("Wrong last name", "Scott", subject.getLastName());
    }

    public void testSaveAssignment() throws Exception {
        {
            Site site = siteDao.getById(-1001);
            StudySite studySite = site.getStudySites().get(0);
            assertEquals("Wrong study site found in test setup", -3001, (int) studySite.getId());
            Subject subject = getDao().getById(-100);
            assertEquals("Subject should already have one assignment", 1, subject.getAssignments().size());

            StudySubjectAssignment spa = new StudySubjectAssignment();
            spa.setSubject(subject);
            spa.setStudySite(studySite);
            spa.setStartDate(new Date());
            spa.setEndDate(DateUtils.createDate(2008, 12, 1));
            spa.setCurrentAmendment(amendmentDao.getById(-55));

            subject.addAssignment(spa);

            getDao().save(subject);
        }

        interruptSession();

        Subject loaded = getDao().getById(-100);
        assertNotNull("Subject reloading failed", loaded);
        assertEquals("Assignment not saved", 2, loaded.getAssignments().size());
        StudySubjectAssignment newAssignment = loaded.getAssignments().get(1);
        assertEquals("Wrong subject", -100, (int) newAssignment.getSubject().getId());
        assertEquals("Wrong study site", -3001, (int) newAssignment.getStudySite().getId());
        assertSameDay("Wrong start date", new Date(), newAssignment.getStartDate());
        assertSameDay("Wrong end date", DateUtils.createDate(2008, 12, 1), newAssignment.getEndDate());
    }

    public void testSaveNewSubject() throws Exception {
        Integer savedId;
        {
            Subject subject = new Subject();
            subject.setFirstName("Jeff");
            subject.setLastName("Someone");
            subject.setGender(Gender.MALE);
            subject.setDateOfBirth(new Date());
            subject.setPersonId("123-45-6789");

            getDao().save(subject);
            savedId = subject.getId();
            assertNotNull("The saved subject id", savedId);
        }

        interruptSession();

        {
            Subject loaded = getDao().getById(savedId);
            assertNotNull("Could not reload subject id " + savedId, loaded);
            assertEquals("Wrong firstname", "Jeff", loaded.getFirstName());
            assertEquals("Wrong lastname", "Someone", loaded.getLastName());
            assertEquals("Wrong gender", Gender.MALE, loaded.getGender());
        }
    }

    public void testSaveSubjectWithoutPersonIdWorks() throws Exception {
        Integer savedId;
        {
            Subject subject = new Subject();
            subject.setFirstName("Jeff");
            subject.setLastName("Someone");
            subject.setGender(Gender.MALE);
            subject.setDateOfBirth(new Date());

            getDao().save(subject);
            savedId = subject.getId();
            assertNotNull("The saved subject id", savedId);
        }

        interruptSession();

        {
            Subject loaded = getDao().getById(savedId);
            assertNotNull("Could not reload subject id " + savedId, loaded);
            assertEquals("Wrong firstname", "Jeff", loaded.getFirstName());
            assertEquals("Wrong lastname", "Someone", loaded.getLastName());
            assertEquals("Wrong gender", Gender.MALE, loaded.getGender());
        }
    }

    public void testSaveSubjectWithOnlyPersonIdWorks() throws Exception {
        Integer savedId;
        {
            Subject subject = new Subject();
            subject.setGender(Gender.MALE);
            subject.setPersonId("123-45-6789");

            getDao().save(subject);
            savedId = subject.getId();
            assertNotNull("The saved subject id", savedId);
        }

        interruptSession();

        {
            Subject loaded = getDao().getById(savedId);
            assertNotNull("Could not reload subject id " + savedId, loaded);
            assertEquals("Wrong person ID", "123-45-6789", loaded.getPersonId());
        }
    }

    public void testLoadScheduledCalendar() throws Exception {
        Subject loaded = getDao().getById(-100);
        StudySubjectAssignment assignment = loaded.getAssignments().get(0);
        ScheduledCalendar actualCalendar = assignment.getScheduledCalendar();
        assertNotNull("Scheduled calendar not found", actualCalendar);
        assertEquals("Wrong scheduled calendar found", -11, (int) actualCalendar.getId());
        assertSame("Relationship not bidirectional", assignment, actualCalendar.getAssignment());
    }

    public void testGetAssignment() throws Exception {
        Subject subject = getDao().getById(-100);
        Study study = studyDao.getById(-2000);
        Site site = siteDao.getById(-1001);
        assertNotNull("Test setup error: no subject", subject);
        assertNotNull("Test setup error: no study", study);
        assertNotNull("Test setup error: no site", site);

        StudySubjectAssignment actual = getDao().getAssignment(subject, study, site);
        assertNotNull("No assignment found", actual);
        assertEquals("Wrong assignment found", -10, (int) actual.getId());
    }
    
    public void testGetByGridId() throws Exception {
        Subject subject = getDao().getByGridId("What is the");
        assertNotNull("No subject matched", subject);
        assertEquals("Wrong subject matched", -101, (int) subject.getId());
    }
    // Becuase of Constraint @Cascade(value = {CascadeType.ALL, CascadeType.DELETE_ORPHAN}) in Subject,
    // the deletion deletes subject and it's referred children
    public void testDeleteSubjectWithSubjectAssigment() throws Exception {
        Subject subject = getDao().getById(-100);
        getDao().delete(subject);
        Subject subject1 = getDao().getById(-100);
        assertNull("Subject is null, althouth it shoudln't be ", subject1);
        
    }
    public void testDeleteSubject() throws Exception {
        Subject subject = getDao().getById(-101);
        getDao().delete(subject);
        Subject subject1 = getDao().getById(-101);
        assertNull("Subject is not null, althouth it shoudl be ", subject1);
    }

    public void testFindSubjectByPersonIdWhenIdIsNull() throws Exception {
        Subject subject = getDao().findSubjectByPersonId(null);
        assertNull("Subject is not null ", subject);
    }
}