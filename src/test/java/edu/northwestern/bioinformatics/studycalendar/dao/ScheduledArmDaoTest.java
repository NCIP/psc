package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class ScheduledArmDaoTest extends ContextDaoTestCase<ScheduledArmDao> {
    @Override
    protected String getTestDataFileName() {
        return "testdata/ScheduledCalendarDaoTest.xml";
    }

    public void testGetById() throws Exception {
        ScheduledArm loaded = getDao().getById(-22);
        assertEquals("Wrong arm", -4, (int) loaded.getArm().getId());
        assertEquals("Wrong schedule", -20, (int) loaded.getScheduledCalendar().getId());
        assertEquals("Wrong start day", 2, (int) loaded.getStartDay());
        StudyCalendarTestCase.assertDayOfDate("Wrong start date", 2006, Calendar.OCTOBER, 27, loaded.getStartDate());
    }
}
