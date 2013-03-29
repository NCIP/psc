/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import org.easymock.classextension.EasyMock;

import java.util.Calendar;
import java.util.Arrays;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class ScheduledCalendarTest extends DomainTestCase {
    private ScheduledCalendar scheduledCalendar;

    protected void setUp() throws Exception {
        super.setUp();
        scheduledCalendar = new ScheduledCalendar();
    }

    public void testGetCurrentStudySegment() throws Exception {
        List<ScheduledStudySegment> studySegments = Arrays.asList(
            registerMockFor(ScheduledStudySegment.class),
            registerMockFor(ScheduledStudySegment.class),
            registerMockFor(ScheduledStudySegment.class)
        );
        scheduledCalendar.setScheduledStudySegments(studySegments);

        EasyMock.expect(studySegments.get(0).isComplete()).andReturn(true);
        EasyMock.expect(studySegments.get(1).isComplete()).andReturn(false);

        replayMocks();
        assertSame(studySegments.get(1), scheduledCalendar.getCurrentStudySegment());
        verifyMocks();
    }
    
    public void testGetCurrentStudySegmentWhenAllComplete() throws Exception {
        List<ScheduledStudySegment> studySegments = Arrays.asList(
            registerMockFor(ScheduledStudySegment.class),
            registerMockFor(ScheduledStudySegment.class),
            registerMockFor(ScheduledStudySegment.class)
        );
        scheduledCalendar.setScheduledStudySegments(studySegments);

        EasyMock.expect(studySegments.get(0).isComplete()).andReturn(true);
        EasyMock.expect(studySegments.get(1).isComplete()).andReturn(true);
        EasyMock.expect(studySegments.get(2).isComplete()).andReturn(true);

        replayMocks();
        assertSame(studySegments.get(2), scheduledCalendar.getCurrentStudySegment());
        verifyMocks();
    }

    public void testGetScheduledStudySegmentsFor() throws Exception {
        StudySegment a1 = setId(4, createNamedInstance("A1", StudySegment.class));
        StudySegment a2 = setId(9, createNamedInstance("A2", StudySegment.class));
        StudySegment unused = setId(262, createNamedInstance("Unused", StudySegment.class));
        scheduledCalendar.addStudySegment(createScheduledStudySegment(a1));
        scheduledCalendar.addStudySegment(createScheduledStudySegment(a2));
        scheduledCalendar.addStudySegment(createScheduledStudySegment(a1));

        List<ScheduledStudySegment> forA1 = scheduledCalendar.getScheduledStudySegmentsFor(a1);
        assertEquals("Wrong number for A1", 2, forA1.size());
        assertSame("Wrong 0th for A1", scheduledCalendar.getScheduledStudySegments().get(0), forA1.get(0));
        assertSame("Wrong 1st for A1", scheduledCalendar.getScheduledStudySegments().get(2), forA1.get(1));

        List<ScheduledStudySegment> forA2 = scheduledCalendar.getScheduledStudySegmentsFor(a2);
        assertEquals("Wrong number for A2", 1, forA2.size());
        assertSame("Wrong 0th for A2", scheduledCalendar.getScheduledStudySegments().get(1), forA2.get(0));

        List<ScheduledStudySegment> forUnused = scheduledCalendar.getScheduledStudySegmentsFor(unused);
        assertEquals("Wrong number for unused", 0, forUnused.size());
    }

    public void testGetScheduledActivitiesFor() throws Exception {
        Activity activity0 = createActivity("0", Fixtures.createActivityType("PROCEDURE"));
        Activity activity1 = createActivity("1", Fixtures.createActivityType("INTERVENTION"));

        PlannedActivity pa0 = createPlannedActivity(activity0, 1);
        PlannedActivity pa1 = createPlannedActivity(activity1, 2);
        PlannedActivity unused = createPlannedActivity(activity1, 1);
        ScheduledStudySegment scheduledStudySegment =  new ScheduledStudySegment();
        scheduledStudySegment.addEvent(createScheduledActivity(setId(1,pa0), 2006, Calendar.SEPTEMBER, 20));
        scheduledStudySegment.addEvent(createScheduledActivity(setId(2,pa1), 2006, Calendar.SEPTEMBER, 20));
        scheduledStudySegment.addEvent(createScheduledActivity(setId(3,pa0), 2006, Calendar.SEPTEMBER, 22));
        scheduledStudySegment.addEvent(createScheduledActivity(setId(4,pa0), 2006, Calendar.SEPTEMBER, 25));
        scheduledCalendar.addStudySegment(scheduledStudySegment);

        List<ScheduledActivity> forPA0 = scheduledCalendar.getScheduledActivitiesFor(pa0);
        assertEquals("Wrong number for PA0", 3, forPA0.size());
        assertSame("Wrong 0th for PA0", scheduledCalendar.getCurrentStudySegment().getActivities().get(0), forPA0.get(0));
        assertSame("Wrong 1st for PA0", scheduledCalendar.getCurrentStudySegment().getActivities().get(2), forPA0.get(1));
        assertSame("Wrong 2nd for PA0", scheduledCalendar.getCurrentStudySegment().getActivities().get(3), forPA0.get(2));


        List<ScheduledActivity> forPA1 = scheduledCalendar.getScheduledActivitiesFor(pa1);
        assertEquals("Wrong number for PA1", 1, forPA1.size());
        assertSame("Wrong 0th for PA0", scheduledCalendar.getCurrentStudySegment().getActivities().get(1), forPA1.get(0));

        List<ScheduledActivity> forUnused = scheduledCalendar.getScheduledActivitiesFor(unused);
        assertEquals("Wrong number for unused", 0, forUnused.size());
    }
}
