/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityLabelDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityStateDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledStudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarMutableDomainObjectDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Parent;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.TransientCloneable;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChildrenChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PlannedCalendarDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationScopeMappings;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.cabig.ctms.domain.MutableDomainObject;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSession;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSessionFactory;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author Rhett Sutphin
 */
public class StudyServiceIntegratedTest extends DaoTestCase {
    private StudyService service;
    private StudyDao studyDao;
    private StudySiteDao studySiteDao;
    private DeltaService deltaService;
    private AmendmentService amendmentService;
    private PscUserService pscUserService;
    private ScheduledCalendarDao scheduledCalendarDao;
    private ScheduledActivityDao scheduledActivityDao;
    private ScheduledActivityStateDao scheduledActivityStateDao;
    private ScheduledStudySegmentDao scheduledStudySegmentDao;
    private StudySubjectAssignmentDao studySubjectAssignmentDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        service = (StudyService) getApplicationContext().getBean("studyService");
        deltaService = (DeltaService) getApplicationContext().getBean("deltaService");
        pscUserService = (PscUserService) getApplicationContext().getBean("pscUserService");
        studyDao = (StudyDao) getApplicationContext().getBean("studyDao");
        studySiteDao = (StudySiteDao) getApplicationContext().getBean("studySiteDao");
        amendmentService = (AmendmentService) getApplicationContext().getBean("amendmentService");
        scheduledCalendarDao = (ScheduledCalendarDao) getApplicationContext().getBean("scheduledCalendarDao");
        scheduledActivityDao = (ScheduledActivityDao) getApplicationContext().getBean("scheduledActivityDao");
        scheduledActivityStateDao = (ScheduledActivityStateDao) getApplicationContext().getBean("scheduledActivityStateDao");
        scheduledStudySegmentDao = (ScheduledStudySegmentDao) getApplicationContext().getBean("scheduledStudySegmentDao");
        studySubjectAssignmentDao = (StudySubjectAssignmentDao ) getApplicationContext().getBean("studySubjectAssignmentDao");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        SecurityContextHolderTestHelper.setSecurityContext(null);
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
        List<Change> changes = new ArrayList<Change>();
        assertNotNull(loaded);
        assertFalse("must have populations", loaded.getPopulations().isEmpty());
        assertEquals("must have 1 population", 1, loaded.getPopulations().size());
        Study copiedStudy = service.copy(loaded, loaded.getDevelopmentAmendment().getId());
        interruptSession();
        copiedStudy = studyDao.getById(copiedStudy.getId());
        for (Delta delta : copiedStudy.getDevelopmentAmendment().getDeltas()) {
            if(delta.getNode() instanceof Study) {
                changes = delta.getChanges();
            }
        }
        assertNotNull(copiedStudy);
        assertFalse("must add populations under study delta ", changes.isEmpty());
        assertEquals("must add populations under study delta", 1, changes.size());
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

    public void testFullTemplateHistoryIsTransient() throws Exception {
        int id = saveBasicSkeleton();
        Study fullHistory = service.getCompleteTemplateHistory(studyDao.getById(id));
        assertMemoryOnly(fullHistory);
        assertMemoryOnly(fullHistory.getPlannedCalendar());
        for (Amendment amendment : fullHistory.getAmendmentsList()) {
            for (Delta<?> delta : amendment.getDeltas()) {
                for (Change change : delta.getChanges()) {
                    if (change instanceof ChildrenChange) {
                        assertMemoryOnly(((ChildrenChange) change).getChild());
                    }
                }
            }
        }
    }

    public void testPurgeStudy() {
        {
            Study loaded = studyDao.getById(-1);
            assertNotNull("must have a study", loaded);

            service.purge(loaded);
        }

        interruptSession();

        Study reloaded = studyDao.getById(-1);
        assertNull("should be purged", reloaded);
    }

    class StudySecondaryIdentifierDao extends StudyCalendarMutableDomainObjectDao<StudySecondaryIdentifier> {
        public Class<StudySecondaryIdentifier> domainClass() { return StudySecondaryIdentifier.class; }
    }
    public void testPurgeStudySecondaryIdentifiers() {
        StudySecondaryIdentifierDao studySecondaryIdentifierDao = new StudySecondaryIdentifierDao();
        studySecondaryIdentifierDao.setHibernateTemplate(studyDao.getHibernateTemplate());

        {
            Study loaded = studyDao.getById(-1);
            assertNotNull(loaded);

            Set<StudySecondaryIdentifier> current = loaded.getSecondaryIdentifiers();
            assertTrue("must have secondary idents", current.size() > 0);
            assertEquals("assignment should exist", -77, current.iterator().next().getId().intValue());

            service.purge(loaded);
        }

        interruptSession();

        StudySecondaryIdentifier reloaded = studySecondaryIdentifierDao.getById(-77);
        assertNull("should be purged", reloaded);
    }

