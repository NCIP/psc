package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import static org.easymock.EasyMock.expect;

public class ImportTemplateServiceTest extends StudyCalendarTestCase {
    private PlannedActivity activity0;
    private PlannedActivity activity1;
    private PlannedActivity activity2;
    private ImportTemplateService service;
    private Study study;
    private ActivityDao activityDao;
    private SourceDao sourceDao;
    private Source source;
    private Period period;
    private DaoFinder daoFinder;
    private PeriodDao periodDao;
    private AmendmentService amendmentService;
    private StudyDao studyDao;
    private PlannedActivityDao plannedActivityDao;

    protected void setUp() throws Exception {
        super.setUp();

        studyDao = registerDaoMockFor(StudyDao.class);
        daoFinder = registerMockFor(DaoFinder.class);
        periodDao = registerDaoMockFor(PeriodDao.class);
        sourceDao = registerDaoMockFor(SourceDao.class);
        activityDao = registerDaoMockFor(ActivityDao.class);
        amendmentService = registerMockFor(AmendmentService.class);
        plannedActivityDao = registerDaoMockFor(PlannedActivityDao.class);

        service = new ImportTemplateService();
        service.setActivityDao(activityDao);
        service.setSourceDao(sourceDao);
        service.setStudyDao(studyDao);
        service.setAmendmentService(amendmentService);
        service.setDaoFinder(daoFinder);

        source = createNamedInstance("Source A", Source.class);

        activity0 = setGridId("grid0", createPlannedActivity("Bone Scan", 1));
        activity0.getActivity().setCode("BS");
        activity0.getActivity().setSource(source);

        activity1 = setGridId("grid1", createPlannedActivity("New Blood Measure", 2));
        activity1.getActivity().setCode("NBM");
        activity1.getActivity().setSource(source);

        activity2 = setGridId("grid2", createPlannedActivity("New Blood Measure", 3));
        activity2.getActivity().setCode("NBM");
        activity2.getActivity().setSource(source);

        PlannedActivity activity3 = setGridId("grid0", createPlannedActivity("Bone Scan", 1));

        period = setGridId("grid10", createNamedInstance("Period A", Period.class));

        Add add0 = Add.create(activity0);
        Delta delta0 = Delta.createDeltaFor(period, add0);

        Add add1 = Add.create(activity1);
        Delta delta1 = Delta.createDeltaFor(period, add1);

        Add add2 = Add.create(activity2);
        Delta delta2 = Delta.createDeltaFor(period, add2);

        Remove remove = Remove.create(activity3);
        Delta delta3 = Delta.createDeltaFor(period, remove);

        Amendment amendment1 = Fixtures.createAmendments("Amendment 0", "Amendment1");
        amendment1.addDelta(delta2);
        amendment1.addDelta(delta3);

        Amendment amendment0 = amendment1.getPreviousAmendment();
        amendment0.addDelta(delta0);
        amendment0.addDelta(delta1);


        study = createNamedInstance("Study A", Study.class);
        study.setAmendment(amendment1);
    }

    public void testResolveExistingActivitiesAndSources() {
        expect(activityDao.getByCodeAndSourceName("BS", "Source A")).andReturn(activity0.getActivity());
        sourceDao.save(source);
        activityDao.save(activity0.getActivity());

        expect(activityDao.getByCodeAndSourceName("NBM", "Source A")).andReturn(null);
        expect(sourceDao.getByName("Source A")).andReturn(null);
        sourceDao.save(source);
        activityDao.save(activity1.getActivity());

        expect(activityDao.getByCodeAndSourceName("NBM", "Source A")).andReturn(activity1.getActivity());
        sourceDao.save(source);
        activityDao.save(activity1.getActivity());
        replayMocks();

        service.resolveExistingActivitiesAndSources(study);
        verifyMocks();
    }

    public void testResolveChangeChildrenFromPlanTreeNodeTree() {
        studyDao.save(study);
        expect(daoFinder.findDao(Period.class)).andReturn((DomainObjectDao)periodDao).times(4);
        expect(periodDao.getByGridId("grid10")).andReturn(period).times(4);

        expect(daoFinder.findDao(PlannedActivity.class)).andReturn((DomainObjectDao)plannedActivityDao).times(4);
        expect(plannedActivityDao.getByGridId("grid0")).andReturn(activity0);
        expect(plannedActivityDao.getByGridId("grid1")).andReturn(activity1);
        expect(plannedActivityDao.getByGridId("grid2")).andReturn(activity2);
        expect(plannedActivityDao.getByGridId("grid0")).andReturn(activity0);

        amendmentService.amend(study);
        amendmentService.amend(study);
        replayMocks();

        service.resolveChangeChildrenFromPlanTreeNodeTree(study);
        verifyMocks();
    }
}
