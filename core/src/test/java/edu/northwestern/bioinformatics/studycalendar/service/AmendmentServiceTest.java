package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import edu.northwestern.bioinformatics.studycalendar.core.*;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.AmendmentMailMessage;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.MailMessageFactory;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.springframework.mail.MailSender;

import static org.easymock.EasyMock.*;

import static java.util.Calendar.*;
import java.util.*;

/**
 * @author Rhett Sutphin
 */
public class AmendmentServiceTest extends StudyCalendarTestCase {
    private AmendmentService service;
    private StudyService studyService;
    private DeltaService mockDeltaService;
    private TemplateService mockTemplateService;
    private AmendmentDao amendmentDao;
    private StudyDao studyDao;
    private PopulationService populationService;
    private DynamicMockDaoFinder daoFinder;

    private Study study;
    private Amendment a0, a1, a2, a3;
    private StudySite portlandSS;
    private PlannedCalendar calendar;
    private Subject subject;
    private StudySubjectAssignmentDao StudySubjectAssignmentDao;
    private TemplateDevelopmentService templateDevService;
    private MailSender mailSender;
    private MailMessageFactory mailMessageFactory;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studyService = registerMockFor(StudyService.class);
        amendmentDao = registerDaoMockFor(AmendmentDao.class);
        studyDao = registerDaoMockFor(StudyDao.class);
        populationService = registerMockFor(PopulationService.class);
        daoFinder = new DynamicMockDaoFinder();
        mailMessageFactory = registerMockFor(MailMessageFactory.class);
        mailSender = registerMockFor(MailSender.class);

        study = setGridId("STUDY-GRID", setId(300, createBasicTemplate()));
        calendar = setGridId("CAL-GRID", setId(400, study.getPlannedCalendar()));
        Epoch e0 = setGridId("E1-GRID", setId(1, calendar.getEpochs().get(0)));
        Epoch e1 = setGridId("E2-GRID", setId(2, calendar.getEpochs().get(1)));
        StudySegment e0a0 = setGridId("E1A0-GRID",
                setId(10, e0.getStudySegments().get(0)));

        a3 = createAmendments("A0", "A1", "A2", "A3");
        a2 = a3.getPreviousAmendment();
        a1 = a2.getPreviousAmendment();
        a0 = a1.getPreviousAmendment();
        study.setAmendment(a3);

        a2.addDelta(Delta.createDeltaFor(calendar, Add.create(e1)));
        a3.addDelta(Delta.createDeltaFor(e0, Add.create(e0a0, 0)));

        Site portland = setId(3, createNamedInstance("Portland", Site.class));
        portlandSS = setId(4, createStudySite(study, portland));
        portlandSS.approveAmendment(a0, DateTools.createDate(2004, JANUARY, 4));

        TestingTemplateService templateService = new TestingTemplateService();
        templateService.setDaoFinder(daoFinder);
        
        service = new AmendmentService();
        service.setStudyService(studyService);
        service.setDeltaService(Fixtures.getTestingDeltaService());
        service.setTemplateService(templateService);
        service.setAmendmentDao(amendmentDao);
        service.setStudyDao(studyDao);
        service.setPopulationService(populationService);
        service.setMailMessageFactory(mailMessageFactory);
        service.setMailSender(mailSender);

        mockTemplateService = registerMockFor(TemplateService.class);
        mockDeltaService = registerMockFor(DeltaService.class);

        subject = edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createSubject("first", "last");
        StudySubjectAssignmentDao = registerDaoMockFor(StudySubjectAssignmentDao.class);
        service.setStudySubjectAssignmentDao(StudySubjectAssignmentDao);

