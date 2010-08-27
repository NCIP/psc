package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;

public class UserDaoTest extends ContextDaoTestCase<UserDao> {
    public void testGetById() throws Exception {
        User actualUser = getDao().getById(-100);
        assertNotNull("User not found", actualUser);
        assertEquals("Wrong id", -100, (int) actualUser.getId());
        assertEquals("Wrong name", "Joey", actualUser.getName());
        assertEquals("Wrong csm user id", -150, (long)actualUser.getCsmUserId());
        assertEquals("Wrong Role Size", 0, actualUser.getUserRoles().size());
        assertEquals("Wrong active flag value", new Boolean(false), actualUser.getActiveFlag());
    }

    public void testGetByName() throws Exception {
        User actualUser = getDao().getByName("Shurabado");
        assertNotNull("User not found", actualUser);
        assertEquals("Wrong id", -200, (int) actualUser.getId());
        assertEquals("Wrong name", "Shurabado", actualUser.getName());
        assertEquals("Wrong csm user id", -250, (long)actualUser.getCsmUserId());
        assertEquals("Wrong Role Size", 1, actualUser.getUserRoles().size());
        assertEquals("Wrong Role", Role.STUDY_ADMIN, actualUser.getUserRoles().iterator().next().getRole());
        assertEquals("Wrong active flag value", new Boolean(true), actualUser.getActiveFlag());
    }

    public void testSave() throws Exception {
        Integer savedId;
        {
            User user = new User();
            user.setName("New user");
            user.setCsmUserId(100L);
            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(Role.STUDY_COORDINATOR);
            user.addUserRole(userRole);
            user.setActiveFlag(new Boolean(true));

            getDao().save(user);
            savedId = user.getId();
            assertNotNull("The saved user didn't get an id", savedId);
        }

        interruptSession();

        {   
            User loaded = getDao().getById(savedId);
            assertNotNull("Could not reload user with id " + savedId, loaded);
            assertEquals("Wrong name", "New user", loaded.getName());
            assertEquals("Wrong CSM User Id", 100L, (long) loaded.getCsmUserId());
            assertEquals("Wrong Role", Role.STUDY_COORDINATOR, loaded.getUserRoles().iterator().next().getRole());
            assertEquals("Wrong active flag value", new Boolean(true), loaded.getActiveFlag());
        }
    }

    public void testLoadAndSave() throws Exception {
        Integer savedId;
        {
            User actualUser = getDao().getById(-100);
            assertNotNull("User not found", actualUser);
            assertEquals("Wrong id", -100, (int) actualUser.getId());
            assertEquals("Wrong name", "Joey", actualUser.getName());
            assertEquals("Wrong csm user id", -150, (long)actualUser.getCsmUserId());
            assertEquals("Wrong Role Size", 0, actualUser.getUserRoles().size());
            assertEquals("Wrong active flag value", new Boolean(false), actualUser.getActiveFlag());

            actualUser.setName("UpdatedName");
            getDao().save(actualUser);
            savedId = actualUser.getId();
        }

        interruptSession();

        {
            User loaded = getDao().getById(savedId);
            assertNotNull("Could not reload user with id " + savedId, loaded);
            assertEquals("Wrong id", -100, (int) loaded.getId());
            assertEquals("Wrong name", "UpdatedName", loaded.getName());
            assertEquals("Wrong csm user id", -150, (long)loaded.getCsmUserId());
            assertEquals("Wrong Role Size", 0, loaded.getUserRoles().size());
            assertEquals("Wrong active flag value", new Boolean(false), loaded.getActiveFlag());
        }
    }

    public void testDeleteUnreferencedRole() throws Exception {
      Integer savedId;
        {
            User actualUser = getDao().getById(-200);
            assertNotNull("User not found", actualUser);
            assertEquals("Wrong id", -200, (int) actualUser.getId());
            assertEquals("No roles assigned", 1, actualUser.getUserRoles().size());


            actualUser.clearUserRoles();
            getDao().save(actualUser);
            savedId = actualUser.getId();
        }

        interruptSession();

        {
            User loaded = getDao().getById(savedId);
            assertNotNull("Could not reload user with id " + savedId, loaded);
            assertEquals("Wrong id", -200, (int) loaded.getId());
            assertEquals("Wrong Role Size", 0, loaded.getUserRoles().size());
        }
    }

}
