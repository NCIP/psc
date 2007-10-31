package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.PlannedEventDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class RemovePlannedEventMutatorTest extends StudyCalendarTestCase {
    private RemovePlannedEventMutator mutator;

    private Remove remove;
    private Delta<?> delta;
    private Amendment amendment;

    private PlannedEvent pe0, pe1, pe2;
    ScheduledCalendar scheduledCalendar;
    ScheduledArm scheduledArm;

    private PlannedEventDao plannedEventDao;
    private ScheduledEvent pe0se0, pe1se0, pe1se1, pe2se0, pe0se1;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        pe0 = createPlannedEvent("E0", 2);
        pe1 = createPlannedEvent("E1", 8);
        pe2 = createPlannedEvent("E2", 4);

        remove = Remove.create(pe1);
        delta = Delta.createDeltaFor(new Period(), remove);
        amendment = createAmendments("Oops");
        amendment.setDate(DateTools.createDate(1922, Calendar.SEPTEMBER, 1));
        amendment.addDelta(delta);

        scheduledCalendar = new ScheduledCalendar();
        scheduledArm = new ScheduledArm();
        scheduledArm.addEvent(pe0se0 = createUnschedulableMockEvent(pe0));
        scheduledArm.addEvent(pe1se0 = createUnschedulableMockEvent(pe1));
        scheduledArm.addEvent(pe1se1 = createUnschedulableMockEvent(pe1));
        scheduledArm.addEvent(pe2se0 = createUnschedulableMockEvent(pe2));
        scheduledArm.addEvent(pe0se1 = createUnschedulableMockEvent(pe0));

        plannedEventDao = registerDaoMockFor(PlannedEventDao.class);
        mutator = new RemovePlannedEventMutator(remove, plannedEventDao);
    }

    public void testAppliesToLiveSchedules() throws Exception {
        assertTrue(mutator.appliesToExistingSchedules());
    }

    public void testOnlyApplicableScheduledEventsUnscheduled() throws Exception {
        scheduledCalendar.addArm(scheduledArm);

        String expectedMessage = "Removed in revision 09/01/1922 (Oops)";
        pe1se0.unscheduleIfOutstanding(expectedMessage);
        pe1se1.unscheduleIfOutstanding(expectedMessage);

        replayMocks();
        mutator.apply(scheduledCalendar);
        verifyMocks();
    }

    private ScheduledEvent createUnschedulableMockEvent(PlannedEvent event) throws NoSuchMethodException {
        ScheduledEvent semimock = registerMockFor(ScheduledEvent.class,
            ScheduledEvent.class.getMethod("unscheduleIfOutstanding", String.class));
        semimock.setPlannedEvent(event);
        return semimock;
    }
}
