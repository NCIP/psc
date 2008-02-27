package edu.northwestern.bioinformatics.studycalendar.domain;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Moses Hohman
 */
public class StudySegmentTest extends StudyCalendarTestCase {
    private StudySegment studySegment = new StudySegment();

    public void testAddPeriod() {
        Period period = new Period();
        studySegment.addPeriod(period);
        assertEquals("wrong number of periods", 1, studySegment.getPeriods().size());
        assertSame("wrong period present", period, studySegment.getPeriods().iterator().next());
        assertEquals("bidirectional relationship not maintained", studySegment, period.getStudySegment());
    }

    public void testLengthSimple() throws Exception {
        Period single = createPeriod("", 3, Duration.Unit.day, 15, 3);
        studySegment.addPeriod(single);

        assertDayRange(3, 47, studySegment.getDayRange());
        assertEquals(45, studySegment.getLengthInDays());
    }
    
    public void testLengthWhenOverlapping() throws Exception {
        Period zero = createPeriod("", 1, Duration.Unit.day, 30, 1);
        Period one = createPeriod("", 17, Duration.Unit.day, 15, 1);
        studySegment.addPeriod(zero);
        studySegment.addPeriod(one);

        assertDayRange(1, 31, studySegment.getDayRange());
        assertEquals(31, studySegment.getLengthInDays());
    }

    public void testLengthNegative() throws Exception {
        Period single = createPeriod("", -28, 15, 1);
        studySegment.addPeriod(single);

        assertDayRange(-28, -14, studySegment.getDayRange());
        assertEquals(15, studySegment.getLengthInDays());
    }

    public void testLengthNegativeAndPositiveWithGap() throws Exception {
        studySegment.addPeriod(createPeriod("dc", -28, 14, 1));
        studySegment.addPeriod(createPeriod("dc", 10, 8, 2));

        assertDayRange(-28, 25, studySegment.getDayRange());
        assertEquals(54, studySegment.getLengthInDays());
    }

    public void testQualifiedNameZeroStudySegmentEpoch() throws Exception {
        assertEquals("Epoch", Epoch.create("Epoch").getStudySegments().get(0).getQualifiedName());
    }
    
    public void testQualifiedName() throws Exception {
        Epoch epoch = Epoch.create("Epoch", "A", "B");
        assertEquals("Epoch: A", epoch.getStudySegments().get(0).getQualifiedName());
        assertEquals("Epoch: B", epoch.getStudySegments().get(1).getQualifiedName());
    }

    public void testDayRangeWithNoPeriods() throws Exception {
        assertEquals(0, new StudySegment().getDayRange().getDayCount());
    }
}