    public void testPurgeStudySite() {
        {
            Study loaded = studyDao.getById(-1);
            assertNotNull(loaded);

            List<StudySite> current = loaded.getStudySites();
            assertTrue("must have assignments", current.size() > 0);
            assertEquals("assignment should exist", -300, current.get(0).getId().intValue());

            service.purge(loaded);
        }

        interruptSession();

        StudySite reloaded = studySiteDao.getById(-300);
        assertNull("should be purged", reloaded);
    }

    public void testPurgeStudySubjectAssignment() {
        {
            Study loaded = studyDao.getById(-1);
            assertNotNull(loaded);

            List<StudySubjectAssignment> current = loaded.getStudySites().get(0).getStudySubjectAssignments();
            assertTrue("must have assignments", current.size() > 0);
            assertEquals("assignment should exist", -500, current.get(0).getId().intValue());

            service.purge(loaded);
        }

        interruptSession();

        StudySubjectAssignment reloaded = studySubjectAssignmentDao.getById(-500);
        assertNull("should be purged", reloaded);
    }

    public void testPurgeScheduledCalendar() {
        {
            Study loaded = studyDao.getById(-1);
            assertNotNull(loaded);

            ScheduledCalendar current = loaded.getStudySites().get(0).getStudySubjectAssignments().get(0).getScheduledCalendar();
            assertNotNull("must have scheduled calendar", current);

            service.purge(loaded);
        }

        interruptSession();

        ScheduledCalendar reloaded = scheduledCalendarDao.getById(-600);
        assertNull("should be purged", reloaded);
    }

    public void testPurgeScheduledStudySegment() {
        {
            Study loaded = studyDao.getById(-1);
            assertNotNull(loaded);

            List<ScheduledStudySegment> current = loaded.getStudySites().get(0).getStudySubjectAssignments().get(0).getScheduledCalendar().getScheduledStudySegments();
            assertTrue("must have scheduled segments", current.size() > 0);
            assertEquals("should include segment", -700, current.get(0).getId().intValue());

            service.purge(loaded);
        }

        interruptSession();

        ScheduledStudySegment reloaded = scheduledStudySegmentDao.getById(-700);
        assertNull("should be purged", reloaded);
    }

    public void testPurgeScheduledActivities() {
        {
            Study loaded = studyDao.getById(-1);
            assertNotNull(loaded);

            List<ScheduledActivity> current = loaded.getStudySites().get(0).getStudySubjectAssignments().get(0).getScheduledCalendar().getScheduledStudySegments().get(0).getActivities();
            assertTrue("must have scheduled activities", current.size() > 0);
            assertEquals("should include activity", -800, current.get(0).getId().intValue());

            service.purge(loaded);
        }

        interruptSession();

        ScheduledActivity reloaded = scheduledActivityDao.getById(-800);
        assertNull("should be purged", reloaded);
    }

    public void testPurgeCurrentActivityState() {
        {
            Study loaded = studyDao.getById(-1);
            assertNotNull(loaded);

            List<ScheduledActivityState> current = loaded.getStudySites().get(0).getStudySubjectAssignments().get(0).getScheduledCalendar().getScheduledStudySegments().get(0).getActivities().get(0).getAllStates();
            assertTrue("must have scheduled activities", current.size() > 0);
            assertEquals("should include activity", -900, current.get(0).getId().intValue());

            service.purge(loaded);
        }

        interruptSession();

        ScheduledActivityState reloaded = scheduledActivityStateDao.getById(-900);
        assertNull("should be purged", reloaded);
    }
    
    public void testPurgePlannedCalendar() {
        PlannedCalendarDao plannedCalendarDao = (PlannedCalendarDao) getApplicationContext().getBean("plannedCalendarDao");
        {
            Study loaded = studyDao.getById(-1);
            assertNotNull(loaded);

            PlannedCalendar current = loaded.getPlannedCalendar();
            assertEquals("must have planned calendar", -1, current.getId().intValue());

            service.purge(loaded);
        }

        interruptSession();

        PlannedCalendar reloaded = plannedCalendarDao.getById(-1);
        assertNull("should be purged", reloaded);
    }

