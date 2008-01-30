package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
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

    protected void setUp() throws Exception {
        super.setUp();

        sourceDao = registerDaoMockFor(SourceDao.class);
        activityDao = registerDaoMockFor(ActivityDao.class);

        service = new ImportTemplateService();
        service.setActivityDao(activityDao);
        service.setSourceDao(sourceDao);

        source = createNamedInstance("Source A", Source.class);

        activity0 = Fixtures.createPlannedActivity("Bone Scan", 1);
        activity0.getActivity().setCode("BS");
        activity0.getActivity().setSource(source);

        activity1 = Fixtures.createPlannedActivity("New Blood Measure", 2);
        activity1.getActivity().setCode("NBM");
        activity1.getActivity().setSource(source);

        activity2 = Fixtures.createPlannedActivity("New Blood Measure", 3);
        activity2.getActivity().setCode("NBM");
        activity2.getActivity().setSource(source);

        Period period = createNamedInstance("Period A", Period.class);

        Add add0 = Add.create(activity0);
        Delta delta0 = Delta.createDeltaFor(period, add0);

        Add add1 = Add.create(activity1);
        Delta delta1 = Delta.createDeltaFor(period, add1);

        Add add2 = Add.create(activity2);
        Delta delta2 = Delta.createDeltaFor(period, add2);

        Amendment amendment1 = Fixtures.createAmendments("Amendment 0", "Amendment1");
        amendment1.addDelta(delta0);
        amendment1.addDelta(delta1);

        Amendment amendment0 = amendment1.getPreviousAmendment();
        amendment0.addDelta(delta2);

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
}
