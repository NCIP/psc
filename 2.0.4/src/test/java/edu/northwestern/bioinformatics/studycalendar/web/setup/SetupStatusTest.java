package edu.northwestern.bioinformatics.studycalendar.web.setup;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.*;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Rhett Sutphin
 */
public class SetupStatusTest extends StudyCalendarTestCase {
    private SetupStatus status;
    private SiteDao siteDao;
    private UserDao userDao;

    public void setUp() throws Exception {
        super.setUp();
        siteDao = registerDaoMockFor(SiteDao.class);
        userDao = registerDaoMockFor(UserDao.class);

        status = new SetupStatus();
        status.setSiteDao(siteDao);
        status.setUserDao(userDao);

        // default behaviors -- satisfied
        expect(siteDao.getCount()).andStubReturn(1);
        expect(userDao.getByRole(Role.SYSTEM_ADMINISTRATOR)).andStubReturn(Arrays.asList(new User()));
    }

    public void testSiteMissingWhenMissing() throws Exception {
        reset(siteDao);
        expect(siteDao.getCount()).andReturn(0);
        replayMocks();

        status.recheck();
        assertTrue(status.isSiteMissing());
        verifyMocks();
    }
    
    public void testSiteMissingWhenNotMissing() throws Exception {
        reset(siteDao);
        expect(siteDao.getCount()).andReturn(1);
        replayMocks();

        status.recheck();
        assertFalse(status.isSiteMissing());
        verifyMocks();
    }

    public void testNextWhenSiteIsNext() throws Exception {
        reset(siteDao);
        expect(siteDao.getCount()).andReturn(0);
        replayMocks();

        assertEquals(SetupStatus.InitialSetupElement.SITE, status.next());
        verifyMocks();
    }
    
    public void testNextWhenAdminIsNext() throws Exception {
        reset(userDao);
        expect(userDao.getByRole(Role.SYSTEM_ADMINISTRATOR)).andReturn(Collections.<User>emptyList());
        replayMocks();

        assertEquals(SetupStatus.InitialSetupElement.ADMINISTRATOR, status.next());
        verifyMocks();
    }
}