    public void testPurgeEpoch() {
        EpochDao epochDao = (EpochDao) getApplicationContext().getBean("epochDao");
        {
            Study loaded = studyDao.getById(-1);
            assertNotNull(loaded);

            List<Epoch> current = loaded.getPlannedCalendar().getEpochs();
            assertTrue("must have epochs", current.size() > 0);
            assertEquals("should include epoch", -11, current.get(0).getId().intValue());

            service.purge(loaded);
        }

        interruptSession();

        Epoch reloaded = epochDao.getById(-11);
        assertNull("should be purged", reloaded);
    }

    public void testPurgeStudySegment() {
        StudySegmentDao studySegmentDao = (StudySegmentDao) getApplicationContext().getBean("studySegmentDao");
        {
            Study loaded = studyDao.getById(-1);
            assertNotNull(loaded);

            List<StudySegment> current = loaded.getPlannedCalendar().getEpochs().get(0).getStudySegments();
            assertTrue("must have study segments", current.size() > 0);
            assertEquals("should include epoch", -32, current.get(0).getId().intValue());

            service.purge(loaded);
        }

        interruptSession();

        StudySegment reloaded = studySegmentDao.getById(-32);
        assertNull("should be purged", reloaded);
    }

    public void testPurgePeriod() {
        PeriodDao periodDao = (PeriodDao) getApplicationContext().getBean("periodDao");
        {
            Study loaded = studyDao.getById(-1);
            assertNotNull(loaded);

            Set<Period> current = loaded.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0).getPeriods();
            assertTrue("must have periods", current.size() > 0);
            assertEquals("should include period", -2, current.iterator().next().getId().intValue());

            service.purge(loaded);
        }

        interruptSession();

