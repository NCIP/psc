package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;

/**
 * @author Rhett Sutphin
 */
public class ScheduledArmDaoTest extends ContextDaoTestCase<ScheduledArmDao> {
    protected String getTestDataFileName() {
        return "testdata/ScheduledCalendarDaoTest.xml";
    }

    public void testGetById() throws Exception {
        ScheduledArm loaded = getDao().getById(-22);
        assertEquals("Wrong arm", -4, (int) loaded.getArm().getId());
        assertEquals("Wrong schedule", -20, (int) loaded.getScheduledCalendar().getId());
    }
}
