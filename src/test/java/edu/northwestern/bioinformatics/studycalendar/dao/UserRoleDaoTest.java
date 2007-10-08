package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.*;

import java.util.Iterator;

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

    public void testLoadAndSave() throws Exception {
        Integer savedId;
        {
            UserRole actualUserRole = getDao().getById(-1);
            assertNotNull("User Role not found", actualUserRole);
            assertEquals("Wrong user role id", -1, (int) actualUserRole.getId());
            assertEquals("Wrong user id", -1, (int) actualUserRole.getId());
            assertEquals("Wrong role", Role.STUDY_ADMIN, actualUserRole.getRole());
            Iterator<Site> siteIter = actualUserRole.getSites().iterator();
            assertEquals("Wrong site", "Mayo Clinic", siteIter.next().getName());
            assertEquals("Wrong site", "Northwestern Clinic", siteIter.next().getName());

            actualUserRole.setRole(Role.SITE_COORDINATOR);
            
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
            Iterator<Site> siteIter = loaded.getSites().iterator();
            assertEquals("Wrong site", "Mayo Clinic", siteIter.next().getName());
            assertEquals("Wrong site", "Northwestern Clinic", siteIter.next().getName());

        }
    }
}
