package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationScopeMappings;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.xml.domain.Registration;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createBasicTemplate;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSite;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createStudySite;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.createPscUser;
import static java.util.Calendar.JANUARY;
import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class RegistrationServiceTest extends StudyCalendarTestCase {
    private RegistrationService service;

    private StudySegmentDao studySegmentDao;
    private SubjectService subjectService;

    private Registration registration;
    private Subject subject;
    private StudySegment segment;
    private PscUser sammyc;
    private StudySite studySite;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        studySegmentDao = registerDaoMockFor(StudySegmentDao.class);
        subjectService = registerMockFor(SubjectService.class);
        subject =  Fixtures.createSubject("P1", "FName", "LName", DateTools.createDate(1987, JANUARY, 4));

        segment = new StudySegment();
        segment.setGridId("segment1");

        sammyc = createPscUser("sammyc", PscRole.STUDY_SUBJECT_CALENDAR_MANAGER);

        Study study = createBasicTemplate();
        study.setAssignedIdentifier("study");
        Site site = createSite("site", "site");
        studySite = createStudySite(study, site);
        PscUserService userService = registerMockFor(PscUserService.class);
        expect(userService.loadUserByUsername("sammyc")).andStubReturn(
            sammyc);

        registration = Registration.create(segment, DateTools.createDate(2010, JANUARY, 25), subject);
        registration.setStudySubjectCalendarManager(createPscUser("sammyc"));

        service =  new RegistrationService();
        service.setStudySegmentDao(studySegmentDao);
        service.setSubjectService(subjectService);
        service.setPscUserDetailsService(userService);
    }

    public void testResolveRegistration() throws Exception {
        expect(studySegmentDao.getByGridId(segment.getGridId())).andReturn(segment);
        expect(subjectService.findSubject(subject)).andReturn(subject);
        replayMocks();
        service.resolveRegistration(registration, studySite);
        verifyMocks();

        assertEquals("Manager not resolved", 1, registration.getStudySubjectCalendarManager().getMemberships().size());
    }

    public void testResolveRegistrationWhenMatchingSegmentNotFound() throws Exception {
        expect(studySegmentDao.getByGridId(segment.getGridId())).andReturn(null);
        replayMocks();
        try {
            service.resolveRegistration(registration, studySite);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("Study Segment with grid id segment1 not found.", scve.getMessage());
        }
    }

    public void testResolveRegistrationWhenMatchingSubjectFound() throws Exception {
        Subject subjectFound =  Fixtures.createSubject("P1", "FName", "LName", DateTools.createDate(1987, JANUARY, 4));
        subjectFound .setId(4);
        expect(studySegmentDao.getByGridId(segment.getGridId())).andReturn(segment);
        expect(subjectService.findSubject(subject)).andReturn(subjectFound );
        replayMocks();
        service.resolveRegistration(registration, studySite);
        verifyMocks();
        assertNotNull("Subject is not present in system", registration.getSubject().getId());
    }

    public void testResolveRegistrationNewSubjectAndUserCanCreateNewSubjectForStudySite() throws Exception {
        sammyc.getMemberships().put(SuiteRole.SUBJECT_MANAGER, AuthorizationScopeMappings.createSuiteRoleMembership(PscRole.SUBJECT_MANAGER).forAllSites());
        expect(studySegmentDao.getByGridId(segment.getGridId())).andReturn(segment);
        expect(subjectService.findSubject(subject)).andReturn(null);
        replayMocks();
        service.resolveRegistration(registration, studySite);
        verifyMocks();
        assertNull("Subject is present in system, No new Subject", registration.getSubject().getId());
    }

    public void testResolveRegistrationNewSubjectAndUserCanNotCreateNewSubjectForStudySite() throws Exception {
        sammyc.getMemberships().put(SuiteRole.SUBJECT_MANAGER, AuthorizationScopeMappings.createSuiteRoleMembership(PscRole.SUBJECT_MANAGER).forSites(new Site()));
        expect(studySegmentDao.getByGridId(segment.getGridId())).andReturn(segment);
        expect(subjectService.findSubject(subject)).andReturn(null);
        replayMocks();
         try {
            service.resolveRegistration(registration, studySite);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("sammyc has insufficient privilege to create new subject.", scve.getMessage());
        }
    }

    public void testResolveRegistrationNewSubjectAndUserCanNotCreateNewSubject() throws Exception {
        expect(studySegmentDao.getByGridId(segment.getGridId())).andReturn(segment);
        expect(subjectService.findSubject(subject)).andReturn(null);
        replayMocks();
         try {
            service.resolveRegistration(registration, studySite);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("sammyc has insufficient privilege to create new subject.", scve.getMessage());
        }
    }
}
