
package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;

import java.util.Collection;
import java.util.Collections;

public class UserRoleDaoTest  extends ContextDaoTestCase<UserRoleDao> {
    private SiteDao siteDao;
    private StudySiteDao studySiteDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        siteDao      = (SiteDao)getApplicationContext().getBean("siteDao");
        studySiteDao = (StudySiteDao)getApplicationContext().getBean("studySiteDao");
    }

    public void testGetUserRoleById() throws Exception {
        UserRole userRole = getDao().getById(-1);
        assertNotNull("User  Role not found", userRole);
        assertEquals("Wrong user role id", -1, (int) userRole.getId());
        assertEquals("Wrong user id", -1, (int) userRole.getId());
        assertEquals("Wrong role", Role.STUDY_ADMIN, userRole.getRole());
        assertSitePresent("Northwestern Clinic", userRole.getSites());
        assertSitePresent("Mayo Clinic", userRole.getSites());
        assertEquals("Wrong user name", "Joey", userRole.getUser().getName());
    }

    public void testLoadAndRemoveSiteAndSave() throws Exception {
        Integer savedId;
        {
            UserRole actualUserRole = getDao().getById(-1);
            assertNotNull("User Role not found", actualUserRole);
            assertEquals("Wrong user role id", -1, (int) actualUserRole.getId());
            assertEquals("Wrong user id", -1, (int) actualUserRole.getId());
            assertEquals("Wrong role", Role.STUDY_ADMIN, actualUserRole.getRole());
            assertEquals("Wrong site size", 2, actualUserRole.getSites().size());
            assertSitePresent("Northwestern Clinic", actualUserRole.getSites());
            assertSitePresent("Mayo Clinic", actualUserRole.getSites());

            actualUserRole.setRole(Role.SITE_COORDINATOR);
            actualUserRole.removeSite(Fixtures.setId(-300, Fixtures.createNamedInstance("Northwestern Clinic", Site.class)));

            getDao().save(actualUserRole);
            savedId = actualUserRole.getId();
        }

        interruptSession();

        {
            UserRole loaded = getDao().getById(savedId);
            assertNotNull("User Role not found", loaded);
            assertEquals("Wrong user role id", -1, (int) loaded.getId());
            assertEquals("Wrong user id", -1, (int) loaded.getId());
            assertEquals("Wrong role", Role.SITE_COORDINATOR, loaded.getRole());
            assertEquals("Wrong site size", 1, loaded.getSites().size());
            assertSitePresent("Mayo Clinic", loaded.getSites());
        }
    }

    public void testLoadAndAddSiteAndSave() throws Exception {
        Integer savedId;
        {
            UserRole actualUserRole = getDao().getById(-1);
            assertNotNull("User Role not found", actualUserRole);
            assertEquals("Wrong user role id", -1, (int) actualUserRole.getId());
            assertEquals("Wrong user id", -1, (int) actualUserRole.getId());
            assertEquals("Wrong role", Role.STUDY_ADMIN, actualUserRole.getRole());
            assertEquals("Wrong site size", 2, actualUserRole.getSites().size());
            assertSitePresent("Northwestern Clinic", actualUserRole.getSites());
            assertSitePresent("Mayo Clinic", actualUserRole.getSites());

            actualUserRole.setRole(Role.SITE_COORDINATOR);
            actualUserRole.addSite(siteDao.getById(-400));

            getDao().save(actualUserRole);
            savedId = actualUserRole.getId();
        }

        interruptSession();

        {
            UserRole loaded = getDao().getById(savedId);
            assertNotNull("User Role not found", loaded);
            assertEquals("Wrong user role id", -1, (int) loaded.getId());
            assertEquals("Wrong user id", -1, (int) loaded.getId());
            assertEquals("Wrong role", Role.SITE_COORDINATOR, loaded.getRole());
            assertEquals("Wrong site size", 3, loaded.getSites().size());
            assertSitePresent("Northwestern Clinic", loaded.getSites());
            assertSitePresent("Mayo Clinic", loaded.getSites());
            assertSitePresent("CDC Clinic", loaded.getSites());
        }
    }

    public void testAddStudySite() throws Exception {
        Integer savedId;
        {
            UserRole actualUserRole = getDao().getById(-1);
            assertNotNull("User Role not found", actualUserRole);
            assertEquals("Wrong id", -1, (int) actualUserRole.getId());
            assertEquals("No study sites assigned", 1, actualUserRole.getStudySites().size());

            StudySite studySite = studySiteDao.getById(-2001);
            actualUserRole.addStudySite(studySite);
            getDao().save(actualUserRole);
            savedId = actualUserRole.getId();
        }

        interruptSession();

        {
            UserRole loaded = getDao().getById(savedId);
            assertNotNull("Could not reload user role with id " + savedId, loaded);
            assertEquals("Wrong id", -1, (int) loaded.getId());
            assertEquals("Wrong study site size", 2, loaded.getStudySites().size());
        }
    }

    public void testRemoveStudySite() throws Exception {
      Integer savedId;
        {
            UserRole actualUserRole = getDao().getById(-1);
            assertNotNull("User Role not found", actualUserRole);
            assertEquals("Wrong id", -1, (int) actualUserRole.getId());
            assertEquals("No study sites assigned", 1, actualUserRole.getStudySites().size());

            actualUserRole.setStudySites(Collections.<StudySite>emptyList());
            getDao().save(actualUserRole);
            savedId = actualUserRole.getId();
        }

        interruptSession();

        {
            UserRole loaded = getDao().getById(savedId);
            assertNotNull("Could not reload user role with id " + savedId, loaded);
            assertEquals("Wrong id", -1, (int) loaded.getId());
            assertEquals("Wrong study site size", 0, loaded.getStudySites().size());
        }
    }
    
    private static void assertSitePresent(String expectedSiteName, Collection<Site> actualSites) {
        for (Site site : actualSites) {
            if (site.getName().equals(expectedSiteName)) return;
        }
        fail("Expected site not present: " + expectedSiteName + " in " + actualSites);
    }
}
