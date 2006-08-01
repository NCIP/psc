package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;

import java.util.List;

/**
 * @author Jaron Sampson
 */
public class ActivityTypeDaoTest extends DaoTestCase {
    private ActivityTypeDao dao = (ActivityTypeDao) getApplicationContext().getBean("activityTypeDao");

    public void testGetById() {
        ActivityType type = dao.getById(-6);
        assertNotNull("activity type not found", type);
        assertEquals("wrong name", "early detection", type.getName());
    }

/* Don't need this yet
    public void testSaveActivityType() throws Exception {
        Integer savedId;
        {
        	ActivityType type = new ActivityType();
            type.setName("Test activity type");
            dao.save(type);
            savedId = type.getId();
            assertNotNull("The saved activity type didn't get an id", savedId);
        }

        interruptSession();

        {
        	ActivityType loaded = dao.getById(savedId);
            assertNotNull("Could not reload activity type with id " + savedId, loaded);
            assertEquals("Wrong name", "Test activity type", loaded.getName());
        }
    }
*/
    
    public void testGetAll() throws Exception {
        List<ActivityType> actual = dao.getAll();
        assertEquals(2, actual.size());
        assertEquals("Wrong order", -7, (int) actual.get(0).getId());
        assertEquals("Wrong order", "early detection", actual.get(1).getName());
    }

}
