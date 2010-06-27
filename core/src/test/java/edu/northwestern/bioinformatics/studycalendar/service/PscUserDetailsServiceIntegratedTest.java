package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;

/**
 * @author Rhett Sutphin
 */
public class PscUserDetailsServiceIntegratedTest extends DaoTestCase {
    private PscUserDetailsService service;
    private SiteDao siteDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        service = (PscUserDetailsService) getApplicationContext().getBean("pscUserDetailsService");
        siteDao = (SiteDao) getApplicationContext().getBean("siteDao");
    }

    public void testUserLoading() throws Exception {
        User loaded = service.loadUserByUsername("Joey");
        assertNotNull(loaded);
    }

    public void testUserModificationsAreNotAutomaticallyPersisted() throws Exception {
        int originalId;
        String originalName;
        {
            User loaded = service.loadUserByUsername("Joey");

            Site someSite = loaded.getUserRole(Role.SUBJECT_COORDINATOR).getSites().iterator().next();
            originalId = someSite.getId();
            originalName = someSite.getName();
            someSite.setName("Something else");
        }

        interruptSession();

        {
            Site reloaded = siteDao.getById(originalId);
            assertEquals("Name changed", originalName, reloaded.getName());
        }
    }
}
