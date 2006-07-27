package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;

import java.util.List;

/**
 * @author Jaron Sampson
 */
public class ActivityDaoTest extends DaoTestCase {
    private ActivityDao dao = (ActivityDao) getApplicationContext().getBean("activityDao");

    public void testGetById() throws Exception {
        Activity activity = dao.getById(100);
        assertNotNull("Screening Activity not found", activity);
        assertEquals("Wrong name", "Screening Activity", activity.getName());
    }

}