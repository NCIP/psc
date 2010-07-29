package edu.northwestern.bioinformatics.studycalendar.core.setup;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService;

import java.util.Collections;

import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class SetupStatusTest extends StudyCalendarTestCase {
    private SetupStatus status;
    private SiteDao siteDao;
    private SourceDao sourceDao;
    private AuthorizationService authorizationService;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        siteDao = registerDaoMockFor(SiteDao.class);
        sourceDao = registerDaoMockFor(SourceDao.class);
        authorizationService = registerMockFor(AuthorizationService.class);

        status = new SetupStatus();
        status.setSiteDao(siteDao);
        status.setSourceDao(sourceDao);
        status.setAuthorizationService(authorizationService);

        // default behaviors -- satisfied
        expect(siteDao.getCount()).andStubReturn(1);
        expect(sourceDao.getCount()).andStubReturn(1);
        expect(authorizationService.getCsmUsers(PscRole.SYSTEM_ADMINISTRATOR)).andStubReturn(
            Collections.singleton(AuthorizationObjectFactory.createCsmUser("jo")));
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

    public void testAdministratorMissingWhenMissing() throws Exception {
        expect(authorizationService.getCsmUsers(PscRole.SYSTEM_ADMINISTRATOR)).
            andReturn(Collections.<gov.nih.nci.security.authorization.domainobjects.User>emptySet());
        replayMocks();

        status.recheck();
        assertTrue(status.isAdministratorMissing());
        verifyMocks();
    }

    public void testAdministratorMissingWhenNotMissing() throws Exception {
        expect(authorizationService.getCsmUsers(PscRole.SYSTEM_ADMINISTRATOR)).
            andReturn(Collections.singleton(new gov.nih.nci.security.authorization.domainobjects.User()));
        replayMocks();

        status.recheck();
        assertFalse(status.isAdministratorMissing());
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
        expect(authorizationService.getCsmUsers(PscRole.SYSTEM_ADMINISTRATOR)).
            andReturn(Collections.<gov.nih.nci.security.authorization.domainobjects.User>emptySet());
        replayMocks();

        assertEquals(SetupStatus.InitialSetupElement.ADMINISTRATOR, status.preAuthenticationSetup());
        verifyMocks();
    }
}
