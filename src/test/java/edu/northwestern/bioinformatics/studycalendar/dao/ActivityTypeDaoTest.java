package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;

/**
 * @author Nataliya Shurupova
 */
public class ActivityTypeDaoTest extends ContextDaoTestCase<ActivityTypeDao> {

    public void testGetById() throws Exception {
        ActivityType activityType = getDao().getById(-10);
        assertNotNull(activityType);
        assertEquals("Wrong id ", -10, (int) activityType.getId());
    }

    public void testGetByName() throws Exception {
        ActivityType activityType = getDao().getByName("My Activity");
        assertNotNull(activityType);
        assertEquals("Wrong name ", "My Activity", activityType.getName());
    }
}
 