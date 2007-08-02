package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class ArmTemplateTest extends StudyCalendarTestCase {
    private Arm arm;
    private ArmTemplate template;

    protected void setUp() throws Exception {
        super.setUp();
        arm = new Arm();
        Period pA = setId(1, createPeriod("A", 1, 7, 7));
        pA.addPlannedEvent(createPlannedEvent("Sailing", 1));
        Period pB = setId(2, createPeriod("B", 3, 4, 8));
        pB.addPlannedEvent(createPlannedEvent("Skydiving", 1));
        Period pC = setId(3, createPeriod("C", 2, 1, 1));
        pC.addPlannedEvent(createPlannedEvent("Skiing", 1));

        arm.addPeriod(pA);
        arm.addPeriod(pB);
        arm.addPeriod(pC);

        template = new ArmTemplate(arm);
    }

    public void testMonthCount() throws Exception {
        assertEquals("49 days should be two months", 2, template.getMonths().size());
    }

    public void testPeriodCount() throws Exception {
        ArmTemplate.Month firstMonth = template.getMonths().get(0);
        assertEquals("Wrong number of periods in month", 3, firstMonth.getPeriods().size());
        assertEquals("Wrong number of periods in day",
            3, firstMonth.getDays().get(1).getPeriods().size());
    }
    
    public void testDayCount() throws Exception {
        assertEquals("Wrong number of days in first month", 28, template.getMonths().get(0).getPeriods().get(0).getDays().size());
        assertEquals("Wrong number of days in second month", 21, template.getMonths().get(1).getPeriods().get(0).getDays().size());
    }

    public void testDaysEmptiness() throws Exception {
        ArmTemplate.DayOfPeriod pAd2 = template.getMonths().get(0).getPeriods().get(0).getDays().get(1);
        ArmTemplate.DayOfPeriod pCd2 = template.getMonths().get(0).getPeriods().get(2).getDays().get(1);
        assertTrue(pAd2.isEmpty());
        assertFalse(pCd2.isEmpty());
    }

    public void testEventsForDay() throws Exception {
        List<PlannedEvent> actualEvents = template.getMonths().get(0).getDays().get(15).getEvents();
        assertEquals(2, actualEvents.size());
        assertEquals("Sailing", actualEvents.get(0).getActivity().getName());
        assertEquals("Skydiving", actualEvents.get(1).getActivity().getName());
    }

    public void testIsResume() throws Exception {
        // none in first month:
        for (ArmTemplate.MonthOfPeriod period : template.getMonths().get(0).getPeriods()) {
            assertFalse("Period " + period.getName() + " in first month incorrectly flagged", period.isResume());
        }
        // only period B in second month
        ArmTemplate.Month secondMonth = template.getMonths().get(1);
        assertFalse(secondMonth.getPeriods().get(0).isResume());
        assertTrue(secondMonth.getPeriods().get(1).isResume());
        assertFalse(secondMonth.getPeriods().get(2).isResume());
    }
}
