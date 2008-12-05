package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import gov.nih.nci.cabig.ctms.domain.MutableDomainObject;

import java.util.List;
import java.util.SortedSet;

import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;

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
        Study copiedStudy = service.copy(loaded, loaded.getDevelopmentAmendment().getId());
        assertNotNull(copiedStudy);
        assertNotNull("must copy the development amendment ", copiedStudy.getDevelopmentAmendment());
        assertNull("must not copy  amendment ", copiedStudy.getAmendment());


    }

    public void testMustNotCopyStudySites() {
        Study loaded = studyDao.getById(-1);
        assertNotNull(loaded);
        assertFalse("must have study sites", loaded.getStudySites().isEmpty());
        Study copiedStudy = service.copy(loaded, loaded.getDevelopmentAmendment().getId());
        assertNotNull(copiedStudy);
        assertTrue("must not copy study sites", copiedStudy.getStudySites().isEmpty());


    }


    public void testCopyBasicProperties() {
        Study loaded = studyDao.getById(-1);
        assertNotNull(loaded);
        Study copiedStudy = service.copy(loaded, loaded.getDevelopmentAmendment().getId());
        assertNotNull(copiedStudy);
        assertEquals("must copy the assigned identifier ", "ECOG copy", copiedStudy.getAssignedIdentifier());
        assertEquals("must copy the long title ", "Study", copiedStudy.getLongTitle());
        assertNotSame("copied study and source study must be different", loaded, copiedStudy);
        validateIds(loaded, copiedStudy);
    }

    public void testCopyPopulations() {
        Study loaded = studyDao.getById(-1);
        assertNotNull(loaded);
        assertFalse("must have populations", loaded.getPopulations().isEmpty());
        assertEquals("must have 1 population", 1, loaded.getPopulations().size());
        Population population = loaded.getPopulations().iterator().next();

        Study copiedStudy = service.copy(loaded, loaded.getDevelopmentAmendment().getId());
        interruptSession();
        copiedStudy = studyDao.getById(copiedStudy.getId());
        assertNotNull(copiedStudy);
        assertFalse("must copy populations", copiedStudy.getPopulations().isEmpty());
        assertEquals("must copy the population", 1, copiedStudy.getPopulations().size());
        Population copiedPopulation = copiedStudy.getPopulations().iterator().next();

        assertSame("copied population  must belong to copied study", copiedPopulation.getStudy(), copiedStudy);

        validatePopulation(population, copiedPopulation);


    }


    public void testCopyPlannedCalendar() {
        Study loaded = studyDao.getById(-1);
        assertNotNull(loaded);
        PlannedCalendar plannedCalendar = loaded.getPlannedCalendar();

        assertNotNull("must have planned calendar", plannedCalendar);
        Study copiedStudy = service.copy(loaded, loaded.getDevelopmentAmendment().getId());

        assertNotNull(copiedStudy);
        PlannedCalendar copiedPlannedCalendar = copiedStudy.getPlannedCalendar();
        assertNotNull("must copy planned calendar", copiedPlannedCalendar);

        validateNode(plannedCalendar, copiedPlannedCalendar);
        assertSame("copied planned calendar  must belong to copied study", copiedPlannedCalendar.getStudy(), copiedStudy);


    }

    public void testCopyEpoch() {
        Study loaded = studyDao.getById(-1);
        assertNotNull(loaded);
        PlannedCalendar calendar = loaded.getPlannedCalendar();
        assertEquals("must have 1 epoch", 1, calendar.getEpochs().size());
        Epoch epoch = calendar.getEpochs().get(0);

        Study copiedStudy = service.copy(loaded, loaded.getDevelopmentAmendment().getId());
        assertNotNull(copiedStudy);
        PlannedCalendar copiedPlannedCalendar = copiedStudy.getPlannedCalendar();
        assertNotNull("must copy planned calendar", copiedPlannedCalendar);
        assertSame(copiedStudy, copiedPlannedCalendar.getStudy());

        assertNotNull("must copy the development amendment ", copiedStudy.getDevelopmentAmendment());
        Study revisedStudy = deltaService.revise(copiedStudy, copiedStudy.getDevelopmentAmendment());
        assertNotNull("must copy planned calendar", revisedStudy.getPlannedCalendar());
        assertEquals("must copy all epoch", 1, revisedStudy.getPlannedCalendar().getEpochs().size());
        Epoch copiedEpoch = revisedStudy.getPlannedCalendar().getEpochs().get(0);

        validateNode(epoch, copiedEpoch);
        assertSame("copied epoch  must belong to copied study", copiedEpoch.getParent(), revisedStudy.getPlannedCalendar());
        assertEquals("must copy name", epoch.getName(), copiedEpoch.getName());


    }

    public void testCopyStudySegments() {
        Study loaded = studyDao.getById(-1);
        assertNotNull(loaded);
        List<StudySegment> studySegments = loaded.getPlannedCalendar().getEpochs().get(0).getChildren();
        assertEquals("must have 1 study segment", 1, studySegments.size());
        StudySegment studySegment = studySegments.get(0);
        Study copiedStudy = service.copy(loaded, loaded.getDevelopmentAmendment().getId());

        assertNotNull(copiedStudy);

        assertNotNull("must copy the development amendment ", copiedStudy.getDevelopmentAmendment());

        Study revisedStudy = deltaService.revise(copiedStudy, copiedStudy.getDevelopmentAmendment());
        assertNotNull("must copy planned calendar", revisedStudy.getPlannedCalendar());
        assertEquals("must copy all epoch", 1, revisedStudy.getPlannedCalendar().getEpochs().size());
        List<StudySegment> copiedStudySegments = revisedStudy.getPlannedCalendar().getEpochs().get(0).getChildren();
        assertEquals("must have 1 study segment", 1, copiedStudySegments.size());
        StudySegment copiedStudySegment = copiedStudySegments.get(0);

        validateNode(studySegment, copiedStudySegment);
        assertSame("copied study segment  must belong to copied study", copiedStudySegment.getParent(), revisedStudy.getPlannedCalendar().getEpochs().get(0));
        assertEquals("must copy name", copiedStudySegment.getName(), studySegment.getName());


    }

    public void testCopyPeriods() {
        Study loaded = studyDao.getById(-1);
        loaded = deltaService.revise(loaded, loaded.getDevelopmentAmendment());

        assertNotNull(loaded);
        SortedSet<Period> periods = loaded.getPlannedCalendar().getEpochs().get(0).getChildren().get(0).getPeriods();
        assertEquals("must have 1 periods", 1, periods.size());
        Period period = periods.iterator().next();
        Study copiedStudy = service.copy(loaded, loaded.getDevelopmentAmendment().getId());
        assertNotNull(copiedStudy);

        Study revisedStudy = deltaService.revise(copiedStudy, copiedStudy.getDevelopmentAmendment());
        assertNotNull("must copy planned calendar", revisedStudy.getPlannedCalendar());
        assertEquals("must copy all epoch", 1, revisedStudy.getPlannedCalendar().getEpochs().size());

        SortedSet<Period> copiedPeriods = revisedStudy.getPlannedCalendar().getEpochs().get(0).getChildren().get(0).getPeriods();

        assertEquals("must have 1 periods", 1, copiedPeriods.size());
        Period copiedPeriod = copiedPeriods.iterator().next();


        validateNode(period, copiedPeriod);
        assertEquals("must copy name", copiedPeriod.getName(), period.getName());
        assertEquals("must copy repetitions", copiedPeriod.getRepetitions(), period.getRepetitions());
        assertEquals("must copy startDay", copiedPeriod.getStartDay(), period.getStartDay());
        assertEquals("must copy duration", copiedPeriod.getDuration(), period.getDuration());
        assertNotSame("duration must not be same reference", copiedPeriod.getDuration(), period.getDuration());


    }

    public void testCopyPlannedActivities() {
        Study loaded = studyDao.getById(-1);
        assertNotNull(loaded);
        loaded = deltaService.revise(loaded, loaded.getDevelopmentAmendment());

        List<PlannedActivity> plannedActivities = loaded.getPlannedCalendar().getEpochs().get(0).getChildren().get(0).getPeriods().iterator().next().getPlannedActivities();
        assertEquals("must have 1 planned activities", 1, plannedActivities.size());

        PlannedActivity plannedActivity = plannedActivities.get(0);
        Study copiedStudy = service.copy(loaded, loaded.getDevelopmentAmendment().getId());
        assertNotNull(copiedStudy);

        Study revisedStudy = deltaService.revise(copiedStudy, copiedStudy.getDevelopmentAmendment());
        assertNotNull("must copy planned calendar", revisedStudy.getPlannedCalendar());
        assertEquals("must copy all epoch", 1, revisedStudy.getPlannedCalendar().getEpochs().size());

        List<PlannedActivity> copiedPlannedActivities = revisedStudy.getPlannedCalendar().getEpochs().get(0).getChildren().get(0).getPeriods().iterator().next().getPlannedActivities();

        assertEquals("must have 1 planned activities", 1, copiedPlannedActivities.size());

        PlannedActivity copiedPlannedActivity = copiedPlannedActivities.get(0);
        validateNode(plannedActivity, copiedPlannedActivity);
        assertEquals("must copy activity", copiedPlannedActivity.getActivity(), plannedActivity.getActivity());
        assertSame("activity must be same", copiedPlannedActivity.getActivity(), plannedActivity.getActivity());
        assertEquals("must copy condition", copiedPlannedActivity.getCondition(), plannedActivity.getCondition());
        assertEquals("must copy day", copiedPlannedActivity.getDay(), plannedActivity.getDay());
        assertEquals("must copy details", copiedPlannedActivity.getDetails(), plannedActivity.getDetails());

    }

    public void testCopyPlannedActivitiesWithPopulations() {
        Study loaded = studyDao.getById(-1);
        assertNotNull(loaded);
        loaded = deltaService.revise(loaded, loaded.getDevelopmentAmendment());

        List<PlannedActivity> plannedActivities = loaded.getPlannedCalendar().getEpochs().get(0).getChildren().get(0).getPeriods().iterator().next().getPlannedActivities();
        assertEquals("must have 1 planned activities", 1, plannedActivities.size());
        Population population = plannedActivities.get(0).getPopulation();
        assertNotNull("planned activity must have population", population);

        Study copiedStudy = service.copy(loaded, loaded.getDevelopmentAmendment().getId());
        assertNotNull(copiedStudy);

        Study revisedStudy = deltaService.revise(copiedStudy, copiedStudy.getDevelopmentAmendment());
        assertNotNull("must copy planned calendar", revisedStudy.getPlannedCalendar());
        assertEquals("must copy all epoch", 1, revisedStudy.getPlannedCalendar().getEpochs().size());

        List<PlannedActivity> copiedPlannedActivities = revisedStudy.getPlannedCalendar().getEpochs().get(0).getChildren().get(0).getPeriods().iterator().next().getPlannedActivities();


        PlannedActivity copiedPlannedActivity = copiedPlannedActivities.get(0);

        assertEquals("must have 1 planned activities", 1, copiedPlannedActivities.size());
        Population copiedPopulation = copiedPlannedActivity.getPopulation();
        validatePopulation(population, copiedPopulation);
        assertSame("copied planned activity must have the population from copied study only", revisedStudy.getPopulations().iterator().next().getName(), copiedPopulation.getName());
        assertSame("copied planned activity must have the population from copied study only", revisedStudy.getPopulations().iterator().next().getAbbreviation(), copiedPopulation.getAbbreviation());

    }

    public void testCopyPlannedActivitiesWithLabels() {
        Study loaded = studyDao.getById(-1);
        assertNotNull(loaded);
        loaded = deltaService.revise(loaded, loaded.getDevelopmentAmendment());

        List<PlannedActivity> plannedActivities = loaded.getPlannedCalendar().getEpochs().get(0).getChildren().get(0).getPeriods().iterator().next().getPlannedActivities();
        assertEquals("must have 1 planned activities", 1, plannedActivities.size());
        assertFalse("planned activity must have labels", plannedActivities.get(0).getPlannedActivityLabels().isEmpty());
        PlannedActivityLabel plannedActivityLabel = plannedActivities.get(0).getPlannedActivityLabels().iterator().next();
        Study copiedStudy = service.copy(loaded, loaded.getDevelopmentAmendment().getId());
        assertNotNull(copiedStudy);

        Study revisedStudy = deltaService.revise(copiedStudy, copiedStudy.getDevelopmentAmendment());
        assertNotNull("must copy planned calendar", revisedStudy.getPlannedCalendar());
        assertEquals("must copy all epoch", 1, revisedStudy.getPlannedCalendar().getEpochs().size());

        List<PlannedActivity> copiedPlannedActivities = revisedStudy.getPlannedCalendar().getEpochs().get(0).getChildren().get(0).getPeriods().iterator().next().getPlannedActivities();

        assertEquals("must have 1 planned activities", 1, copiedPlannedActivities.size());
        assertFalse("planned activity must have labels", copiedPlannedActivities.get(0).getPlannedActivityLabels().isEmpty());

        PlannedActivityLabel copiedPlannedActivityLabel = copiedPlannedActivities.get(0).getPlannedActivityLabels().iterator().next();
        assertNotSame(" labels must not be same", plannedActivityLabel, copiedPlannedActivityLabel);
        validateIds(plannedActivityLabel, copiedPlannedActivityLabel);

    }

    private void validateIds(final MutableDomainObject object, final MutableDomainObject copiedObject) {
        assertNotNull(copiedObject.getId());
        assertNotNull(copiedObject.getGridId());
        assertFalse("copied object and source object must have different ids", copiedObject.getId().equals(object.getId()));
        assertFalse("copied object  and source object must have different grid ids", copiedObject.getGridId().equals(object.getGridId()));
    }

    private void validatePopulation(final Population population, final Population copiedPopulation) {
        assertNotNull(population);
        assertNotNull(copiedPopulation);
        assertNotSame("copied population and source population must be different", copiedPopulation, population);
        assertNotSame("copied population and source population must belong to different different studies", copiedPopulation.getStudy(), population.getStudy());
        validateIds(population, copiedPopulation);
        assertEquals("must copy the name ", copiedPopulation.getName(), population.getName());
        assertEquals("must copy the abbreviation ", copiedPopulation.getAbbreviation(), population.getAbbreviation());
    }

    private void validateNode(final PlanTreeNode planTreeNode, final PlanTreeNode copiedPlanTreeNode) {
        assertNotNull(planTreeNode);
        assertNotNull(copiedPlanTreeNode);

        assertNotSame("copied node and source node must be different", copiedPlanTreeNode, planTreeNode);
        assertNotSame("copied node and source node must have  different parent", copiedPlanTreeNode.getParent(), planTreeNode.getParent());
        validateIds(planTreeNode, copiedPlanTreeNode);
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
