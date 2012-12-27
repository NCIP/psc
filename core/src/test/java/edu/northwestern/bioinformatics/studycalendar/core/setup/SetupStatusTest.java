/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.core.setup;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class SetupStatusTest extends StudyCalendarTestCase {
    private SetupStatus status;
    private SiteDao siteDao;
    private SourceDao sourceDao;
    private PscUserService pscUserService;
    private JdbcTemplate jdbcTemplate;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        siteDao = registerDaoMockFor(SiteDao.class);
        sourceDao = registerDaoMockFor(SourceDao.class);
        pscUserService = registerMockFor(PscUserService.class);
        jdbcTemplate = registerMockFor(JdbcTemplate.class);

        status = new SetupStatus();
        status.setSiteDao(siteDao);
        status.setSourceDao(sourceDao);
        status.setPscUserService(pscUserService);
        status.setJdbcTemplate(jdbcTemplate);

        // default behaviors -- satisfied
        expect(siteDao.getCount()).andStubReturn(1);
        expect(sourceDao.getManualTargetSource()).andStubReturn(new Source());
        expect(pscUserService.getCsmUsers(PscRole.SYSTEM_ADMINISTRATOR)).andStubReturn(
            Collections.singleton(AuthorizationObjectFactory.createCsmUser("jo")));
        expect(jdbcTemplate.queryForInt(SetupStatus.AUTHENTICATION_SYSTEM_SET_QUERY)).
            andStubReturn(1);
    }

    public void testSiteMissingWhenMissing() throws Exception {
        expectNoSites();
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
        expectNoManualActivityTargetSource();
        replayMocks();

        status.recheck();
        assertTrue(status.isSourceMissing());
        verifyMocks();
    }

    public void testSourceMissingWhenNotMissing() throws Exception {
        expect(sourceDao.getManualTargetSource()).andReturn(new Source());
        replayMocks();

        status.recheck();
        assertFalse(status.isSourceMissing());
        verifyMocks();
    }

    public void testAdministratorMissingWhenMissing() throws Exception {
        expectNoSystemAdministrators();
        replayMocks();

        status.recheck();
        assertTrue(status.isAdministratorMissing());
        verifyMocks();
    }

    public void testAdministratorMissingWhenNotMissing() throws Exception {
        expect(pscUserService.getCsmUsers(PscRole.SYSTEM_ADMINISTRATOR)).
            andReturn(Collections.singleton(new gov.nih.nci.security.authorization.domainobjects.User()));
        replayMocks();

        status.recheck();
        assertFalse(status.isAdministratorMissing());
        verifyMocks();
    }

    public void testAuthenticationSystemConfiguredWhenNotConfigured() throws Exception {
        expectNoAuthenticationSystem();
        replayMocks();

        status.recheck();
        assertTrue(status.isAuthenticationSystemNotConfigured());
        verifyMocks();
    }

    public void testAuthenticationSystemConfiguredWhenConfigured() throws Exception {
        expect(jdbcTemplate.queryForInt(SetupStatus.AUTHENTICATION_SYSTEM_SET_QUERY)).andReturn(1);
        replayMocks();

        status.recheck();
        assertFalse(status.isAuthenticationSystemNotConfigured());
        verifyMocks();
    }

    public void testPreAuthenticationSetupNeededWhenNoAdministrator() throws Exception {
        expectNoSystemAdministrators();
        replayMocks();

        assertTrue(status.isPreAuthenticationSetupNeeded());
        verifyMocks();
    }

    public void testPreAuthenticationSetupNeededWhenNoAuthSystem() throws Exception {
        expectNoAuthenticationSystem();
        replayMocks();

        assertTrue(status.isPreAuthenticationSetupNeeded());
        verifyMocks();
    }

    public void testPreAuthenticationSetupNotNeededWhenSatisfied() throws Exception {
        replayMocks();

        assertFalse(status.isPreAuthenticationSetupNeeded());
        verifyMocks();
    }

    public void testExpectPostAuthenticationSetupNeededWithNoSites() throws Exception {
        expectNoSites();
        replayMocks();

        assertTrue(status.isPostAuthenticationSetupNeeded());
        verifyMocks();
    }

    public void testExpectPostAuthenticationSetupNeededWithNoManualSource() throws Exception {
        expectNoManualActivityTargetSource();
        replayMocks();

        assertTrue(status.isPostAuthenticationSetupNeeded());
        verifyMocks();
    }

    public void testExpectPostAuthenticationSetupNotNeededWhenSatisfied() throws Exception {
        replayMocks();

        assertFalse(status.isPostAuthenticationSetupNeeded());
        verifyMocks();
    }

    public void testPostAuthenticationSetupGoesToSiteFirst() throws Exception {
        expectNoSites();
        expectNoManualActivityTargetSource();
        replayMocks();

        assertEquals(SetupStatus.InitialSetupElement.SITE, status.postAuthenticationSetup());
        verifyMocks();
    }

    public void testPostAuthenticationSetupGoesToSourceSecond() throws Exception {
        expect(siteDao.getCount()).andReturn(1);
        expectNoManualActivityTargetSource();
        replayMocks();

        assertEquals(SetupStatus.InitialSetupElement.SOURCE, status.postAuthenticationSetup());
        verifyMocks();
    }

    public void testPreAuthenticationSetupGoesToAuthenticationSystemFirst() throws Exception {
        expectNoAuthenticationSystem();
        expectNoSystemAdministrators();
        replayMocks();

        assertEquals(SetupStatus.InitialSetupElement.AUTHENTICATION_SYSTEM, status.preAuthenticationSetup());
    }

    public void testPreAuthenticationSetupGoesToAdminSecond() throws Exception {
        expectNoSystemAdministrators();
        replayMocks();

        assertEquals(SetupStatus.InitialSetupElement.ADMINISTRATOR, status.preAuthenticationSetup());
        verifyMocks();
    }

    ////// HELPERS

    private void expectNoManualActivityTargetSource() {
        expect(sourceDao.getManualTargetSource()).andReturn(null);
    }

    private void expectNoSites() {
        expect(siteDao.getCount()).andReturn(0);
    }

    private void expectNoAuthenticationSystem() {
        expect(jdbcTemplate.queryForInt(SetupStatus.AUTHENTICATION_SYSTEM_SET_QUERY)).andReturn(0);
    }

    private void expectNoSystemAdministrators() {
        expect(pscUserService.getCsmUsers(PscRole.SYSTEM_ADMINISTRATOR)).
            andReturn(Collections.<gov.nih.nci.security.authorization.domainobjects.User>emptySet());
    }
}
