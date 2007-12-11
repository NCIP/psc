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

    public void testGetById() throws Exception {
        Activity activity = dao.getById(-100);
        assertNotNull("Screening Activity not found", activity);
        assertEquals("Wrong name", "Screening Activity", activity.getName());
        assertEquals("Wrong description", "Description of screening activity", activity.getDescription());
        assertEquals("Wrong type", ActivityType.INTERVENTION, activity.getType());
        assertEquals("Wrong source", "ICD9", activity.getSource().getName());
        assertEquals("Wrong code", "SA", activity.getCode());
    }

    public void testSaveNewActivity() throws Exception {
        Integer savedId;
        {
            Activity activity = new Activity();
            activity.setName("Give drug");
            activity.setDescription("Administer aspirin");
            activity.setType(ActivityType.PROCEDURE);
            activity.setCode("AA");
            dao.save(activity);
            savedId = activity.getId();
            assertNotNull("The saved activity didn't get an id", savedId);
        }

        interruptSession();

        {
            Activity loaded = dao.getById(savedId);
            assertNotNull("Could not reload activity with id " + savedId, loaded);
            assertEquals("Wrong code", "AA", loaded.getCode());
            assertEquals("Wrong name", "Give drug", loaded.getName());
            assertSame("Wrong name for activity type", ActivityType.PROCEDURE, loaded.getType());
        }
    }

    public void testGetAll() throws Exception {
        List<Activity> actual = dao.getAll();
        assertEquals(5, actual.size());
    }

    public void testGetAllSortOrder() throws Exception {
        List<Activity> actual = dao.getAll();
        assertEquals("Wrong order", -96, (int) actual.get(0).getId());
        assertEquals("Wrong order", -98, (int) actual.get(1).getId());
        assertEquals("Wrong order", -100, (int) actual.get(2).getId());
        assertEquals("Wrong order", "Administer Drug A", actual.get(3).getName());
        assertEquals("Wrong order", "Administer Drug Z", actual.get(4).getName());
    }

}