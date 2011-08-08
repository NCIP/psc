package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityProperty;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;

import java.util.List;

/**
 * @author Jaron Sampson
 */
public class ActivityDaoTest extends DaoTestCase {
    private ActivityDao dao = (ActivityDao) getApplicationContext().getBean("activityDao");
    private ActivityTypeDao activityTypeDao = (ActivityTypeDao) getApplicationContext().getBean("activityTypeDao");
    private Source icd9, icd11;

    public void setUp() throws Exception {
        super.setUp();
        icd9 = ((SourceDao) getApplicationContext().getBean("sourceDao")).getById(-200);
        icd11 = ((SourceDao) getApplicationContext().getBean("sourceDao")).getById(-201);
    }

    @Override
    protected void tearDown() throws Exception {
        getJdbcTemplate().execute("delete from activity_properties");
        super.tearDown();
    }

    public void testGetById() throws Exception {
        Activity activity = dao.getById(-100);
        assertNotNull("Screening Activity not found", activity);
        assertEquals("Wrong name", "Screening Activity", activity.getName());
        assertEquals("Wrong description", "Description of screening activity", activity.getDescription());
        assertEquals("Wrong type", Fixtures.createNamedInstance("LAB_TEST", ActivityType.class).getName(), activity.getType().getName());
        assertEquals("Wrong source", "ICD9", activity.getSource().getName());
        assertEquals("Wrong code", "SA", activity.getCode());
    }

    public void testGetByCodeAndSourceName() throws Exception {
        Activity activity = dao.getByCodeAndSourceName("CS", "ICD9");
        assertEquals("Wrong id", -98, (int) activity.getId());
    }

    public void testGetByUniqueKey() throws Exception {
        Activity activity = dao.getByUniqueKey("ICD9|CS");
        assertEquals("Wrong activity", -98, (int) activity.getId());
    }

    public void testSaveNewActivity() throws Exception {
        Integer savedId;
        {
            Activity activity = new Activity();
            activity.setName("Give drug");
            activity.setDescription("Administer aspirin");
            activity.setType(activityTypeDao.getByName("PROCEDURE"));
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
            assertEquals("Wrong name for activity type", Fixtures.createNamedInstance("PROCEDURE", ActivityType.class).getName(),
                    loaded.getType().getName());
        }
    }

    public void testSaveNewActivityWithProperties() throws Exception {
        Integer savedId;
        {
            Activity a = new Activity();
            a.setName("Give drug");
            a.setType(activityTypeDao.getByName("PROCEDURE"));
            a.setCode("AA");
            ActivityProperty p = new ActivityProperty();
            p.setName("0.id");
            p.setNamespace("URI");

            a.addProperty(p);
            dao.save(a);
            savedId = a.getId();
            assertNotNull("The saved activity didn't get an id", savedId);
        }
        interruptSession();
        {
            Activity loaded = dao.getById(savedId);
            assertEquals("Wrong number of properties", 1, loaded.getProperties().size());
            assertEquals("Wrong property", "0.id", loaded.getProperties().get(0).getName());
        }
    }

    public void testGetAll() throws Exception {
        List<Activity> actual = dao.getAll();
        assertEquals(7, actual.size());
    }

    public void testGetActivitiesBySearchText() throws Exception {
        List<Activity> actual = dao.getActivitiesBySearchText("A");
        assertEquals("Wrong activities: " + actual, 6, actual.size());
    }

    public void testGetActivitiesBySearchTextWithType() throws Exception {
        List<Activity> actual = dao.getActivitiesBySearchText("A", activityTypeDao.getByName("PROCEDURE"), null);
        assertEquals("Wrong activities: " + actual, 2, actual.size());
    }

    public void testGetActivitiesBySearchTextWithSource() throws Exception {
        List<Activity> actual = dao.getActivitiesBySearchText("A", null, icd9);
        assertEquals("Wrong activities: " + actual, 3, actual.size());
    }

    public void testGetActivitiesBySearchTextLooksAtCode() throws Exception {
        List<Activity> actual = dao.getActivitiesBySearchText("ADZ");
        assertEquals("Wrong activities: " + actual, 1, actual.size());
        assertEquals("Wrong activity", -99, (int) actual.get(0).getId());
    }

