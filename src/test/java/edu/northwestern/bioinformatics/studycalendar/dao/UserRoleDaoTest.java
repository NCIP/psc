package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;

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

}
