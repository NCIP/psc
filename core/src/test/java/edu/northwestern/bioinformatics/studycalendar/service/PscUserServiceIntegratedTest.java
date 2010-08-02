package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.LegacyModeSwitch;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
@Deprecated
public class PscUserServiceIntegratedTest extends DaoTestCase {
    private PscUserService service;
    private SiteDao siteDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        service = (PscUserService) getApplicationContext().getBean("pscUserService");
        siteDao = (SiteDao) getApplicationContext().getBean("siteDao");
    }

    public void testUserLoading() throws Exception {
        PscUser loaded = service.loadUserByUsername("Joey");
        assertNotNull(loaded);
    }

    public void testGetUsers() throws Exception {
        List<PscUser> users = service.getAllUsers();
        assertEquals("Wrong number of users", 1, users.size());
        PscUser actualUser = users.get(0);
        assertEquals("Wrong user", new Long(-14), actualUser.getCsmUser().getUserId());
        assertEquals("Not expected to have memberships", 0, actualUser.getMemberships().size());
    }

    public void testUserModificationsAreNotAutomaticallyPersisted() throws Exception {
        if (!isLegacyMode()) return;
        int originalId;
        String originalName;
        {
            PscUser loaded = service.loadUserByUsername("Joey");

            Site someSite = loaded.getLegacyUser().getUserRole(Role.SUBJECT_COORDINATOR).getSites().iterator().next();
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

    private boolean isLegacyMode() {
        return ((LegacyModeSwitch) getApplicationContext().
            getBean("authorizationLegacyModeSwitch")).isOn();
    }
}
