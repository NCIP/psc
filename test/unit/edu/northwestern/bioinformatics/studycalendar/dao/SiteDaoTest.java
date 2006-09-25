package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;

import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class SiteDaoTest extends DaoTestCase {
    private SiteDao siteDao = (SiteDao) getApplicationContext().getBean("siteDao");

    public void testGetById() throws Exception {
        Site actual = siteDao.getById(4);
        assertNotNull("Study not found", actual);
        assertEquals("Wrong id", 4, (int) actual.getId());
        assertEquals("Wrong name", "default", actual.getName());
    }

    public void testGetDefaultSite() throws Exception {
        Site actual = siteDao.getDefaultSite();
        assertNotNull("Default study not found", actual);
        assertEquals(Site.DEFAULT_SITE_NAME, actual.getName());
    }

    public void testErrorIfNoDefaultSite() throws Exception {
        getJdbcTemplate().update("DELETE FROM sites WHERE name=?", new Object[] { Site.DEFAULT_SITE_NAME });

        try {
            siteDao.getDefaultSite();
            fail("No error");
        } catch (StudyCalendarError error) {
            assertEquals("No default site in database (should have a site named '" + Site.DEFAULT_SITE_NAME + "')", error.getMessage());
        }
    }
}
