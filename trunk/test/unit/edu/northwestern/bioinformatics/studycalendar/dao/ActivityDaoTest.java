package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;

import java.util.List;

/**
 * @author Jaron Sampson
 */
public class ActivityDaoTest extends DaoTestCase {
    private ActivityDao dao = (ActivityDao) getApplicationContext().getBean("activityDao");
    private ActivityTypeDao activityTypeDao = (ActivityTypeDao) getApplicationContext().getBean("activityTypeDao");

    public void testGetById() throws Exception {
        Activity activity = dao.getById(100);
        assertNotNull("Screening Activity not found", activity);
        assertEquals("Wrong name", "Screening Activity", activity.getName());
        assertEquals("Wrong description", "Decription of screening activity.", activity.getDescription());
    }

    public void testGetActivityTypeName() throws Exception {
        Activity activity = dao.getById(100);
        assertEquals("Wrong activity type name", "screening", activity.getType().getName());
    }

    
	public void testSaveNewActivity() throws Exception {
        Integer savedId;
        {
            Activity activity = new Activity();
            activity.setName("Give drug");
            activity.setDescription("Administer aspirin");
            activity.setType(activityTypeDao.getById(5));
            dao.save(activity);
            savedId = activity.getId();
            assertNotNull("The saved activity didn't get an id", savedId);
        }

        interruptSession();

        {
            Activity loaded = dao.getById(savedId);
            assertNotNull("Could not reload activity with id " + savedId, loaded);
            assertEquals("Wrong name", "Give drug", loaded.getName());
            assertEquals("Wrong name for activity type", "prevention", loaded.getType().getName());
        }
    }
}