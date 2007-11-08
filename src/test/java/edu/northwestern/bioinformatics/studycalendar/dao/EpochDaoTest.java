package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;

/**
 * @author Rhett Sutphin
 */
public class EpochDaoTest extends ContextDaoTestCase<EpochDao> {

    public void testGetById() throws Exception {
        Epoch loaded = getDao().getById(-1);
        assertEquals("Wrong name", "Treatment", loaded.getName());
        assertEquals("Wrong planned calendar", -2, (int) loaded.getPlannedCalendar().getId());
        assertEquals("Wrong number of arms", 3, loaded.getArms().size());
        assertEquals("Wrong 0th arm", "A", loaded.getArms().get(0).getName());
        assertEquals("Wrong 1st arm", "B", loaded.getArms().get(1).getName());
        assertEquals("Wrong 2nd arm", "C", loaded.getArms().get(2).getName());
    }

    public void testSaveDetached() throws Exception {
        Integer id;
        {
            Epoch epoch = new Epoch();
            epoch.setName("J");
            getDao().save(epoch);
            assertNotNull("not saved", epoch.getId());
            id = epoch.getId();
        }

        interruptSession();

        Epoch loaded = getDao().getById(id);
        assertNotNull("Could not reload", loaded);
        assertEquals("Wrong epoch loaded", "J", loaded.getName());
    }
}
