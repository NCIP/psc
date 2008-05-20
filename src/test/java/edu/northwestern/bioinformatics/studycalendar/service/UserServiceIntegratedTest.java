package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarDbTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;


/**
 * @author Rhett Sutphin
 */
public class UserServiceIntegratedTest extends StudyCalendarDbTestCase {
    private TransactionStatus transaction;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        transaction = getTransactionManager().getTransaction(
            new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED));
    }

    @Override
    protected void tearDown() throws Exception {
        getTransactionManager().rollback(transaction);
        // CSM seems to use its own transaction, or something
        getJdbcTemplate().execute("DELETE from csm_user");
        super.tearDown();
    }

    private PlatformTransactionManager getTransactionManager() {
        return (PlatformTransactionManager) getApplicationContext().getBean("transactionManager");
    }

    @Override
    protected String getTestDataFileName() {
        return "testdata/none.xml";
    }

    // this test is primarily to ensure that saving users all the way through to the database works,
    // including the mirror into CSM
    public void testUserCreationWorks() throws Exception {
        {
            User user = Fixtures.createUser("joe", Role.STUDY_ADMIN, Role.SITE_COORDINATOR);
            getUserService().saveUser(user, "alfalfa", "user@email.com");
        }

        User reloaded = getUserDao().getByName("joe");
        assertNotNull(reloaded);
    }

    private UserDao getUserDao() {
        return (UserDao) getApplicationContext().getBean("userDao");
    }

    private UserService getUserService() {
        return (UserService) getApplicationContext().getBean("userService");
    }
}
