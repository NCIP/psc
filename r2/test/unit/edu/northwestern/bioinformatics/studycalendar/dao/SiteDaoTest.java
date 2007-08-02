package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;

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
}
