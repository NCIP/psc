package edu.northwestern.bioinformatics.studycalendar.web;

import edu.nwu.bioinformatics.commons.testing.CoreTestCase;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedSchedule;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;

import java.util.Iterator;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class CalendarTemplateTest extends CoreTestCase {
    private CalendarTemplate template;
    private PlannedSchedule schedule;

    /*
        BASE TEST STRUCTURE:
        schedule
         - E1
           - P0: start 1, 7 days, repeat 3
           - P1: start 4, 7 days, repeat 3
         - E2
           - X
             - P0: start  8, 14 days, repeat 2
             - P1: start 15, 21 days
           - Y
             - P0: start  1, 21 days
             - P1: start 22, 21 days
         - E3
           - P0: start 1, 365 days, repeat 6
    */
    protected void setUp() throws Exception {
        super.setUp();
        schedule = new PlannedSchedule();
        schedule.addEpoch(createEpoch("E1"));
        schedule.addEpoch(createEpoch("E2", "X", "Y"));
        schedule.addEpoch(createEpoch("E3"));

        Arm e1 = schedule.getEpochs().get(0).getArms().get(0);
        e1.addPeriod(createPeriod("1P0", 1, 7, 3));
        e1.addPeriod(createPeriod("1P1", 4, 7, 3));

        Arm e2x = schedule.getEpochs().get(1).getArms().get(0);
        e2x.addPeriod(createPeriod("2xP0",  8, 14, 2));
        e2x.addPeriod(createPeriod("2xP1", 15, 21, 1));

        Arm e2y = schedule.getEpochs().get(1).getArms().get(1);
        e2y.addPeriod(createPeriod("2yP0",  1, 21, 1));
        e2y.addPeriod(createPeriod("2yP1", 22, 21, 1));

        Arm e3 = schedule.getEpochs().get(2).getArms().get(0);
        e3.addPeriod(createPeriod("3P0", 1, 365, 6));

        initTemplate();
    }

    private void initTemplate() {
        template = new CalendarTemplate(schedule);
    }

    public void testName() throws Exception {
        String expectedName = "The Study";
        schedule.setStudy(createNamedInstance(expectedName, Study.class));
        initTemplate();

        assertEquals(expectedName, template.getName());
    }

    public void testEpochs() throws Exception {
        assertEquals(3, template.getEpochs().size());
    }

    public void testWeekCounts() throws Exception {
        assertEquals(4, template.getEpochs().get(0).getWeeks().size());
        assertEquals(6, template.getEpochs().get(1).getWeeks().size());
        assertEquals(313, template.getEpochs().get(2).getWeeks().size());
    }

    public void testArmCounts() throws Exception {
        List<CalendarTemplate.Week> e2_weeks = template.getEpochs().get(1).getWeeks();
        assertEquals(2, e2_weeks.get(0).getArms().size());
        assertEquals(1, e2_weeks.get(5).getArms().size());
    }

    public void testWeekRanges() throws Exception {
        CalendarTemplate.Week e1_3 = template.getEpochs().get(0).getWeeks().get(2);
        assertEquals("Wrong start date for week 3", 15, e1_3.getRange().getMinimumInteger());
        assertEquals("Wrong end date for week 3", 21, e1_3.getRange().getMaximumInteger());
    }

    public void testDaysOfPeriod() throws Exception {
        CalendarTemplate.Day e2x_d20 = template.getEpochs().get(1).getWeeks().get(2).getArms().get(0).getDays().get(5);
        assertEquals("2xP0", e2x_d20.getPeriods().get(0).getName());
        assertEquals("2xP1", e2x_d20.getPeriods().get(1).getName());
        assertEquals(2, e2x_d20.getPeriods().size());
    }

    public void testPlannedEvents() throws Exception {
        Epoch e1 = schedule.getEpochs().get(0);
        Iterator<Period> e1p = e1.getArms().get(0).getPeriods().iterator();
        Period e1p0 = e1p.next();
        Period e1p1 = e1p.next();
        assertFalse(e1p.hasNext());

        e1p0.addPlannedEvent(createPlannedEvent("A0", 6));
        e1p1.addPlannedEvent(createPlannedEvent("A1", 3));
        initTemplate();

        CalendarTemplate.Day e1_d6 = template.getEpochs().get(0).getWeeks().get(0).getArms().get(0).getDays().get(5);
        assertEquals("A0", e1_d6.getPeriods().get(0).getPlannedEvents().get(0).getActivity().getName());
        assertEquals("A1", e1_d6.getPeriods().get(1).getPlannedEvents().get(0).getActivity().getName());
    }
}
