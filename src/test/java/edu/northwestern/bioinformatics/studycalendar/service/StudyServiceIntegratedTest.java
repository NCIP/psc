package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;

import java.util.List;
import java.util.SortedSet;

/**
 * @author Rhett Sutphin
 */
public class StudyServiceIntegratedTest extends DaoTestCase {
    private StudyService service;
    private StudyDao studyDao;
    private DeltaService deltaService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        service = (StudyService) getApplicationContext().getBean("studyService");
        deltaService = (DeltaService) getApplicationContext().getBean("deltaService");
        studyDao = (StudyDao) getApplicationContext().getBean("studyDao");
    }

    private int saveBasicSkeleton() {
        int id;
        Study blank = TemplateSkeletonCreator.BASIC.create(null);
        service.save(blank);

        assertNotNull("Not saved", blank.getId());
        id = blank.getId();
        interruptSession();
        return id;
    }

    public void testCopyDevelopmentAmendmentOnly() {
        Study loaded = studyDao.getById(-1);
        assertNotNull(loaded);
        assertNotNull(loaded.getDevelopmentAmendment());
        Study copiedStudy = service.copy(loaded);
        assertNotNull(copiedStudy);
        assertNotNull("must copy the development amendment ", copiedStudy.getDevelopmentAmendment());
        assertNull("must not copy  amendment ", copiedStudy.getAmendment());


    }

    public void testMustNotCopyStudySites() {
        Study loaded = studyDao.getById(-1);
        assertNotNull(loaded);
        assertFalse("must have study sites", loaded.getStudySites().isEmpty());
        Study copiedStudy = service.copy(loaded);
        assertNotNull(copiedStudy);
        assertTrue("must not copy study sites", copiedStudy.getStudySites().isEmpty());


    }


    public void testCopyBasicProperties() {
        Study loaded = studyDao.getById(-1);
        assertNotNull(loaded);
        Study copiedStudy = service.copy(loaded);
        assertNotNull(copiedStudy);
        assertEquals("must copy the assigned identifier ", "ECOG copy", copiedStudy.getAssignedIdentifier());
        assertEquals("must copy the long title ", "Study", copiedStudy.getLongTitle());

    }

    public void testCopyPopulations() {
        Study loaded = studyDao.getById(-1);
        assertNotNull(loaded);
        assertFalse("must have populations", loaded.getPopulations().isEmpty());
        assertEquals("must have 3 populations", 3, loaded.getPopulations().size());
        Study copiedStudy = service.copy(loaded);
        assertNotNull(copiedStudy);
        assertFalse("must copy populations", copiedStudy.getPopulations().isEmpty());
        assertEquals("must copy all 3 populations", 3, copiedStudy.getPopulations().size());

    }

    public void testCopyPlannedCalendar() {
        Study loaded = studyDao.getById(-1);
        assertNotNull(loaded);
        assertNotNull("must have planned calendar", loaded.getPlannedCalendar());
        Study copiedStudy = service.copy(loaded);
        assertNotNull(copiedStudy);
        assertNotNull("must copy planned calendar", copiedStudy.getPlannedCalendar());
        assertSame(copiedStudy, copiedStudy.getPlannedCalendar().getStudy());

    }

    public void testCopyEpoch() {
        Study loaded = studyDao.getById(-1);
        assertNotNull(loaded);
        PlannedCalendar calendar = loaded.getPlannedCalendar();
        assertEquals("must have 1 epoch", 1, calendar.getEpochs().size());

        Study copiedStudy = service.copy(loaded);
        assertNotNull(copiedStudy);
        PlannedCalendar copiedPlannedCalendar = copiedStudy.getPlannedCalendar();
        assertNotNull("must copy planned calendar", copiedPlannedCalendar);
        assertSame(copiedStudy, copiedPlannedCalendar.getStudy());

        assertNotNull("must copy the development amendment ", copiedStudy.getDevelopmentAmendment());

        Study revisedStudy = deltaService.revise(copiedStudy, copiedStudy.getDevelopmentAmendment());
        assertNotNull("must copy planned calendar", revisedStudy.getPlannedCalendar());

        assertEquals("must copy all epoch", 1, revisedStudy.getPlannedCalendar().getEpochs().size());

    }

    public void testCopyStudySegments() {
        Study loaded = studyDao.getById(-1);
        assertNotNull(loaded);
        List<StudySegment> studySegments = loaded.getPlannedCalendar().getEpochs().get(0).getChildren();
        assertEquals("must have 1 study segment", 1, studySegments.size());

        Study copiedStudy = service.copy(loaded);
        assertNotNull(copiedStudy);

        assertNotNull("must copy the development amendment ", copiedStudy.getDevelopmentAmendment());

        Study revisedStudy = deltaService.revise(copiedStudy, copiedStudy.getDevelopmentAmendment());
        assertNotNull("must copy planned calendar", revisedStudy.getPlannedCalendar());
        assertEquals("must copy all epoch", 1, revisedStudy.getPlannedCalendar().getEpochs().size());

        List<StudySegment> copiedStudySegments = revisedStudy.getPlannedCalendar().getEpochs().get(0).getChildren();

        assertEquals("must have 1 study segment", 1, copiedStudySegments.size());

    }

    public void testCopyPeriods() {
        Study loaded = studyDao.getById(-1);
        loaded = deltaService.revise(loaded, loaded.getDevelopmentAmendment());

        assertNotNull(loaded);
        SortedSet<Period> periods = loaded.getPlannedCalendar().getEpochs().get(0).getChildren().get(0).getPeriods();
        assertEquals("must have 1 periods", 1, periods.size());

        Study copiedStudy = service.copy(loaded);
        assertNotNull(copiedStudy);

        Study revisedStudy = deltaService.revise(copiedStudy, copiedStudy.getDevelopmentAmendment());
        assertNotNull("must copy planned calendar", revisedStudy.getPlannedCalendar());
        assertEquals("must copy all epoch", 1, revisedStudy.getPlannedCalendar().getEpochs().size());

        SortedSet<Period> copiedPeriods = revisedStudy.getPlannedCalendar().getEpochs().get(0).getChildren().get(0).getPeriods();

        assertEquals("must have 1 periods", 1, copiedPeriods.size());

    }

    public void testCopyPlannedActivities() {
        Study loaded = studyDao.getById(-1);
        assertNotNull(loaded);
        loaded = deltaService.revise(loaded, loaded.getDevelopmentAmendment());

        List<PlannedActivity> plannedActivities = loaded.getPlannedCalendar().getEpochs().get(0).getChildren().get(0).getPeriods().iterator().next().getPlannedActivities();
        assertEquals("must have 1 planned activities", 1, plannedActivities.size());

        Study copiedStudy = service.copy(loaded);
        assertNotNull(copiedStudy);

        Study revisedStudy = deltaService.revise(copiedStudy, copiedStudy.getDevelopmentAmendment());
        assertNotNull("must copy planned calendar", revisedStudy.getPlannedCalendar());
        assertEquals("must copy all epoch", 1, revisedStudy.getPlannedCalendar().getEpochs().size());

        List<PlannedActivity> copiedPlannedActivities = revisedStudy.getPlannedCalendar().getEpochs().get(0).getChildren().get(0).getPeriods().iterator().next().getPlannedActivities();

        assertEquals("must have 1 planned activities", 1, copiedPlannedActivities.size());

    }

    public void testCopyPlannedActivitiesWithPopulations() {
        Study loaded = studyDao.getById(-1);
        assertNotNull(loaded);
        loaded = deltaService.revise(loaded, loaded.getDevelopmentAmendment());

        List<PlannedActivity> plannedActivities = loaded.getPlannedCalendar().getEpochs().get(0).getChildren().get(0).getPeriods().iterator().next().getPlannedActivities();
        assertEquals("must have 1 planned activities", 1, plannedActivities.size());
        Population population = plannedActivities.get(0).getPopulation();
        assertNotNull("planned activity must have population", population);
        Study copiedStudy = service.copy(loaded);
        assertNotNull(copiedStudy);

        Study revisedStudy = deltaService.revise(copiedStudy, copiedStudy.getDevelopmentAmendment());
        assertNotNull("must copy planned calendar", revisedStudy.getPlannedCalendar());
        assertEquals("must copy all epoch", 1, revisedStudy.getPlannedCalendar().getEpochs().size());

        List<PlannedActivity> copiedPlannedActivities = revisedStudy.getPlannedCalendar().getEpochs().get(0).getChildren().get(0).getPeriods().iterator().next().getPlannedActivities();

        assertEquals("must have 1 planned activities", 1, copiedPlannedActivities.size());
        Population copiedPopulation = copiedPlannedActivities.get(0).getPopulation();
        assertNotNull("planned activity must have population", copiedPopulation);
        assertEquals("must copy new instance of population ", population.getAbbreviation(), copiedPopulation.getAbbreviation());
        assertNotSame(" population must not be same", population, copiedPopulation);

    }

    public void testCopyPlannedActivitiesWithLabels() {
        Study loaded = studyDao.getById(-1);
        assertNotNull(loaded);
        loaded = deltaService.revise(loaded, loaded.getDevelopmentAmendment());
        
        List<PlannedActivity> plannedActivities = loaded.getPlannedCalendar().getEpochs().get(0).getChildren().get(0).getPeriods().iterator().next().getPlannedActivities();
        assertEquals("must have 1 planned activities", 1, plannedActivities.size());
        assertFalse("planned activity must have labels", plannedActivities.get(0).getPlannedActivityLabels().isEmpty());
        PlannedActivityLabel plannedActivityLabel = plannedActivities.get(0).getPlannedActivityLabels().iterator().next();
        Study copiedStudy = service.copy(loaded);
        assertNotNull(copiedStudy);

        Study revisedStudy = deltaService.revise(copiedStudy, copiedStudy.getDevelopmentAmendment());
        assertNotNull("must copy planned calendar", revisedStudy.getPlannedCalendar());
        assertEquals("must copy all epoch", 1, revisedStudy.getPlannedCalendar().getEpochs().size());

        List<PlannedActivity> copiedPlannedActivities = revisedStudy.getPlannedCalendar().getEpochs().get(0).getChildren().get(0).getPeriods().iterator().next().getPlannedActivities();

        assertEquals("must have 1 planned activities", 1, copiedPlannedActivities.size());
        assertFalse("planned activity must have labels", copiedPlannedActivities.get(0).getPlannedActivityLabels().isEmpty());

        PlannedActivityLabel copiedPlannedActivityLabel = copiedPlannedActivities.get(0).getPlannedActivityLabels().iterator().next();
        assertNotSame(" labels must not be same", plannedActivityLabel, copiedPlannedActivityLabel);

    }

    public void testSaveBasicSkeleton() throws Exception {
        int id = saveBasicSkeleton();

        Study reloaded = studyDao.getById(id);
        assertEquals("Amendment not saved", 1, reloaded.getDevelopmentAmendment().getDeltas().size());
        List<Change> changes = reloaded.getDevelopmentAmendment().getDeltas().get(0).getChanges();
        // The detail of the epochs are tested in more detail in TemplateSkeletonCreatorTest
        assertEquals("Wrong number of changes", 2, changes.size());
        assertNotNull("First epoch ID not saved with amendment",
                ((Add) changes.get(0)).getChildId());
        assertNotNull("Second epoch ID not saved with amendment",
                ((Add) changes.get(1)).getChildId());
    }

    public void testSaveWithChangedDeltaDoesNotUpdateAmendment() throws Exception {
        int id = saveBasicSkeleton();

        int originalAmendmentVersion;
        {
            Study original = studyDao.getById(id);
            originalAmendmentVersion = original.getDevelopmentAmendment().getVersion();
            log.info("Development amendment under test has id={}", original.getDevelopmentAmendment().getId());
            Delta<?> originalDelta = original.getDevelopmentAmendment().getDeltas().get(0);
            assertEquals("Test setup failure: wrong number of changes in delta", 2,
                    originalDelta.getChanges().size());
            originalDelta.addChange(Add.create(Epoch.create("N")));
            service.save(original);
        }

        interruptSession();

        Study reloaded = studyDao.getById(id);
        Delta<?> reloadedDelta = reloaded.getDevelopmentAmendment().getDeltas().get(0);
        assertEquals("New change not present in reloaded study", 3,
                reloadedDelta.getChanges().size());
        assertTrue("New change not present in reloaded study", reloadedDelta.getChanges().get(2) instanceof Add);

        assertEquals("Amendment version changed even though the amendment itself didn't change",
                originalAmendmentVersion, (int) reloaded.getDevelopmentAmendment().getVersion());
    }
}
