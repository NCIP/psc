package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;

import java.util.List;
import java.util.Iterator;

public class UserDaoTest extends ContextDaoTestCase<UserDao> {

    public void testGetById() throws Exception {
        User actualUser = getDao().getById(-100);
        assertNotNull("User not found", actualUser);
        assertEquals("Wrong id", -100, (int) actualUser.getId());
        assertEquals("Wrong name", "Joey", actualUser.getName());
        assertEquals("Wrong csm user id", -150, (long)actualUser.getCsmUserId());
        assertEquals("Wrong Role Size", 0, actualUser.getRoles().size());
        assertEquals("Wrong active flag value", new Boolean(false), actualUser.getActiveFlag());
    }

    public void testGetByName() throws Exception {
        List<User> actualUsers = getDao().getByName("Shurabado");
        assertEquals("Wrong List Size", 1, actualUsers.size());

        User actualUser = actualUsers.iterator().next();
        assertNotNull("User not found", actualUser);
        assertEquals("Wrong id", -200, (int) actualUser.getId());
        assertEquals("Wrong name", "Shurabado", actualUser.getName());
        assertEquals("Wrong csm user id", -250, (long)actualUser.getCsmUserId());
        assertEquals("Wrong Role Size", 1, actualUser.getRoles().size());
        assertEquals("Wrong Role", Role.STUDY_ADMIN.getCode(), actualUser.getRoles().iterator().next().getCode());
        assertEquals("Wrong active flag value", new Boolean(true), actualUser.getActiveFlag());
    }

    public void testSave() throws Exception {
        Integer savedId;
        {
            User user = new User();
            user.setName("New user");
            user.setCsmUserId(100L);
            user.addRole(Role.STUDY_COORDINATOR);
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
            assertEquals("Wrong Role", Role.STUDY_COORDINATOR.getCode(), loaded.getRoles().iterator().next().getCode());
            assertEquals("Wrong active flag value", new Boolean(true), loaded.getActiveFlag());
        }
    }

    public void getUserList() throws Exception {
        List<User> actualUsers = getDao().getAll();
        assertEquals("Wrong List Size", 2, actualUsers.size());
        Iterator<User> actualUsersIter = actualUsers.iterator();

        User actualUser = actualUsersIter.next();
        assertNotNull("User not found", actualUser);
        assertEquals("Wrong id", -200, (int) actualUser.getId());
        assertEquals("Wrong name", "Shurabado", actualUser.getName());
        assertEquals("Wrong csm user id", -250, (long)actualUser.getCsmUserId());
        assertEquals("Wrong Role Size", 1, actualUser.getRoles().size());
        assertEquals("Wrong Role", Role.STUDY_ADMIN.getCode(), actualUser.getRoles().iterator().next().getCode());
        assertEquals("Wrong active flag value", new Boolean(true), actualUser.getActiveFlag());

        actualUser = actualUsersIter.next();
        assertNotNull("User not found", actualUser);
        assertEquals("Wrong id", -100, (int) actualUser.getId());
        assertEquals("Wrong name", "Joey", actualUser.getName());
        assertEquals("Wrong csm user id", -150, (long)actualUser.getCsmUserId());
        assertEquals("Wrong Role Size", 0, actualUser.getRoles().size());
        assertEquals("Wrong active flag value", new Boolean(false), actualUser.getActiveFlag());
    }

    public void testLoadAndSave() throws Exception {
        Integer savedId;
        {
            User actualUser = getDao().getById(-100);
            assertNotNull("User not found", actualUser);
            assertEquals("Wrong id", -100, (int) actualUser.getId());
            assertEquals("Wrong name", "Joey", actualUser.getName());
            assertEquals("Wrong csm user id", -150, (long)actualUser.getCsmUserId());
            assertEquals("Wrong Role Size", 0, actualUser.getRoles().size());
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
            assertEquals("Wrong Role Size", 0, loaded.getRoles().size());
            assertEquals("Wrong active flag value", new Boolean(false), loaded.getActiveFlag());
        }
    }
}