        Period reloaded = periodDao.getById(-2);
        assertNull("should be purged", reloaded);
    }

    public void testPurgePlannedActivity() {
        PlannedActivityDao plannedActivityDao = (PlannedActivityDao) getApplicationContext().getBean("plannedActivityDao");
        {
            Study loaded = studyDao.getById(-1);
            assertNotNull(loaded);

            List<PlannedActivity> current = loaded.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0).getPeriods().iterator().next().getPlannedActivities();
            assertTrue("must have planend activities", current.size() > 0);
            assertEquals("should include planned activities", -2004, current.get(0).getId().intValue());

            service.purge(loaded);
        }

        interruptSession();

        PlannedActivity reloaded = plannedActivityDao.getById(-2004);
        assertNull("should be purged", reloaded);
    }

    public void testPurgePlannedActivityLabel() {
        PlannedActivityLabelDao plannedActivityLabelDao = (PlannedActivityLabelDao) getApplicationContext().getBean("plannedActivityLabelDao");
        {
            Study loaded = studyDao.getById(-1);
            assertNotNull(loaded);

            Set<PlannedActivityLabel> current = loaded.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0).getPeriods().iterator().next().getPlannedActivities().get(0).getPlannedActivityLabels();
            assertTrue("must have planend activity labels", current.size() > 0);
            assertEquals("should include planned activity labels", -3001, current.iterator().next().getId().intValue());

            service.purge(loaded);
        }

        interruptSession();

        PlannedActivityLabel reloaded = plannedActivityLabelDao.getById(-3001);
        assertNull("should be purged", reloaded);
    }

    public void testPurgeAmendment() {
        AmendmentDao amendmentDao = (AmendmentDao) getApplicationContext().getBean("amendmentDao");
        {
            Study loaded = studyDao.getById(-1);
            assertNotNull(loaded);

            Amendment current = loaded.getAmendment();

            assertEquals("should include previous amendment", -220, current.getId().intValue());

            service.purge(loaded);
        }

        interruptSession();

        Amendment reloaded = amendmentDao.getById(-220);
        assertNull("should be purged", reloaded);
    }

    public void testPurgePreviousAmendment() {
        AmendmentDao amendmentDao = (AmendmentDao) getApplicationContext().getBean("amendmentDao");
        {
            Study loaded = studyDao.getById(-1);
            assertNotNull(loaded);

            Amendment current = loaded.getAmendment().getPreviousAmendment();

            assertEquals("should include amendment", -200, current.getId().intValue());

            service.purge(loaded);
        }

        interruptSession();

        Amendment reloaded = amendmentDao.getById(-200);
        assertNull("should be purged", reloaded);
    }

    public void testPurgeDevelopmentAmendment() {
        AmendmentDao amendmentDao = (AmendmentDao) getApplicationContext().getBean("amendmentDao");
        {
            Study loaded = studyDao.getById(-1);
            assertNotNull(loaded);

            Amendment current = loaded.getDevelopmentAmendment();

            assertEquals("should include development amendment", -1, current.getId().intValue());

            service.purge(loaded);
        }

        interruptSession();

        Amendment reloaded = amendmentDao.getById(-1);
        assertNull("should be purged", reloaded);
    }

    public void testPurgePreviousDevelopmentAmendment() {
        AmendmentDao amendmentDao = (AmendmentDao) getApplicationContext().getBean("amendmentDao");
        {
            Study loaded = studyDao.getById(-1);
            assertNotNull(loaded);

            Amendment current = loaded.getDevelopmentAmendment().getPreviousAmendment();

            assertEquals("should include previous development amendment", -2, current.getId().intValue());

            service.purge(loaded);
        }

        interruptSession();

        Amendment reloaded = amendmentDao.getById(-2);
        assertNull("should be purged", reloaded);
    }

    class AmendmentApprovalDao extends StudyCalendarMutableDomainObjectDao<AmendmentApproval> {
        public Class<AmendmentApproval> domainClass() { return AmendmentApproval.class; }
    }
    public void testPurgeAmendmentApproval() {
        AmendmentApprovalDao amendmentApprovalDao = new AmendmentApprovalDao();
        amendmentApprovalDao.setHibernateTemplate(studyDao.getHibernateTemplate());
        {
            Study loaded = studyDao.getById(-1);
            assertNotNull(loaded);

            List<AmendmentApproval> current = loaded.getStudySites().get(0).getAmendmentApprovals();

            assertTrue("must have amendment approvals", current.size() > 0);
            assertEquals("should include amendment approval", -33, current.get(0).getId().intValue());

            service.purge(loaded);
        }

        interruptSession();

        AmendmentApproval reloaded = amendmentApprovalDao.getById(-33);
        assertNull("should be purged", reloaded);
    }

    public void testPurgeDelta() {
        DeltaDao deltaDao = (DeltaDao) getApplicationContext().getBean("deltaDao");
        {
            Study loaded = studyDao.getById(-1);
            assertNotNull(loaded);

            List<Delta<?>> current = loaded.getDevelopmentAmendment().getDeltas();
            assertTrue("must have planend deltas", current.size() > 0);
            assertEquals("should include delta", -1, current.get(0).getId().intValue());

            service.purge(loaded);
        }

        interruptSession();

        Delta reloaded = deltaDao.getById(-1);
        assertNull("should be purged", reloaded);
    }

    public void testPurgeChange() {
        ChangeDao changeDao = (ChangeDao) getApplicationContext().getBean("changeDao");
        {
            Study loaded = studyDao.getById(-1);
            assertNotNull(loaded);

            List<Change> current = loaded.getDevelopmentAmendment().getDeltas().get(0).getChanges();
            assertTrue("must have planend changes", current.size() > 0);
            assertEquals("should include changes", -2001, current.get(0).getId().intValue());

            service.purge(loaded);
        }

        interruptSession();

        Change reloaded = changeDao.getById(-2001);
        assertNull("should be purged", reloaded);
    }

    private void assertMemoryOnly(TransientCloneable<?> node) {
        assertTrue(node + " is not memory-only", node.isMemoryOnly());
        if (node instanceof Parent) {
            for (Object child : ((Parent) node).getChildren()) {
                assertMemoryOnly((TransientCloneable<?>) child);
            }
        }
    }

    public void testFullTemplateHistoryIncludesReleasedRemovedNodes() throws Exception {
        int id = saveBasicSkeleton();

        {
            Study reloaded = studyDao.getById(id);
            amendmentService.amend(reloaded);
            Epoch treatment = reloaded.getPlannedCalendar().getEpochs().get(0);
            Amendment dev = Fixtures.createAmendment("Bye-bye C", DateTools.createDate(2007, Calendar.APRIL, 6));
            reloaded.setDevelopmentAmendment(dev);
            dev.addDelta(
                Delta.createDeltaFor(treatment, Remove.create(treatment.getStudySegments().get(2)))
            );
            amendmentService.amend(reloaded);
        }

        interruptSession();

        Study fullHistory = service.getCompleteTemplateHistory(studyDao.getById(id));
        PlannedCalendarDelta initialDelta = (PlannedCalendarDelta) fullHistory.getAmendment().getPreviousAmendment().getDeltas().get(0);
        assertEquals("Wrong number of changes", 2, initialDelta.getChanges().size());
        Add treatmentAdd = (Add) initialDelta.getChanges().get(0);
        assertTrue("Treatment Add doesn't add an Epoch: " + treatmentAdd.getChild(),
            treatmentAdd.getChild() instanceof Epoch);
        Epoch treatment = (Epoch) treatmentAdd.getChild();
        assertEquals(
            "Wrong number of study segments in treatment epoch of initial template: " + treatment.getStudySegments(),
            3, treatment.getStudySegments().size());
    }

    public void testFullTemplateHistoryIncludesAllAmendments() throws Exception {
        int id = saveBasicSkeleton();
        {
            Study reloaded = studyDao.getById(id);
            Amendment a1 = Fixtures.createAmendment("A1", DateTools.createDate(2007, Calendar.APRIL, 6));
            reloaded.setDevelopmentAmendment(a1);
            amendmentService.amend(reloaded);
            Amendment a2 = Fixtures.createAmendment("A2", DateTools.createDate(2007, Calendar.APRIL, 7));
            reloaded.setDevelopmentAmendment(a2);
            amendmentService.amend(reloaded);
            Amendment a3 = Fixtures.createAmendment("A3", DateTools.createDate(2007, Calendar.APRIL, 8));
            reloaded.setDevelopmentAmendment(a3);
            amendmentService.amend(reloaded);
        }

        interruptSession();
        Study fullHistory = service.getCompleteTemplateHistory(studyDao.getById(id));
        assertEquals("Wrong number of amendments", 3, fullHistory.getAmendmentsList().size());
        assertEquals("Wrong previous amendment key", "2007-04-07~A2", fullHistory.getAmendment().getPreviousAmendment().getNaturalKey());
    }

    public void testFullTemplateHistoryResolveAddForCurrentAmendment() throws Exception {
        int id = saveBasicSkeleton();
        {
            Study reloaded = studyDao.getById(id);
            amendmentService.amend(reloaded);
            Epoch treatment = reloaded.getPlannedCalendar().getEpochs().get(0);
            Amendment a1 = Fixtures.createAmendment("A1", DateTools.createDate(2007, Calendar.APRIL, 6));
            reloaded.setDevelopmentAmendment(a1);
            amendmentService.amend(reloaded);
            Amendment a2 = Fixtures.createAmendment("A2", DateTools.createDate(2007, Calendar.APRIL, 8));
            reloaded.setDevelopmentAmendment(a2);
            StudySegment segment = new StudySegment();
            segment.setName("S1");
            a2.addDelta(
                Delta.createDeltaFor(treatment, Add.create(segment)));
            amendmentService.amend(reloaded);
        }
        interruptSession();
        Study fullHistory = service.getCompleteTemplateHistory(studyDao.getById(id));
        ChildrenChange add = (ChildrenChange) (fullHistory.getAmendment().getDeltas().get(0).getChanges().get(0));
        assertNotNull("Add does not have child node", add.getChild());
        assertEquals("Add has wrong child class", StudySegment.class, add.getChild().getClass());
    }

    public void testDefaultManagingSitesApplyWhenUserSitesAreDetached() throws Exception {
        SuiteRoleMembership sc =
            AuthorizationScopeMappings.createSuiteRoleMembership(PscRole.STUDY_CREATOR).forSites("OS-75");
        createProvisioningSession(-45).replaceRole(sc);

        interruptSession();

        PscUser alice = pscUserService.getAuthorizableUser("alice");
        SecurityContextHolderTestHelper.setSecurityContext(alice);
        // force load & check assumptions
        assertEquals("Test setup failure",
            1, alice.getMembership(PscRole.STUDY_CREATOR).getSites().size());
        assertEquals("Test setup failure",
            "Old site",
            ((Site) alice.getMembership(PscRole.STUDY_CREATOR).getSites().get(0)).getName());

        interruptSession();

        int id = saveBasicSkeleton();
        Study reloaded = studyDao.getById(id);
        assertEquals("Wrong number of managing sites", 1, reloaded.getManagingSites().size());
        assertEquals("Wrong managing site",
            "Old site", reloaded.getManagingSites().iterator().next().getName());

        // manual teardown because it's not possible to put csm_user_group roles in the
        // testdata in a database-independent way
        createProvisioningSession(-45).deleteRole(sc.getRole());
    }

    private ProvisioningSession createProvisioningSession(int userId) {
        return ((ProvisioningSessionFactory) getApplicationContext().getBean("provisioningSessionFactory")).
            createSession(userId);
    }
}
