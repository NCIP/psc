package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createScheduledEvent;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Occurred;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Canceled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Scheduled;
import edu.nwu.bioinformatics.commons.DateUtils;

import java.util.List;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Collection;

import org.easymock.classextension.EasyMock;

/**
 * @author Rhett Sutphin
 */
public class ScheduledCalendarTest extends StudyCalendarTestCase {
    private ScheduledCalendar scheduledCalendar;

    protected void setUp() throws Exception {
        super.setUp();
        scheduledCalendar = new ScheduledCalendar();
    }

    public void testGetCurrentArm() throws Exception {
        List<ScheduledArm> arms = Arrays.asList(
            registerMockFor(ScheduledArm.class),
            registerMockFor(ScheduledArm.class),
            registerMockFor(ScheduledArm.class)
        );
        scheduledCalendar.setScheduledArms(arms);

        EasyMock.expect(arms.get(0).isComplete()).andReturn(true);
        EasyMock.expect(arms.get(1).isComplete()).andReturn(false);

        replayMocks();
        assertSame(arms.get(1), scheduledCalendar.getCurrentArm());
        verifyMocks();
    }
    
    public void testGetCurrentArmWhenAllComplete() throws Exception {
        List<ScheduledArm> arms = Arrays.asList(
            registerMockFor(ScheduledArm.class),
            registerMockFor(ScheduledArm.class),
            registerMockFor(ScheduledArm.class)
        );
        scheduledCalendar.setScheduledArms(arms);

        EasyMock.expect(arms.get(0).isComplete()).andReturn(true);
        EasyMock.expect(arms.get(1).isComplete()).andReturn(true);
        EasyMock.expect(arms.get(2).isComplete()).andReturn(true);

        replayMocks();
        assertSame(arms.get(2), scheduledCalendar.getCurrentArm());
        verifyMocks();
    }

    public void testGetScheduledArmsFor() throws Exception {
        Arm a1 = setId(4, createNamedInstance("A1", Arm.class));
        Arm a2 = setId(9, createNamedInstance("A2", Arm.class));
        Arm unused = setId(262, createNamedInstance("Unused", Arm.class));
        scheduledCalendar.addArm(createScheduledArm(a1));
        scheduledCalendar.addArm(createScheduledArm(a2));
        scheduledCalendar.addArm(createScheduledArm(a1));

        List<ScheduledArm> forA1 = scheduledCalendar.getScheduledArmsFor(a1);
        assertEquals("Wrong number for A1", 2, forA1.size());
        assertSame("Wrong 0th for A1", scheduledCalendar.getScheduledArms().get(0), forA1.get(0));
        assertSame("Wrong 1st for A1", scheduledCalendar.getScheduledArms().get(2), forA1.get(1));

        List<ScheduledArm> forA2 = scheduledCalendar.getScheduledArmsFor(a2);
        assertEquals("Wrong number for A2", 1, forA2.size());
        assertSame("Wrong 0th for A2", scheduledCalendar.getScheduledArms().get(1), forA2.get(0));

        List<ScheduledArm> forUnused = scheduledCalendar.getScheduledArmsFor(unused);
        assertEquals("Wrong number for unused", 0, forUnused.size());
    }
}
