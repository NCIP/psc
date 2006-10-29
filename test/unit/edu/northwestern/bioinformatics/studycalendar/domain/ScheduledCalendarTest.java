package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.List;
import java.util.Arrays;

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
}
