package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarAuthorizationException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.SubjectProperty;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationScopeMappings;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.Registration;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;

import java.util.Calendar;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createBasicTemplate;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
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
    private PscUser sammyc, apploman;
    private StudySite studySite;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        studySegmentDao = registerDaoMockFor(StudySegmentDao.class);
        subjectService = registerMockFor(SubjectService.class);

        subject =  createSubject("P1", "FName", "LName", DateTools.createDate(1987, JANUARY, 4));
        // by default, expect the subject is new
        expect(subjectService.findSubject(subject)).andStubReturn(null);

        segment = new StudySegment();
        segment.setGridId("segment1");
        // by default, expect to find the segment
        expect(studySegmentDao.getByGridId(segment.getGridId())).andStubReturn(segment);

        sammyc = createPscUser("sammyc", PscRole.STUDY_SUBJECT_CALENDAR_MANAGER);
        apploman = createPscUser("apploman", PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER);
        SecurityContextHolderTestHelper.setSecurityContext(apploman);

        Study study = createBasicTemplate();
        study.setAssignedIdentifier("study");
        Site site = createSite("site", "site");
        studySite = createStudySite(study, site);
        PscUserService userService = registerMockFor(PscUserService.class);
        expect(userService.loadUserByUsername("sammyc")).andStubReturn(
            sammyc);

        registration = new Registration.Builder().
            firstStudySegment(segment).
            date(DateTools.createDate(2010, JANUARY, 25)).
            subject(subject).
            manager(createPscUser("sammyc")).
            toRegistration();

        service =  new RegistrationService();
        service.setStudySegmentDao(studySegmentDao);
        service.setSubjectService(subjectService);
        service.setPscUserDetailsService(userService);
        service.setApplicationSecurityManager(applicationSecurityManager);
    }

    public void testResolveRegistration() throws Exception {
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
        Subject subjectFound =  createSubject("P1", "FName", "LName", DateTools.createDate(1987, JANUARY, 4));
        subjectFound .setId(4);
        expect(subjectService.findSubject(subject)).andReturn(subjectFound );
        replayMocks();
        service.resolveRegistration(registration, studySite);
        verifyMocks();
        assertNotNull("Subject is not present in system", registration.getSubject().getId());
    }

    public void testResolveRegistrationMergesPropertiesWhenSubjectExists() throws Exception {
        Subject existing = setId(7,
            createSubject("P1", "DC", "DC", DateTools.createDate(1987, Calendar.JANUARY, 4)));
        existing.getProperties().add(new SubjectProperty("First language", "jp"));
        existing.getProperties().add(new SubjectProperty("Favorite color", "red"));
        expect(subjectService.findSubject(subject)).andReturn(existing);

        subject.getProperties().add(new SubjectProperty("Height", "150cm"));
        subject.getProperties().add(new SubjectProperty("Favorite color", "burgundy"));

        replayMocks(); service.resolveRegistration(registration, studySite); verifyMocks();

        List<SubjectProperty> actualProps = registration.getSubject().getProperties();
        assertEquals("Wrong number of resulting properties",
            3, actualProps.size());
        assertSubjectProperty("Unchanged property not left alone",
            "First language", "jp", actualProps.get(0));
        assertSubjectProperty("Existing property not updated",
            "Favorite color", "burgundy", actualProps.get(1));
        assertSubjectProperty("New property not appended",
            "Height", "150cm", actualProps.get(2));
    }

    private void assertSubjectProperty(String message, String expectedName, String expectedValue, SubjectProperty actual) {
        assertEquals(message + ": wrong name", expectedName, actual.getName());
        assertEquals(message + ": wrong value", expectedValue, actual.getValue());
    }

    public void testResolveRegistrationNewSubjectAndUserCanCreateNewSubjectForStudySite() throws Exception {
        apploman.getMemberships().put(SuiteRole.SUBJECT_MANAGER,
            AuthorizationScopeMappings.createSuiteRoleMembership(PscRole.SUBJECT_MANAGER).forAllSites());
        replayMocks();
        service.resolveRegistration(registration, studySite);
        verifyMocks();
        assertNull("Subject is present in system, No new Subject", registration.getSubject().getId());
    }

    public void testResolveRegistrationNewSubjectAndUserCanNotCreateNewSubjectForStudySite() throws Exception {
        apploman.getMemberships().put(SuiteRole.SUBJECT_MANAGER,
            AuthorizationScopeMappings.createSuiteRoleMembership(PscRole.SUBJECT_MANAGER).forSites(new Site()));
        replayMocks();
         try {
            service.resolveRegistration(registration, studySite);
            fail("Exception not thrown");
        } catch (StudyCalendarAuthorizationException scve) {
            assertEquals("apploman may not create a new subject.", scve.getMessage());
        }
    }

    public void testResolveRegistrationNewSubjectAndUserCanNotCreateNewSubject() throws Exception {
        replayMocks();
         try {
            service.resolveRegistration(registration, studySite);
            fail("Exception not thrown");
        } catch (StudyCalendarAuthorizationException scve) {
            assertEquals("apploman may not create a new subject.", scve.getMessage());
        }
    }
}
