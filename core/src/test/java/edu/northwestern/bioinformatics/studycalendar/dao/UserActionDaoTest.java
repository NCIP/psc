package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.UserAction;

public class UserActionDaoTest extends ContextDaoTestCase<UserActionDao> {
//    public void testGetAll() throws Exception {
//        List<UserAction> actual = getDao().getAll();
//        assertEquals("Wrong size", 2, actual.size());
//        assertEquals("Wrong first subject", "Ng", actual.get(0).getLastName());
//        assertEquals("Wrong second subject", "Scott", actual.get(1).getLastName());
//    }

    public void testGetById() throws Exception {
        UserAction action = getDao().getById(-100);
        assertNotNull("User action not found", action);
        assertEquals("Wrong description", "Notification dismissed", action.getDescription());
    }
}
