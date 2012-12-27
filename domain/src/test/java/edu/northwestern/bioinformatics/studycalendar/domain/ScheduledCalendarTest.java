/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import org.easymock.classextension.EasyMock;

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
}
