package edu.northwestern.bioinformatics.studycalendar.service;

import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import edu.northwestern.bioinformatics.studycalendar.testing.ContextTools;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;

import java.util.Date;

import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;


/**
 * @author Rhett Sutphin
 */
public class UserServiceIntegratedTest extends AbstractTransactionalDataSourceSpringContextTests {
    public UserServiceIntegratedTest() {
        setAutowireMode(AUTOWIRE_BY_NAME);
    }

    @Override
    protected String[] getConfigLocations() {
        return ContextTools.DEPLOYED_CONFIG_LOCATIONS;
    }

    @Override
    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();
        DataAuditInfo.setLocal(new DataAuditInfo("admin", "127.0.0.8", new Date()));
    }

    @Override
    protected void onTearDownAfterTransaction() throws Exception {
        DataAuditInfo.setLocal(null);
        // CSM seems to do its own COMMIT, so we need to clean up after it 
        deleteFromTables(new String[] { "csm_user" });
        super.onTearDownAfterTransaction();
    }

    // this test is primarily to ensure that saving users all the way through to the database works,
    // including the mirror into CSM
    public void testUserCreationWorks() throws Exception {
        {
            User user = Fixtures.createUser("joe", Role.STUDY_ADMIN, Role.SITE_COORDINATOR);
            getUserService().saveUser(user, "alfalfa");
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
