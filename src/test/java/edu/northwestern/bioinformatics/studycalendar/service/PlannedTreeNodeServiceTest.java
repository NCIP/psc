package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.delta.MemoryOnlyMutatorFactory;
import edu.northwestern.bioinformatics.studycalendar.service.delta.MutatorFactory;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.EasyMock.expect;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Saurabh Agrawal
 */
public class PlannedTreeNodeServiceTest extends StudyCalendarTestCase

{
    private Study study;
    private PlannedCalendar calendar;
    private Epoch e1;
    private Epoch e2;
    private StudySegment e1a0;

    private DeltaService deltaService;
    private TemplateService mockTemplateService;

    private DeltaDao deltaDao;
    private ChangeDao changeDao;
    private EpochDao epochDao;
    private StudySegmentDao studySegmentDao;

    private PeriodDao periodDao;
    private PlannedActivityDao plannedActivityDao;
    private Period period, revisedPeriod;
    private List<Activity> activities = new LinkedList<Activity>();

    private PlannedCalendar plannedCalendar;

    Amendment developmentAmendment;

    private PlanTreeNodeService planTreeNodeService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        planTreeNodeService = new PlanTreeNodeService();


        study = setGridId("STUDY-GRID", setId(300, createBasicTemplate()));


        changeDao = registerDaoMockFor(ChangeDao.class);
        deltaDao = registerDaoMockFor(DeltaDao.class);
        epochDao = registerDaoMockFor(EpochDao.class);
        studySegmentDao = registerDaoMockFor(StudySegmentDao.class);
        expect(epochDao.getById(2)).andReturn(e2).anyTimes();
        expect(studySegmentDao.getById(10)).andReturn(e1a0).anyTimes();

        StaticDaoFinder daoFinder = new StaticDaoFinder(epochDao, studySegmentDao);
        MutatorFactory mutatorFactory = new MutatorFactory();
        mutatorFactory.setDaoFinder(daoFinder);
        TestingTemplateService templateService = new TestingTemplateService();
        templateService.setDaoFinder(daoFinder);

        deltaService = new DeltaService();
        deltaService.setMutatorFactory(mutatorFactory);
        deltaService.setTemplateService(templateService);
        deltaService.setChangeDao(changeDao);
        deltaService.setDeltaDao(deltaDao);
        deltaService.setDaoFinder(daoFinder);

        mockTemplateService = registerMockFor(TemplateService.class);
        period = createPeriod("7th", 10, 8, 4);
        period.setParent(e1a0);
        period.setId(1);
        PlannedActivity newEvent = Fixtures.createPlannedActivity("activity 1", 4);
        newEvent.setId(1);
        PlannedActivity anotherEvent = Fixtures.createPlannedActivity("activity 1", 5);
        anotherEvent.setId(2);
        period.addPlannedActivity(newEvent);
        period.addPlannedActivity(anotherEvent);

        revisedPeriod = (Period) period.transientClone();

        planTreeNodeService.setDeltaService(deltaService);


    }


    public void testCopyPeriodFromReleasedTemplate() throws Exception {
        plannedCalendar = new PlannedCalendar();
        plannedCalendar.setStudy(study);
        plannedCalendar.getStudy().setDevelopmentAmendment(developmentAmendment);
        deltaService.setMutatorFactory(new MemoryOnlyMutatorFactory());
        deltaService.setTemplateService(new TestingTemplateService());

        replayMocks();

        Period copiedPeriod = planTreeNodeService.copy(period, false);
        verify(copiedPeriod, period);
        verifyMocks();
    }

    private void verify(final Period copiedPeriod, final Period period) {
        assertNotNull(period);
        assertNotNull(copiedPeriod);


        assertNull(copiedPeriod.getId());
        assertNotNull(period.getId());
        assertEquals(period.getName(), copiedPeriod.getName());
        assertEquals(period.getDuration(), copiedPeriod.getDuration());
        assertEquals(period.getStartDay(), copiedPeriod.getStartDay());
        assertEquals(period.getRepetitions(), copiedPeriod.getRepetitions());
        assertEquals(period.getChildren().size(), copiedPeriod.getChildren().size());

        assertNotNull(copiedPeriod.getChildren().get(0).getActivity());
        assertNotNull(period.getChildren().get(0).getId());
        assertNotNull(period.getChildren().get(1).getId());

        assertNull(copiedPeriod.getChildren().get(0).getId());
        assertNull(copiedPeriod.getChildren().get(1).getId());

        assertEquals("activity 1", copiedPeriod.getChildren().get(0).getActivity().getName());
        assertEqualArrays(new Integer[]{4, 5}, new Integer[]{copiedPeriod.getChildren().get(0).getDay(), copiedPeriod.getChildren().get(1).getDay()});

        //verify that its a deep copy

        period.getDuration().setUnit(Duration.Unit.quarter);
        period.getDuration().setQuantity(12);

        assertEquals(Duration.Unit.day, copiedPeriod.getDuration().getUnit());


    }

}