    public void testGetActivitiesBySearchTextLooksAtName() throws Exception {
        List<Activity> actual = dao.getActivitiesBySearchText("Drug A");
        assertEquals("Wrong activities: " + actual, 1, actual.size());
        assertEquals("Wrong activity", -97, (int) actual.get(0).getId());
    }

    public void testGetActivitiesBySearchTextWithNoText() throws Exception {
        List<Activity> actual = dao.getActivitiesBySearchText(null, null, icd11);
        assertEquals("Wrong activities: " + actual, 1, actual.size());
        assertEquals("Wrong activity", -99, (int) actual.get(0).getId());
    }

    public void testGetActivitiesBySearchTextWithLimit() throws Exception {
        List<Activity> actual = dao.getActivitiesBySearchText("A", null, null, 3, null, null, null);
        assertEquals("Wrong activities: " + actual, 3, actual.size());
    }

    public void testGetActivitiesBySearchTextWithOffset() throws Exception {
        List<Activity> actual = dao.getActivitiesBySearchText("A", null, null, null, 1, null, null);
        assertEquals("Wrong activity", "X-ray: Colon", actual.get(0).getName());
    }

    public void testGetAllSortOrder() throws Exception {
        List<Activity> actual = dao.getAll();
        assertEquals("Wrong order", "PSA", actual.get(0).getName());
        assertEquals("Wrong order", "X-ray: Colon", actual.get(1).getName());
        assertEquals("Wrong order", -96, (int) actual.get(2).getId());
        assertEquals("Wrong order", -98, (int) actual.get(3).getId());
        assertEquals("Wrong order", -100, (int) actual.get(4).getId());
        assertEquals("Wrong order", "Administer Drug A", actual.get(5).getName());
        assertEquals("Wrong order", "Administer Drug Z", actual.get(6).getName());
    }

    public void testGetAllWithLimit() {
        List<Activity> actual = dao.getAllWithLimitAndOffset(2, 0);
        assertEquals("Wrong size", 2, actual.size());
        assertEquals("Wrong order", "PSA", actual.get(0).getName());
        assertEquals("Wrong order", "X-ray: Colon", actual.get(1).getName());
    }

    public void testGetAllWithLimitSortOrder() {
        List<Activity> actual = dao.getAllWithLimitAndOffset(8, 0);
        assertEquals("Wrong order", "PSA", actual.get(0).getName());
        assertEquals("Wrong order", "X-ray: Colon", actual.get(1).getName());
        assertEquals("Wrong order", -96, (int) actual.get(2).getId());
        assertEquals("Wrong order", -98, (int) actual.get(3).getId());
        assertEquals("Wrong order", -100, (int) actual.get(4).getId());
        assertEquals("Wrong order", "Administer Drug A", actual.get(5).getName());
        assertEquals("Wrong order", "Administer Drug Z", actual.get(6).getName());
    }

    public void testCount() {
         assertEquals("Wrong size", 7, (int) dao.getCount());
    }

    public void testGetAllWithLimitAndOffset() {
        List<Activity> actual = dao.getAllWithLimitAndOffset(2, 1);
        assertEquals("Wrong size", 2, actual.size());
        assertEquals("Wrong order", "X-ray: Colon", actual.get(0).getName());
        assertEquals("Wrong order", "Administer Drug L", actual.get(1).getName());
    }

    public void testGetAllWithSortActivityNameAsc() {
        List<Activity> actual = dao.getActivitiesBySearchText(null, null, null, null, null, ActivityDao.ActivitySearchCriteria.ACTIVITY_NAME, null);
        assertEquals("Wrong order", "Administer Drug A", actual.get(0).getName());
    }

    public void testGetAllWithSortActivityNameDesc() {
        List<Activity> actual = dao.getActivitiesBySearchText(null, null, null, null, null, ActivityDao.ActivitySearchCriteria.ACTIVITY_NAME, "desc");
        assertEquals("Wrong order", "X-ray: Colon", actual.get(0).getName());
    }

    public void testGetAllWithSortActivityTypeDesc() {
        List<Activity> actual = dao.getActivitiesBySearchText(null, null, null, null, null, ActivityDao.ActivitySearchCriteria.ACTIVITY_TYPE, null);
        assertEquals("Wrong order", "DISEASE_MEASURE", actual.get(0).getType().getName());
    }
}