package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedEventDao;
import edu.northwestern.bioinformatics.studycalendar.service.ParticipantService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class AddPlannedEventMutatorTest extends StudyCalendarTestCase {
    private static final int PLANNED_EVENT_ID = 21;

    private AddPlannedEventMutator mutator;

    private Amendment amendment;
    private Add add;

    private Arm arm;
    private Period period;
    private PlannedEvent plannedEvent;
    private ScheduledCalendar scheduledCalendar;

    private PlannedEventDao plannedEventDao;
    private ParticipantService participantService;
    private TemplateService templateService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        arm = setId(45, new Arm());
        period = setId(81, createPeriod("P1", 4, 17, 8));

        plannedEvent = setId(21, createPlannedEvent("Swim", 8));
        add = Add.create(plannedEvent, 4);
        amendment = createAmendments("Oops");
        amendment.addDelta(Delta.createDeltaFor(period, add));

        scheduledCalendar = new ScheduledCalendar();

        plannedEventDao = registerDaoMockFor(PlannedEventDao.class);
        participantService = registerMockFor(ParticipantService.class);
        templateService = registerMockFor(TemplateService.class);

        mutator = new AddPlannedEventMutator(
            add, plannedEventDao, participantService, templateService);

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

        participantService.schedulePlannedEvent(plannedEvent, period, amendment, scheduledCalendar.getScheduledArms().get(1));

        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
    }

    public void testApplyToMultipleRelevantScheduledArms() throws Exception {
        scheduledCalendar.addArm(createScheduledArm(arm));
        scheduledCalendar.addArm(createScheduledArm(createNamedInstance("Some other arm", Arm.class)));
        scheduledCalendar.addArm(createScheduledArm(arm));

        participantService.schedulePlannedEvent(plannedEvent, period, amendment, scheduledCalendar.getScheduledArms().get(0));
        participantService.schedulePlannedEvent(plannedEvent, period, amendment, scheduledCalendar.getScheduledArms().get(2));

        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
    }
}
