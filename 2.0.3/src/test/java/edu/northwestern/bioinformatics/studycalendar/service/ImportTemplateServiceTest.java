package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ImportTemplateServiceTest extends StudyCalendarTestCase {
    private PlannedActivity activity0, activity1, activity2, activity3, activity4, activity5;
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
    private StudyService studyService;
    private StudySegmentDao studySegmentDao;
    private EpochDao epochDao;
    private ChangeDao changeDao;
    private DeltaDao deltaDao;
    private AmendmentDao amendmentDao;
    private TemplateService templateService;
    private DeltaService deltaService;

    protected void setUp() throws Exception {
        super.setUp();

        registerMocks();

        service = service();

        source = createNamedInstance("Source A", Source.class);

        // Bone Scan on Day 1
        activity0 = setGridId("grid0", createPlannedActivity("Bone Scan", 1));
        activity0.getActivity().setCode("BS");
        activity0.getActivity().setSource(source);

        // New Blood Measure on Day 2
        activity1 = setGridId("grid1", createPlannedActivity("New Blood Measure", 2));
        activity1.getActivity().setCode("NBM");
        activity1.getActivity().setSource(source);

        // New Blood Measure on Day 3
        activity2 = setGridId("grid2", createPlannedActivity("New Blood Measure", 3));
        activity2.getActivity().setCode("NBM");
        activity2.getActivity().setSource(source);

        // Planned Activity placeholder waiting to be resolved to Bone Scan on Day 1
        activity3 = setGridId("grid0", createPlannedActivity("Bone Scan", 1));
        
        // Administer Drugs on Day 4
        activity4 = setGridId("grid4", createPlannedActivity("Administer Drugs", 4));
        activity4.getActivity().setCode("AD");
        activity4.getActivity().setSource(source);

        // Planned Activity placeholder waiting to be resolved to New Blood Measure on Day 2
        activity5 = setGridId("grid1", createPlannedActivity("New Blood Measure", 2));

        period = setGridId("grid10", createNamedInstance("Period A", Period.class));

        study = study();
    }



    public void testResolveExistingActivitiesAndSources() {
        expectResolveExistingActivityAndSource("BS", activity0.getActivity(), "Source A", source);
        expectResolveNewActivityAndSource("NBM", activity1.getActivity(), "Source A", source);
        expectResolveExistingActivityAndSource("NBM", activity1.getActivity(), "Source A", source);
        expectResolveNewActivityAndSource("AD", activity4.getActivity(), "Source A", source);

        replayMocks();

        service.resolveExistingActivitiesAndSources(study);
        verifyMocks();
    }

    public void testResolveChangeChildrenFromPlanTreeNodeTree() {
        expectSaveStudyWithDao();

        // Amendment 0
        expectResolvePeriod();
        expectResolvePlannedActivity("grid0", activity0);

        expectResolvePeriod();
        expectResolvePlannedActivity("grid1", activity1);
        expectAmendStudy();

        // Amendment 1
        expectResolvePeriod();
        expectResolvePlannedActivity("grid2", activity2);

        expectResolvePeriod();
        expectResolvePlannedActivity("grid0", activity0);
        expectAmendStudy();

        // Development Amendment
        expectResolvePeriod();
        expectResolvePlannedActivity("grid4", activity4);

        expectResolvePeriod();
        expectResolvePlannedActivity("grid1", activity1);

        expectSaveStudyWithService();

        replayMocks();

        service.resolveChangeChildrenFromPlanTreeNodeTree(study);
        verifyMocks();
    }

    ////// Helper expect methods

    private void expectResolveExistingActivityAndSource(String activityCode, Activity activity, String sourceCode, Source source) {
        expect(activityDao.getByCodeAndSourceName(activityCode, sourceCode)).andReturn(activity);
        sourceDao.save(source);
        activityDao.save(activity);
    }

    private void expectResolveNewActivityAndSource(String activityCode, Activity activity, String sourceCode, Source source) {
        expect(activityDao.getByCodeAndSourceName(activityCode, sourceCode)).andReturn(null);
        expect(sourceDao.getByName(sourceCode)).andReturn(null);
        sourceDao.save(source);
        activityDao.save(activity);
    }

    private void expectResolvePeriod() {
        expect(daoFinder.findDao(Period.class)).andReturn((DomainObjectDao)periodDao);
        expect(periodDao.getByGridId("grid10")).andReturn(period);
    }

    private void expectResolvePlannedActivity(String activityGridId, PlannedActivity activity) {
        expect(daoFinder.findDao(PlannedActivity.class)).andReturn((DomainObjectDao)plannedActivityDao);
        expect(plannedActivityDao.getByGridId(activityGridId)).andReturn(activity);
    }

    private void expectAmendStudy() {
        amendmentService.amend(study);
    }

    private void expectSaveStudyWithDao() {
        studyDao.save(study);
    }

    private void expectSaveStudyWithService() {
        studyService.save(study);
    }

    ////// Helper Create Methods
    private void registerMocks() {
        studyDao = registerDaoMockFor(StudyDao.class);
        changeDao = registerDaoMockFor(ChangeDao.class);
        daoFinder = registerMockFor(DaoFinder.class);
        epochDao = registerDaoMockFor(EpochDao.class);
        deltaDao = registerDaoMockFor(DeltaDao.class);
        periodDao = registerDaoMockFor(PeriodDao.class);
        sourceDao = registerDaoMockFor(SourceDao.class);
        activityDao = registerDaoMockFor(ActivityDao.class);
        studyService = registerMockFor(StudyService.class);
        amendmentDao = registerDaoMockFor(AmendmentDao.class);
        studySegmentDao = registerDaoMockFor(StudySegmentDao.class);
        amendmentService = registerMockFor(AmendmentService.class);
        plannedActivityDao = registerDaoMockFor(PlannedActivityDao.class);
        templateService = registerMockFor(TemplateService.class);
        deltaService = registerMockFor(DeltaService.class);
    }

    private ImportTemplateService service() {
        ImportTemplateService service = new ImportTemplateService();
        service.setStudyDao(studyDao);
        service.setDeltaDao(deltaDao);
        service.setChangeDao(changeDao);
        service.setDaoFinder(daoFinder);
        service.setSourceDao(sourceDao);
        service.setActivityDao(activityDao);
        service.setAmendmentDao(amendmentDao);
        service.setStudyService(studyService);
        service.setDeltaService(deltaService);
        service.setAmendmentService(amendmentService);
        service.setTemplateService(templateService);
        return service;
    }

    private Study study() {
        Study study = createNamedInstance("Study A", Study.class);
        study.pushAmendment(amendment0());
        study.pushAmendment(amendment1());
        study.setDevelopmentAmendment(developmentAmendment());
        return study;
    }
    private Amendment amendment0() {
        Amendment amendment = Fixtures.createAmendments("Amendment 0");

        Add add0 = Add.create(activity0);
        Delta delta0 = Delta.createDeltaFor(period, add0);

        Add add1 = Add.create(activity1);
        Delta delta1 = Delta.createDeltaFor(period, add1);

        amendment.addDelta(delta0);
        amendment.addDelta(delta1);

        return amendment;
    }

    private Amendment amendment1() {
        Amendment amendment = Fixtures.createAmendments("Amendment 1");

        Add add = Add.create(activity2);
        Delta delta0 = Delta.createDeltaFor(period, add);

        Remove remove = Remove.create(activity3);
        Delta delta1 = Delta.createDeltaFor(period, remove);

        amendment.addDelta(delta0);
        amendment.addDelta(delta1);
        return amendment;
    }

    private Amendment developmentAmendment() {
        Amendment amendment = Fixtures.createAmendments("Development Amendment");

        Add add = Add.create(activity4);
        Delta delta0 = Delta.createDeltaFor(period, add);

        Remove remove = Remove.create(activity5);
        Delta delta1 = Delta.createDeltaFor(period, remove);

        amendment.addDelta(delta0);
        amendment.addDelta(delta1);
        return amendment;
    }
}
