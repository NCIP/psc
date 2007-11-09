package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;

/**
 * Created by IntelliJ IDEA.
 * User: nshurupova
 * Date: Nov 9, 2007
 * Time: 12:31:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlannedActivityDaoTest extends ContextDaoTestCase<PlannedActivityDao> {
    public void testGetById() throws Exception {
        PlannedActivity loaded = getDao().getById(-12);

        assertEquals("Wrong id", -12, (int) loaded.getId());
        assertEquals("Wrong day number", new Integer(4), loaded.getDay());
        assertNotNull("Period not loaded", loaded.getPeriod());
        assertEquals("Wrong period", -300L, (long) loaded.getPeriod().getId());
        assertNotNull("Activity not loaded", loaded.getActivity());
        assertEquals("Wrong activity", -200L, (long) loaded.getActivity().getId());
        assertEquals("Wrong condition", "At least 37", loaded.getCondition());
    }

    public void testPeriodBidirectional() throws Exception {
        PlannedActivity loaded = getDao().getById(-12);
        assertTrue(loaded.getPeriod().getPlannedEvents().contains(loaded));
    }

    public void testSaveDetached() throws Exception {
        Integer id;
        {
            PlannedActivity plannedEvent = new PlannedActivity();
            plannedEvent.setDay(5);
            plannedEvent.setActivity(getDao().getById(-12).getActivity());
            getDao().save(plannedEvent);
            assertNotNull("not saved", plannedEvent.getId());
            id = plannedEvent.getId();
        }

        interruptSession();

        PlannedActivity loaded = getDao().getById(id);
        assertNotNull("Could not reload", loaded);
        assertEquals("Wrong event loaded", 5, (int) loaded.getDay());
    }
}
