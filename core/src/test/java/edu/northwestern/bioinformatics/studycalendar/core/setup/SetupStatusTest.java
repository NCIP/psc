package edu.northwestern.bioinformatics.studycalendar.core.setup;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
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
    private SourceDao sourceDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        siteDao = registerDaoMockFor(SiteDao.class);
        userDao = registerDaoMockFor(UserDao.class);
        sourceDao = registerDaoMockFor(SourceDao.class);

        status = new SetupStatus();
        status.setSiteDao(siteDao);
        status.setUserDao(userDao);
        status.setSourceDao(sourceDao);

        // default behaviors -- satisfied
        expect(siteDao.getCount()).andStubReturn(1);
        expect(userDao.getByRole(Role.SYSTEM_ADMINISTRATOR)).andStubReturn(Arrays.asList(new User()));
        expect(sourceDao.getCount()).andStubReturn(1);
    }

    public void testSiteMissingWhenMissing() throws Exception {
        expect(siteDao.getCount()).andReturn(0);
        replayMocks();

        status.recheck();
        assertTrue(status.isSiteMissing());
        verifyMocks();
    }
    
    public void testSiteMissingWhenNotMissing() throws Exception {
        expect(siteDao.getCount()).andReturn(1);
        replayMocks();

        status.recheck();
        assertFalse(status.isSiteMissing());
        verifyMocks();
    }

    public void testSourceMissingWhenMissing() throws Exception {
        expect(sourceDao.getCount()).andReturn(0);
        replayMocks();

        status.recheck();
        assertTrue(status.isSourceMissing());
        verifyMocks();
    }

    public void testSourceMissingWhenNotMissing() throws Exception {
        expect(sourceDao.getCount()).andReturn(1);
        replayMocks();

        status.recheck();
        assertFalse(status.isSourceMissing());
        verifyMocks();
    }

    public void testPostAuthenticationSiteSetup() throws Exception {
        expect(siteDao.getCount()).andReturn(0);
        replayMocks();

        assertEquals(SetupStatus.InitialSetupElement.SITE, status.postAuthenticationSetup());
        verifyMocks();
    }

    public void testPostAuthenticationSourceSetup() throws Exception {
        expect(sourceDao.getCount()).andReturn(0);
        replayMocks();

        assertEquals(SetupStatus.InitialSetupElement.SOURCE, status.postAuthenticationSetup());
        verifyMocks();
    }
    
    public void testPreAuthenticationSetup() throws Exception {
        expect(userDao.getByRole(Role.SYSTEM_ADMINISTRATOR)).andReturn(Collections.<User>emptyList());
        replayMocks();

        assertEquals(SetupStatus.InitialSetupElement.ADMINISTRATOR, status.preAuthenticationSetup());
        verifyMocks();
    }
}