        templateDevService = new TemplateDevelopmentService();
        templateDevService.setAmendmentService(service);
        templateDevService.setTemplateService(mockTemplateService);
        templateDevService.setAmendmentDao(amendmentDao);
        templateDevService.setStudyDao(studyDao);
        templateDevService.setDaoFinder(daoFinder);
        templateDevService.setDeltaService(mockDeltaService);
        templateDevService.setStudyService(studyService);
    }

    @Override
    protected void replayMocks() {
        super.replayMocks();
        daoFinder.replayAll();
    }

    @Override
    protected void verifyMocks() {
        daoFinder.verifyAll();
        super.verifyMocks();
    }

    public void testAmend() throws Exception {
        assertEquals("Wrong number of epochs to start with", 2, calendar.getEpochs().size());
        assertEquals("Wrong number of amendments to start with", 3,
                study.getAmendment().getPreviousAmendmentsCount());

        Amendment inProgress = new Amendment("LTF");
        Epoch newEpoch = setGridId("E-NEW", setId(8, Epoch.create("Long term")));
        inProgress.addDelta(Delta.createDeltaFor(calendar, Add.create(newEpoch)));
        study.setDevelopmentAmendment(inProgress);

        studyService.save(study);

        replayMocks();
        service.amend(study);
        verifyMocks();

        assertEquals("Epoch not added", 3, study.getPlannedCalendar().getEpochs().size());
        assertEquals("Epoch not added in the expected location", 8,
                (int) study.getPlannedCalendar().getEpochs().get(2).getId());
        assertEquals("Development amendment did not become current", inProgress, study.getAmendment());
        assertEquals("Development amendment did not become current", "A3",
                study.getAmendment().getPreviousAmendment().getName());
        assertNull("Development amendment not moved to stack (still present as dev)", study.getDevelopmentAmendment());
        assertEquals("Wrong number of amendments on stack", 4, study.getAmendment().getPreviousAmendmentsCount());
    }

    public void testApproveNonMandatoryAmendmentDoesNotAmend() throws Exception {
        assertEquals("Test setup failure", 1, portlandSS.getAmendmentApprovals().size());

        service.setDeltaService(mockDeltaService);
        a1.setMandatory(false);
        AmendmentApproval expectedApproval = AmendmentApproval.create(a1, DateTools.createDate(2004, DECEMBER, 1));

        replayMocks();
        service.approve(portlandSS, expectedApproval);
        verifyMocks();
        assertEquals("Approval not recorded", 2, portlandSS.getAmendmentApprovals().size());
        assertSame(expectedApproval, portlandSS.getAmendmentApprovals().get(1));
    }

    public void testCreateNotificationsWhenNonMandatoryAmendmentIsApproved() throws Exception {
        assertEquals("Test setup failure", 1, portlandSS.getAmendmentApprovals().size());

        service.setDeltaService(mockDeltaService);
        a1.setMandatory(false);
        AmendmentApproval expectedApproval = AmendmentApproval.create(a1, DateTools.createDate(2004, DECEMBER, 1));
        StudySubjectAssignment assignment = Fixtures.createAssignment(study, portlandSS.getSite(), subject);
        portlandSS.addStudySubjectAssignment(assignment);
        StudySubjectAssignmentDao.save(assignment);

        replayMocks();
        service.approve(portlandSS, expectedApproval);
        verifyMocks();
        assertFalse("assignment must have one notification", assignment.getNotifications().isEmpty());

    }

    public void testApproveMandatoryAmendmentGenerallyAmends() throws Exception {
        assertEquals("Test setup failure", 1, portlandSS.getAmendmentApprovals().size());
        service.setDeltaService(mockDeltaService);

        StudySubjectAssignment assignment = new StudySubjectAssignment();
        assignment.setCurrentAmendment(a0);
        portlandSS.addStudySubjectAssignment(assignment);
        AmendmentApproval expectedApproval = AmendmentApproval.create(a1, DateTools.createDate(2004, DECEMBER, 1));

        mockDeltaService.amend(assignment, a1);

        replayMocks();
        service.approve(portlandSS, expectedApproval);
        verifyMocks();

        assertEquals("Approval not recorded", 2, portlandSS.getAmendmentApprovals().size());
        assertSame(expectedApproval, portlandSS.getAmendmentApprovals().get(1));
    }

    public void testApproveMandatoryAmendmentDoesNotAmendAssignmentWhenNotOnImmediatelyPrecedingAmendment() throws Exception {
        service.setDeltaService(mockDeltaService);
        portlandSS.approveAmendment(a1, DateTools.createDate(2005, JANUARY, 3));

        StudySubjectAssignment assignment = new StudySubjectAssignment();
        assignment.setCurrentAmendment(a0);
        portlandSS.addStudySubjectAssignment(assignment);
        AmendmentApproval expectedApproval = AmendmentApproval.create(a2, DateTools.createDate(2006, DECEMBER, 1));

        replayMocks();
        service.approve(portlandSS, expectedApproval);
        verifyMocks();

        assertEquals("Approval not recorded", 3, portlandSS.getAmendmentApprovals().size());
        assertSame(expectedApproval, portlandSS.getAmendmentApprovals().get(2));
    }

    public void testGetAmendedWhenAtAmendedLevel() throws Exception {
        Study actual = service.getAmendedStudy(study, study.getAmendment());
        assertEquals(study.getAmendment(), actual.getAmendment());
        assertTrue("Amended study is not marked transient", actual.isMemoryOnly());
    }

    public void testGetAmendedWhenAmendmentNotRelevant() throws Exception {
        study.setName("Study E");
        study.setLongTitle("Study E");
        Amendment irrelevant = setGridId("B0-GRID", createAmendments("B0"));
        try {
            service.getAmendedStudy(study, irrelevant);
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException e) {
            assertEquals("Amendment B0 (B0-GRID) does not apply to the template for Study E (STUDY-GRID)", e.getMessage());
        }
    }

    public void testGetAmendedOneRevBack() throws Exception {
        assertEquals("Test setup failure", 2, calendar.getEpochs().size());

        replayMocks();
        Study amended
                = service.getAmendedStudy(study, study.getAmendment().getPreviousAmendment());
        verifyMocks();

        assertNotNull(amended);
        assertNotSame(calendar, amended);
        assertEquals("Amended calendar reflects incorrect level", "A2",
                amended.getAmendment().getName());

        List<Epoch> actualEpochs = amended.getPlannedCalendar().getEpochs();
        List<StudySegment> actualE0StudySegments = actualEpochs.get(0).getStudySegments();
        assertEquals("StudySegment add not reverted: " + actualE0StudySegments, 2, actualE0StudySegments.size());
        assertEquals("Epoch add incorrectly reverted: " + actualEpochs, 2, actualEpochs.size());
    }

    public void testGetAmendedTwoRevsBack() throws Exception {
        assertEquals("Test setup failure", 2, calendar.getEpochs().size());
        assertEquals("Test setup failure", 3, calendar.getEpochs().get(0).getStudySegments().size());

        replayMocks();
        Study amended
                = service.getAmendedStudy(study, study.getAmendment().getPreviousAmendment().getPreviousAmendment());
        verifyMocks();

        assertNotNull(amended);
        assertNotSame(calendar, amended);

        List<StudySegment> actualE0StudySegments = amended.getPlannedCalendar().getEpochs().get(0).getStudySegments();
        assertEquals("StudySegment add in A3 not reverted: " + actualE0StudySegments, 2, actualE0StudySegments.size());
        assertEquals("Epoch add in A2 not reverted: " + amended.getPlannedCalendar().getEpochs(), 1,
                amended.getPlannedCalendar().getEpochs().size());
        assertEquals("Amended calendar reflects incorrect level", "A1",
                amended.getAmendment().getName());
    }

    public void testUpdateDevAmendmentForStudyOnly() throws Exception {
        service.setDeltaService(mockDeltaService);
        Population population = new Population();
        population.setName("Adding a population");
        population.setAbbreviation("Abbreviation");
                
        Amendment expectedDevAmendment = new Amendment();
        study.setDevelopmentAmendment(expectedDevAmendment);
        Add expectedChange = Add.create(population);

        mockDeltaService.updateRevisionForStudy(expectedDevAmendment, study, expectedChange);
        studyService.save(study);
        replayMocks();
        service.updateDevelopmentAmendmentForStudyAndSave(study, expectedChange);
        verifyMocks();
    }

    public void testUpdateDevAmendmentForStudyWithPopulationsModifications() throws Exception {
        service.setDeltaService(mockDeltaService);
        Population population = new Population();
        population.setName("Adding a population");
        population.setAbbreviation("Abbreviation");

        Set<Population> setOfPopulations = new HashSet<Population>();
        setOfPopulations.add(population);
        study.setPopulations(setOfPopulations);        

        Population updatedPopulation = population;
        updatedPopulation.setName("New population name");
        Amendment expectedDevAmendment = new Amendment();
        study.setDevelopmentAmendment(expectedDevAmendment);

        List<Change> changes = new ArrayList<Change>();;
        Change expectedChangeName = PropertyChange.create("name", population.getName(), updatedPopulation.getName());
        Change expectedChangeAbbreviation = PropertyChange.create("abbreviation", population.getAbbreviation(), updatedPopulation.getAbbreviation());
        changes.add(expectedChangeName);
        changes.add(expectedChangeAbbreviation);

        for (Change change: changes) {
            mockDeltaService.updateRevision(expectedDevAmendment, population, change);
        }

        studyService.save(study);
        replayMocks();
        Population populationAfterUpdate = service.updateDevelopmentAmendmentForStudyAndSave(population, study, changes.toArray(new Change[changes.size()]));
        verifyMocks();
        assertEquals("Update population name is valid ", "New population name", populationAfterUpdate.getName());
        assertEquals("Update population abbreviation is valid ", "Abbreviation", populationAfterUpdate.getAbbreviation());
    }


    public void testUpdateDevAmendment() throws Exception {
        service.setDeltaService(mockDeltaService);

        Amendment expectedDevAmendment = new Amendment();
        study.setDevelopmentAmendment(expectedDevAmendment);
        Epoch epoch = calendar.getEpochs().get(1);
        Remove expectedChange = Remove.create(epoch.getStudySegments().get(0));

        mockDeltaService.updateRevision(expectedDevAmendment, epoch, expectedChange);
        replayMocks();
        service.updateDevelopmentAmendment(epoch, expectedChange);
        verifyMocks();
    }

    public void testUpdateDevAmendmentLooksUpOriginalNodeIfNecessary() throws Exception {
        service.setDeltaService(mockDeltaService);

        Amendment expectedDevAmendment = new Amendment();
        study.setDevelopmentAmendment(expectedDevAmendment);
        Epoch originalEpoch = calendar.getEpochs().get(1);
        Remove expectedChange = Remove.create(originalEpoch.getStudySegments().get(0));
        Epoch paramEpoch = (Epoch) originalEpoch.transientClone();

        EpochDao epochDao = daoFinder.expectDaoFor(Epoch.class, EpochDao.class);
        expect(epochDao.getByGridId(paramEpoch.getGridId())).andReturn(originalEpoch);
        mockDeltaService.updateRevision(expectedDevAmendment, originalEpoch, expectedChange);

        replayMocks();
        service.updateDevelopmentAmendment(paramEpoch, expectedChange);
        verifyMocks();
    }

    public void testDeleteDevelopmentAmendment() throws Exception {
        service.setDeltaService(mockDeltaService);

        Amendment dev = new Amendment();
        Epoch e = Epoch.create("E", "S0");
        StudySegment s0 = e.getStudySegments().get(0);
        StudySegment s1 = new StudySegment(), s2 = new StudySegment();
        Delta<Epoch> delta = Delta.createDeltaFor(
                e, Remove.create(s0), Add.create(s1), Add.create(s2));
        dev.addDelta(delta);
        study.setDevelopmentAmendment(dev);

        mockDeltaService.delete(delta);
        amendmentDao.delete(dev);
        studyService.save(study);

        replayMocks();
        templateDevService.deleteDevelopmentAmendment(study);
        assertNull("Should be no dev amendment left", study.getDevelopmentAmendment());
        verifyMocks();
    }

    public void testDeleteDevelopmentAmendmentWhenItsTheOnlyThing() throws Exception {
        service.setDeltaService(mockDeltaService);
        service.setTemplateService(mockTemplateService);

        Amendment dev = new Amendment();
        Delta<Epoch> d1 = Delta.createDeltaFor(Epoch.create("E"));
        Delta<Epoch> d2 = Delta.createDeltaFor(Epoch.create("F"));
        dev.addDelta(d1);
        dev.addDelta(d2);
        study.setAmendment(null);
        study.setDevelopmentAmendment(dev);

        mockDeltaService.delete(d1);
        mockDeltaService.delete(d2);
        mockTemplateService.delete(study.getPlannedCalendar());
        amendmentDao.delete(dev);
        studyDao.delete(study);

        replayMocks();
        templateDevService.deleteDevelopmentAmendment(study);
        verifyMocks();
    }

    public void testDeleteDevelopmentAmendmentOnly() throws Exception {
        service.setDeltaService(mockDeltaService);
        service.setTemplateService(mockTemplateService);

        Amendment dev = new Amendment();
        Delta<Epoch> d1 = Delta.createDeltaFor(Epoch.create("E"));
        Delta<Epoch> d2 = Delta.createDeltaFor(Epoch.create("F"));
        dev.addDelta(d1);
        dev.addDelta(d2);
        study.setAmendment(null);
        study.setDevelopmentAmendment(dev);

        mockDeltaService.delete(d1);
        mockDeltaService.delete(d2);
        amendmentDao.delete(dev);
        studyService.save(study);

        replayMocks();
//        service.deleteDevelopmentAmendmentOnly(study);
        templateDevService.deleteDevelopmentAmendmentOnly(study);
        assertNull("Should be no dev amendment left", study.getDevelopmentAmendment());
        verifyMocks();
    }

    public void testResolveAmendmentApprovalWhenAmendmentFoundForStudy() throws Exception {
        AmendmentApproval amendmentApproval = createAmendementApproval();
        Amendment existingAmendment = createAmendment("Amendment1", DateTools.createDate(2010, Calendar.APRIL, 1));
        existingAmendment.setId(1);

        assertNull(amendmentApproval.getAmendment().getId());
        expect(amendmentDao.getByNaturalKey(amendmentApproval.getAmendment().getNaturalKey(), study)).andReturn(existingAmendment);

        replayMocks();
        AmendmentApproval actual = service.resolveAmentmentApproval(amendmentApproval, study);
        verifyMocks();
        assertNotNull(actual.getAmendment().getId());
    }

    public void testResolveAmendmentApprovalWhenAmendmentNotFoundForStudy() throws Exception {
        study.setAssignedIdentifier("Study");
        AmendmentApproval amendmentApproval = createAmendementApproval();
        expect(amendmentDao.getByNaturalKey(amendmentApproval.getAmendment().getNaturalKey(), study)).andReturn(null);
        replayMocks();
        try {
            service.resolveAmentmentApproval(amendmentApproval, study);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("Amendment '2010-04-01~Amendment1' not found for study 'Study'.", scve.getMessage());
        }
    }

    public void testSendMailForNewAmendmentsInStudy() throws Exception {
        AmendmentMailMessage amendmentMailMessage = new AmendmentMailMessage();
        expect(mailMessageFactory.createAmendmentMailMessage(study, a1)).andReturn(amendmentMailMessage);
        mailSender.send(amendmentMailMessage);

        replayMocks();
        service.sendMailForNewAmendmentsInStudy(study, a1, Arrays.asList("user@email.com"));
        verifyMocks();
    }

    public void testSendMailToUserForPossiblyScheduleChange() throws Exception {
        assertEquals("Test setup failure", 1, portlandSS.getAmendmentApprovals().size());

        service.setDeltaService(mockDeltaService);
        a1.setMandatory(false);
        AmendmentApproval expectedApproval = AmendmentApproval.create(a1, DateTools.createDate(2004, DECEMBER, 1));
        StudySubjectAssignment assignment = Fixtures.createAssignment(study, portlandSS.getSite(), subject);
        User SSCM = AuthorizationObjectFactory.createCsmUser(1, "testUser");
        SSCM.setEmailId("testUser@email.com");
        assignment.setStudySubjectCalendarManager(SSCM);
        portlandSS.addStudySubjectAssignment(assignment);
        StudySubjectAssignmentDao.save(assignment);
        expectMailSentForNewAmendment();

        replayMocks();
        service.approve(portlandSS, expectedApproval);
        verifyMocks();
        assertFalse("assignment must have one notification", assignment.getNotifications().isEmpty());
    }


    //Helper Method
    private AmendmentApproval createAmendementApproval() {
        AmendmentApproval amendmentApproval = new AmendmentApproval();
        amendmentApproval.setDate(DateTools.createDate(2010, Calendar.APRIL, 2));
        Amendment amendment = createAmendment("Amendment1", DateTools.createDate(2010, Calendar.APRIL, 1));
        amendmentApproval.setAmendment(amendment);
        return amendmentApproval;
    }

    private void expectMailSentForNewAmendment() {
        AmendmentMailMessage amendmentMailMessage = new AmendmentMailMessage();
        expect(mailMessageFactory.createAmendmentMailMessage(study, a1)).andReturn(amendmentMailMessage);
        mailSender.send(amendmentMailMessage);
    }
}
