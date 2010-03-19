package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.xml.domain.Registration;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;

import static java.util.Calendar.JANUARY;

import gov.nih.nci.cabig.ctms.lang.DateTools;
import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class RegistrationServiceTest extends StudyCalendarTestCase {
    private StudySegmentDao studySegmentDao;
    private UserDao userDao;
    private SubjectService subjectService;
    private RegistrationService service;
    private Registration registration;
    private Subject subject;
    private StudySegment segment;
    private User subjectCo;

    public void setUp() throws Exception {
        super.setUp();
        service =  new RegistrationService();
        studySegmentDao = registerDaoMockFor(StudySegmentDao.class);
        userDao = registerDaoMockFor(UserDao.class);
        subjectService = registerMockFor(SubjectService.class);
        service.setStudySegmentDao(studySegmentDao);
        service.setSubjectService(subjectService);
        service.setUserDao(userDao);
        subject =  Fixtures.createSubject("P1", "FName", "LName", DateTools.createDate(1987, JANUARY, 4));

        segment = new StudySegment();
        segment.setGridId("segment1");
        subjectCo = new User();
        subjectCo.setName("subjectCo");
        registration = Registration.create(segment, DateTools.createDate(2010, JANUARY, 25), subject);
        registration.setSubjectCoordinator(subjectCo);
    }

    public void testResolveRegistration() throws Exception {
        expect(userDao.getByName(subjectCo.getName())).andReturn(subjectCo);
        expect(studySegmentDao.getByGridId(segment.getGridId())).andReturn(segment);
        expect(subjectService.findSubject(subject)).andReturn(subject);
        replayMocks();
        service.resolveRegistration(registration);
        verifyMocks();
    }

    public void testResolveRegistrationWhenMatchingSegmentNotFound() throws Exception {
        expect(userDao.getByName(subjectCo.getName())).andReturn(subjectCo);
        expect(studySegmentDao.getByGridId(segment.getGridId())).andReturn(null);
        replayMocks();
        try {
            service.resolveRegistration(registration);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("Study Segment with grid id segment1 not found.", scve.getMessage());
        }
    }

    public void testResolveRegistrationWhenMatchingSubjectFound() throws Exception {
        Subject subjectFound =  Fixtures.createSubject("P1", "FName", "LName", DateTools.createDate(1987, JANUARY, 4));
        subjectFound .setId(4);
        expect(userDao.getByName(subjectCo.getName())).andReturn(subjectCo);
        expect(studySegmentDao.getByGridId(segment.getGridId())).andReturn(segment);
        expect(subjectService.findSubject(subject)).andReturn(subjectFound );
        replayMocks();
        service.resolveRegistration(registration);
        verifyMocks();
        assertNotNull("Subject is not present in system", registration.getSubject().getId());
    }

    public void testResolveRegistrationWhenMatchingSubjectNotFound() throws Exception {
        expect(userDao.getByName(subjectCo.getName())).andReturn(subjectCo);
        expect(studySegmentDao.getByGridId(segment.getGridId())).andReturn(segment);
        expect(subjectService.findSubject(subject)).andReturn(null);
        replayMocks();
        service.resolveRegistration(registration);
        verifyMocks();
        assertNull("Subject is present in system, No new Subject", registration.getSubject().getId());
    }
}
