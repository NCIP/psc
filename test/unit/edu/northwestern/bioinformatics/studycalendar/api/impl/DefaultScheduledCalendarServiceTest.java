package edu.northwestern.bioinformatics.studycalendar.api.impl;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.service.ParticipantService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreator;
import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;

import java.util.Date;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class DefaultScheduledCalendarServiceTest extends StudyCalendarTestCase {
    private static final String STUDY_BIG_ID = "STUDY-GUID";
    private static final String SITE_BIG_ID = "SITE-GUID";
    private static final String PARTICIPANT_BIG_ID = "PARTICPANT-GUID";
    private static final String ARM_BIG_ID = "ARM-GUID";

    private DefaultScheduledCalendarService service;
    private SiteDao siteDao;
    private StudyDao studyDao;
    private ParticipantDao participantDao;
    private ParticipantService participantService;

    private Study parameterStudy;
    private Site parameterSite;
    private Participant parameterParticipant;
    private Arm parameterArm;

    private Study loadedStudy;
    private Site loadedSite;
    private Participant loadedParticipant;
    private Arm loadedArm;

    protected void setUp() throws Exception {
        super.setUp();
        siteDao = registerDaoMockFor(SiteDao.class);
        studyDao = registerDaoMockFor(StudyDao.class);
        participantDao = registerDaoMockFor(ParticipantDao.class);
        participantService = registerMockFor(ParticipantService.class);

        service = new DefaultScheduledCalendarService();
        service.setParticipantDao(participantDao);
        service.setParticipantService(participantService);
        service.setStudyDao(studyDao);
        service.setSiteDao(siteDao);

        parameterStudy = setBigId(STUDY_BIG_ID, new Study());
        parameterSite = setBigId(SITE_BIG_ID, new Site());
        parameterParticipant = setBigId(PARTICIPANT_BIG_ID, new Participant());
        parameterArm = setBigId(ARM_BIG_ID, new Arm());

        loadedStudy = setBigId(STUDY_BIG_ID, TemplateSkeletonCreator.BASIC.create());
        loadedStudy.setBigId(STUDY_BIG_ID);
        loadedSite = setBigId(SITE_BIG_ID, createNamedInstance("NU", Site.class));
        loadedStudy.addSite(loadedSite);
        loadedArm = setBigId(ARM_BIG_ID, loadedStudy.getPlannedCalendar().getEpochs().get(1).getArms().get(0));

        loadedParticipant = setBigId(PARTICIPANT_BIG_ID, createParticipant("Edward", "Armor-o"));
    }

    private void setAssigned() {
        StudyParticipantAssignment assignment = new StudyParticipantAssignment();
        StudySite studySite = loadedStudy.getStudySites().get(0);
        assignment.setParticipant(loadedParticipant);
        assignment.setStudySite(studySite);
        assignment.setStartDateEpoch(new Date());
        studySite.getStudyParticipantAssignments().add(assignment);
        loadedParticipant.addAssignment(assignment);
        assignment.setScheduledCalendar(new ScheduledCalendar());
    }

    public void testBasicGet() throws Exception {
        setAssigned();
        StudyParticipantAssignment expectedAssignment = loadedParticipant.getAssignments().get(0);

        expectLoadStudy();
        expectLoadParticipant();
        expectLoadSite();

        expect(participantDao.getAssignment(loadedParticipant, loadedStudy, loadedSite)).andReturn(expectedAssignment);

        replayMocks();
        assertSame(expectedAssignment.getScheduledCalendar(),
            service.getScheduledCalendar(parameterStudy, parameterParticipant, parameterSite));
        verifyMocks();
    }

    public void testBasicGetWithNoSchedule() throws Exception {
        expectLoadStudy();
        expectLoadParticipant();
        expectLoadSite();
        expect(participantDao.getAssignment(loadedParticipant, loadedStudy, loadedSite)).andReturn(null);

        replayMocks();
        assertNull(service.getScheduledCalendar(parameterStudy, parameterParticipant, parameterSite));
        verifyMocks();
    }

    public void testGetInvalidStudy() throws Exception {
        expect(studyDao.getByBigId(parameterStudy)).andReturn(null);
        expectLoadSite();
        expectLoadParticipant();

        replayMocks();
        try {
            service.getScheduledCalendar(parameterStudy, parameterParticipant, parameterSite);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("Could not get schedule: no study with bigId " + STUDY_BIG_ID, iae.getMessage());
        }
        verifyMocks();
    }

    public void testGetInvalidSite() throws Exception {
        expect(siteDao.getByBigId(parameterSite)).andReturn(null);
        expectLoadStudy();
        expectLoadParticipant();

        replayMocks();
        try {
            service.getScheduledCalendar(parameterStudy, parameterParticipant, parameterSite);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("Could not get schedule: no site with bigId " + SITE_BIG_ID, iae.getMessage());
        }
        verifyMocks();
    }

    public void testGetInvalidParticipant() throws Exception {
        expect(participantDao.getByBigId(parameterParticipant)).andReturn(null);
        expectLoadStudy();
        expectLoadSite();

        replayMocks();
        try {
            service.getScheduledCalendar(parameterStudy, parameterParticipant, parameterSite);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("Could not get schedule: no participant with bigId " + PARTICIPANT_BIG_ID, iae.getMessage());
        }
        verifyMocks();
    }

    public void testGetWithNoStudyBigId() throws Exception {
        parameterStudy.setBigId(null);

        replayMocks();
        try {
            service.getScheduledCalendar(parameterStudy, parameterParticipant, parameterSite);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("Could not get schedule: no bigId on study parameter", iae.getMessage());
        }
        verifyMocks();
    }

    public void testGetWithNoParticpantBigId() throws Exception {
        parameterParticipant.setBigId(null);

        replayMocks();
        try {
            service.getScheduledCalendar(parameterStudy, parameterParticipant, parameterSite);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("Could not get schedule: no bigId on participant parameter", iae.getMessage());
        }
        verifyMocks();
    }

    public void testGetWithNoSiteBigId() throws Exception {
        parameterSite.setBigId(null);

        replayMocks();
        try {
            service.getScheduledCalendar(parameterStudy, parameterParticipant, parameterSite);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("Could not get schedule: no bigId on site parameter", iae.getMessage());
        }
        verifyMocks();
    }

    public void testAssignKnownParticipant() throws Exception {
        // TODO
    }

    public void testAssignUnknownParticpant() throws Exception {
        // TODO
    }

    public void testAssignAgain() throws Exception {
        // TODO
    }

    public void testAssignFromSiteNotAssociatedWithStudy() throws Exception {
        // TODO
    }

    public void testAssignFromArmNotInStudy() throws Exception {
        // TODO
    }


    private void expectLoadParticipant() {
        expect(participantDao.getByBigId(parameterParticipant)).andReturn(loadedParticipant);
    }

    private void expectLoadSite() {
        expect(siteDao.getByBigId(parameterSite)).andReturn(loadedSite);
    }

    private void expectLoadStudy() {
        expect(studyDao.getByBigId(parameterStudy)).andReturn(loadedStudy);
    }

}
