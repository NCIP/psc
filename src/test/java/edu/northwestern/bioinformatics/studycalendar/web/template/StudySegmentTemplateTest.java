package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class StudySegmentTemplateTest extends StudyCalendarTestCase {
    private StudySegment studySegment;
    private StudySegmentTemplate template;

    protected void setUp() throws Exception {
        super.setUp();
        studySegment = new StudySegment();
        Period pA = setId(1, createPeriod("A", 1, 7, 7));
        pA.addPlannedActivity(createPlannedActivity("Sailing", 1));
        Period pB = setId(2, createPeriod("B", 3, 5, 8));
        pB.addPlannedActivity(createPlannedActivity("Skydiving", 1));
        Period pC = setId(3, createPeriod("C", 2, 1, 1));
        pC.addPlannedActivity(createPlannedActivity("Skiing", 1));
        Period pD = setId(4, createPeriod("D", -13, 7, 1));
        pD.addPlannedActivity(createPlannedActivity("Surfing", 1));

        studySegment.addPeriod(pA);
        studySegment.addPeriod(pB);
        studySegment.addPeriod(pC);
        studySegment.addPeriod(pD);

        initTemplate();
    }

    private void initTemplate() {
        template = new StudySegmentTemplate(studySegment);
    }

    public void testMonthCount() throws Exception {
        assertEquals("63 days should be three months", 3, template.getMonths().size());
    }

    public void testPeriodCount() throws Exception {
        StudySegmentTemplate.Month firstMonth = template.getMonths().get(0);
        assertEquals("Wrong number of periods in month", 4, firstMonth.getPeriods().size());
        assertEquals("Wrong number of periods in day",
            4, firstMonth.getDays().get(1).getPeriods().size());
    }
    
    public void testDayCount() throws Exception {
        assertEquals("Wrong number of days in first month",  28, template.getMonths().get(0).getDays().size());
        assertEquals("Wrong number of days in second month", 28, template.getMonths().get(1).getDays().size());
        assertEquals("Wrong number of days in third month",   7, template.getMonths().get(2).getDays().size());
    }

    public void testDayCountInPeriods() throws Exception {
        assertEquals("Wrong number of days in first month",  28, template.getMonths().get(0).getPeriods().get(0).getDays().size());
        assertEquals("Wrong number of days in second month", 28, template.getMonths().get(1).getPeriods().get(0).getDays().size());
        assertEquals("Wrong number of days in third month",   7, template.getMonths().get(2).getPeriods().get(0).getDays().size());
    }

    public void testDayNumbering() throws Exception {
        assertEquals(-13, (int) template.getMonths().get(0).getDays().firstKey());
        assertEquals(15, (int) template.getMonths().get(1).getDays().firstKey());
        assertEquals(43, (int) template.getMonths().get(2).getDays().firstKey());
    }

    public void testDaysEmptiness() throws Exception {
        // index 15 is day 2
        StudySegmentTemplate.DayOfPeriod pAd2 = template.getMonths().get(0).getPeriods().get(0).getDays().get(15);
        StudySegmentTemplate.DayOfPeriod pCd2 = template.getMonths().get(0).getPeriods().get(2).getDays().get(15);
        assertEquals("Malformed test", "2", pAd2.getDay().getNumber().toString());
        assertTrue("Expected day 2 of pA empty", pAd2.isEmpty());
        assertFalse("Expected day 2 of pC not empty", pCd2.isEmpty());
    }

    public void testEventsForDay() throws Exception {
        List<PlannedActivity> actualEvents = template.getMonths().get(0).getDays().get(8).getEvents();
        assertEquals(2, actualEvents.size());
        assertEquals("Sailing", actualEvents.get(0).getActivity().getName());
        assertEquals("Skydiving", actualEvents.get(1).getActivity().getName());
    }

    public void testEventsForNegativeDay() throws Exception {
        List<PlannedActivity> actualEvents = template.getMonths().get(0).getDays().get(-13).getEvents();
        assertEquals(1, actualEvents.size());
        assertEquals("Surfing", actualEvents.get(0).getActivity().getName());
    }

    public void testIsResume() throws Exception {
        // none in first month:
        for (StudySegmentTemplate.MonthOfPeriod period : template.getMonths().get(0).getPeriods()) {
            assertFalse("Period " + period.getName() + " in first month incorrectly flagged", period.isResume());
        }
        // only period B in second month
        StudySegmentTemplate.Month secondMonth = template.getMonths().get(1);
        assertFalse("Expected pA not resume", secondMonth.getPeriods().get(0).isResume());
        assertTrue("Expected pB resume", secondMonth.getPeriods().get(1).isResume());
        assertFalse("Expected pC not resume", secondMonth.getPeriods().get(2).isResume());
        assertFalse("Expected pD not resume", secondMonth.getPeriods().get(3).isResume());
        // none in third month:
        for (StudySegmentTemplate.MonthOfPeriod period : template.getMonths().get(2).getPeriods()) {
            assertFalse("Period " + period.getName() + " in third month incorrectly flagged", period.isResume());
        }
    }

    public void testUsesCycleNumbersIfAppropriate() {
        studySegment.setCycleLength(7);
        initTemplate();

        StudySegmentTemplate.Day firstDayOfSecondMonth
                = template.getMonths().get(1).getDays().values().iterator().next();
        assertEquals("C3D1", firstDayOfSecondMonth.getNumber().toString());
    }
    
    public void testHasEvents() throws Exception {
        assertTrue(template.getHasEvents());
        assertFalse(new StudySegmentTemplate(new StudySegment()).getHasEvents());
    }
}
