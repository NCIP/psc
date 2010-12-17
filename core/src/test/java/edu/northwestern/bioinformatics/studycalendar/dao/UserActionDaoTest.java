package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.UserAction;

public class UserActionDaoTest extends ContextDaoTestCase<UserActionDao> {
    public void testGetById() throws Exception {
        UserAction action = getDao().getById(-100);
        assertNotNull("User action not found", action);
        assertEquals("Wrong description", "Notification dismissed", action.getDescription());
    }
}
