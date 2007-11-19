package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createPeriod;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createAmendments;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createScheduledArm;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class AddPlannedActivityMutatorTest extends StudyCalendarTestCase {
    private static final int PLANNED_ACTIVITY_ID = 21;

    private AddPlannedActivityMutator mutator;

    private Amendment amendment;
    private Add add;

    private Arm arm;
    private Period period;
    private PlannedActivity plannedActivity;
    private ScheduledCalendar scheduledCalendar;

    private PlannedActivityDao plannedActivityDao;
    private SubjectService subjectService;
    private TemplateService templateService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        arm = setId(45, new Arm());
        period = setId(81, createPeriod("P1", 4, 17, 8));

        plannedActivity = setId(21, Fixtures.createPlannedActivity("Swim", 8));
        add = Add.create(plannedActivity, 4);
        amendment = createAmendments("Oops");
        amendment.addDelta(Delta.createDeltaFor(period, add));

        scheduledCalendar = new ScheduledCalendar();

        plannedActivityDao = registerDaoMockFor(PlannedActivityDao.class);
        subjectService = registerMockFor(SubjectService.class);
        templateService = registerMockFor(TemplateService.class);

        mutator = new AddPlannedActivityMutator(
            add, plannedActivityDao, subjectService, templateService);

        expect(templateService.findParent(period)).andReturn(arm);
    }

    public void testAppliesToSchedules() throws Exception {
        assertTrue(mutator.appliesToExistingSchedules());
    }

    public void testApplyWhenNoRelevantScheduledArms() throws Exception {
        scheduledCalendar.addArm(createScheduledArm(createNamedInstance("Some other arm", Arm.class)));

        // expect nothing to happen

        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
    }

    public void testApplyToOneRelevantScheduledArm() throws Exception {
        scheduledCalendar.addArm(createScheduledArm(createNamedInstance("Some other arm", Arm.class)));
        scheduledCalendar.addArm(createScheduledArm(arm));

        subjectService.schedulePlannedActivity(plannedActivity, period, amendment, scheduledCalendar.getScheduledArms().get(1));

        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
    }

    public void testApplyToMultipleRelevantScheduledArms() throws Exception {
        scheduledCalendar.addArm(createScheduledArm(arm));
        scheduledCalendar.addArm(createScheduledArm(createNamedInstance("Some other arm", Arm.class)));
        scheduledCalendar.addArm(createScheduledArm(arm));

        subjectService.schedulePlannedActivity(plannedActivity, period, amendment, scheduledCalendar.getScheduledArms().get(0));
        subjectService.schedulePlannedActivity(plannedActivity, period, amendment, scheduledCalendar.getScheduledArms().get(2));

        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
    }
}

