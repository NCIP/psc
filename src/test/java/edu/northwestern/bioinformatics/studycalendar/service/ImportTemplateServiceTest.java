package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ImportTemplateServiceTest extends StudyCalendarTestCase {
    private ImportTemplateService service;
    private DaoFinder daoFinder;
    private PeriodDao periodDao;
    private StudyDao studyDao;
    private PlannedActivityDao plannedActivityDao;
    private StudySegmentDao studySegmentDao;
    private EpochDao epochDao;
    private ChangeDao changeDao;
    private DeltaDao deltaDao;
    private AmendmentDao amendmentDao;

    protected void setUp() throws Exception {
        super.setUp();

        registerMocks();

        service = service();
    }

    public void testDeletePlannedActivities() {
        PlannedActivity activity0 = new PlannedActivity();
        PlannedActivity activity1 = new PlannedActivity();

        List<PlannedActivity> activities = new ArrayList<PlannedActivity>();
        activities.add(activity0);
        activities.add(activity1);

        plannedActivityDao.delete(activity0);
        plannedActivityDao.delete(activity1);
        replayMocks();

        service.deletePlannedActivities(activities);
        verifyMocks();

        assertTrue("There should be no planned activities", activities.isEmpty());
    }

    public void testDeleteStudySegments() {
        StudySegment segment0 = createNamedInstance("Segment A", StudySegment.class);
        StudySegment segment1 = createNamedInstance("Segment B", StudySegment.class);

        List<StudySegment> segments = new ArrayList<StudySegment>();
        segments.add(segment0);
        segments.add(segment1);

        studySegmentDao.delete(segment0);
        studySegmentDao.delete(segment1);
        replayMocks();

        service.deleteStudySegments(segments);
        verifyMocks();

        assertTrue("There should be no study segments", segments.isEmpty());
    }

    public void testDeletePeriods() {
        Period period0 = createNamedInstance("Period A", Period.class);
        Period period1 = createNamedInstance("Period B", Period.class);

        Set<Period> periods = new TreeSet<Period>();
        periods.add(period0);
        periods.add(period1);

        periodDao.delete(period0);
        periodDao.delete(period1);
        replayMocks();

        service.deletePeriods(periods);
        verifyMocks();

        assertTrue("There should be no periods", periods.isEmpty());
    }

    public void testDeleteEpochs() {
        Epoch epoch0 = createNamedInstance("Epoch A", Epoch.class);
        Epoch epoch1 = createNamedInstance("Epoch B", Epoch.class);

        List<Epoch> epochs = new ArrayList<Epoch>();
        epochs.add(epoch0);
        epochs.add(epoch1);

        epochDao.delete(epoch0);
        epochDao.delete(epoch1);
        replayMocks();

        service.deleteEpochs(epochs);
        verifyMocks();

        assertTrue("There should be no epochs", epochs.isEmpty());
    }

    public void testDeleteDeltas() {
        Epoch epoch = setId(99, createNamedInstance("Epoch A", Epoch.class));
        StudySegment segment = createNamedInstance("Segment A", StudySegment.class);

        Change change0 = Add.create(epoch);
        ((Add)change0).setChildId(99);
        Change change1 = Remove.create(segment);

        Delta delta0 = Delta.createDeltaFor(new PlannedCalendar(), change0);
        Delta delta1 = Delta.createDeltaFor(new Epoch(), change1);

        List<Delta<?>> deltas = new ArrayList<Delta<?>>();
        deltas.add(delta0);
        deltas.add(delta1);

        deltaDao.delete(delta0);
        changeDao.delete(change0);
        expect(daoFinder.findDao(Epoch.class)).andReturn((DomainObjectDao) epochDao);
        expect(epochDao.getById(99)).andReturn(epoch);
        epochDao.delete(epoch);

        deltaDao.delete(delta1);
        changeDao.delete(change1);
        replayMocks();

        service.deleteDeltas(deltas);
        verifyMocks();

        assertTrue("There should be no changes in delta0", delta0.getChanges().isEmpty());
        assertTrue("There should be no changes in delta1", delta1.getChanges().isEmpty());
        assertTrue("There should be no deltas", deltas.isEmpty());
    }

    public void testDeleteAmendment() {
        Amendment amendment = new Amendment();

        amendmentDao.delete(amendment);
        replayMocks();

        service.deleteAmendment(amendment);
        verifyMocks();
    }

    ////// Helper Create Methods
    private void registerMocks() {
        studyDao = registerDaoMockFor(StudyDao.class);
        changeDao = registerDaoMockFor(ChangeDao.class);
        daoFinder = registerMockFor(DaoFinder.class);
        epochDao = registerDaoMockFor(EpochDao.class);
        deltaDao = registerDaoMockFor(DeltaDao.class);
        periodDao = registerDaoMockFor(PeriodDao.class);
        amendmentDao = registerDaoMockFor(AmendmentDao.class);
        studySegmentDao = registerDaoMockFor(StudySegmentDao.class);
        plannedActivityDao = registerDaoMockFor(PlannedActivityDao.class);
    }

    private ImportTemplateService service() {
        ImportTemplateService service = new ImportTemplateService();
        service.setStudyDao(studyDao);
        service.setEpochDao(epochDao);
        service.setDeltaDao(deltaDao);
        service.setChangeDao(changeDao);
        service.setDaoFinder(daoFinder);
        service.setPeriodDao(periodDao);
        service.setAmendmentDao(amendmentDao);
        service.setStudySegmentDao(studySegmentDao);
        service.setPlannedActivityDao(plannedActivityDao);
        return service;
    }
}
