package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.UserAction;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class UserActionDaoTest extends ContextDaoTestCase<UserActionDao> {
    private UserActionDao dao = (UserActionDao) getApplicationContext().getBean("userActionDao");

    public void setUp() throws Exception {
        super.setUp();
        dao.setJdbcTemplate(getJdbcTemplate());
    }

    public void testGetById() throws Exception {
        UserAction action = getDao().getById(-100);
        assertNotNull("User action not found", action);
        assertEquals("Wrong description", "Notification dismissed", action.getDescription());
    }

    public void testGetUserActionsByContext() throws Exception {
        String context = "http://foo/bar";
        List<UserAction> userActions = dao.getUserActionsByContext(context);
        assertNotNull("User actions not found", userActions);
        assertEquals("Wrong no of user actions", 2, userActions.size());
    }

    public void testGetUserActionsByContextAssociatedWithTime() throws Exception {
        String context = "http://foo/bar";
        List<UserAction> userActions = dao.getUserActionsByContext(context);
        assertNotNull("User actions not found", userActions);
        assertEquals("Wrong no of user actions", 2, userActions.size());
        SimpleDateFormat sdf = DateFormat.getUTCFormat();

        Date time1 = sdf.parse("2010-08-17 21:27:58.361");
        UserAction ua1 = userActions.get(0);
        assertEquals("Wrong user action", "Alabaster", ua1.getGridId());
        assertNotNull("Time stamp not associated", ua1.getTime());
        assertEquals("Wrong time", time1, ua1.getTime());

        Date time2 = sdf.parse("2010-08-17 23:24:58.361");
        UserAction ua2 = userActions.get(1);
        assertEquals("Wrong user action", "grid1", ua2.getGridId());
        assertNotNull("Time stamp not associated", ua2.getTime());
        assertEquals("Wrong time", time2, ua2.getTime());
    }
}
