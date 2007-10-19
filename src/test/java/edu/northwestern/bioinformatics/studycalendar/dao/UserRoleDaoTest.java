package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.*;

import java.util.Iterator;
import java.util.Collections;
import java.util.List;

public class UserRoleDaoTest  extends ContextDaoTestCase<UserRoleDao> {
    public void testGetUserRoleById() throws Exception {
        UserRole userRole = getDao().getById(-1);
        assertNotNull("User  Role not found", userRole);
        assertEquals("Wrong user role id", -1, (int) userRole.getId());
        assertEquals("Wrong user id", -1, (int) userRole.getId());
        assertEquals("Wrong role", Role.STUDY_ADMIN, userRole.getRole());
        Iterator<Site> siteIter = userRole.getSites().iterator();
        assertEquals("Wrong site", "Mayo Clinic", siteIter.next().getName());
        assertEquals("Wrong site", "Northwestern Clinic", siteIter.next().getName());
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
            Iterator<Site> siteIter = actualUserRole.getSites().iterator();
            assertEquals("Wrong site", "Mayo Clinic", siteIter.next().getName());
            assertEquals("Wrong site", "Northwestern Clinic", siteIter.next().getName());

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
            Iterator<Site> siteIter = loaded.getSites().iterator();
            assertEquals("Wrong site", "Mayo Clinic", siteIter.next().getName());

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
            Iterator<Site> siteIter = actualUserRole.getSites().iterator();
            assertEquals("Wrong site", "Mayo Clinic", siteIter.next().getName());
            assertEquals("Wrong site", "Northwestern Clinic", siteIter.next().getName());

            actualUserRole.setRole(Role.SITE_COORDINATOR);
            actualUserRole.addSite(((SiteDao)getApplicationContext().getBean("siteDao")).getById(-400));

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
            Iterator<Site> siteIter = loaded.getSites().iterator();
            assertEquals("Wrong site", "Mayo Clinic", siteIter.next().getName());
            assertEquals("Wrong site", "Northwestern Clinic", siteIter.next().getName());
            assertEquals("Wrong site", "CDC Clinic", siteIter.next().getName());

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

    public void testGetAllParticipantCoordinators() throws Exception {
        List<UserRole> usersRoles = getDao().getAllParticipantCoordinatorUserRoles();

        assertEquals("wrong participant coordinator", "PC A", usersRoles.get(0).getUser().getName());
        assertEquals("wrong participant coordinator", "PC B", usersRoles.get(1).getUser().getName());
    }
}
