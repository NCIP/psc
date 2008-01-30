package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import static edu.northwestern.bioinformatics.studycalendar.domain.Role.*;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import static org.easymock.classextension.EasyMock.expect;

import static java.util.Arrays.asList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Arrays;


/**
 * @author Padmaja Vedula
 */
public class SiteServiceTest extends StudyCalendarTestCase {
    private SiteDao siteDao;
    private SiteService service;
    private StudyCalendarAuthorizationManager authorizationManager;
    private StudySiteDao studySiteDao;
    private UserDao userDao;
    private User user;
    private Site nu;
    private Site mayo;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        siteDao = registerDaoMockFor(SiteDao.class);
        authorizationManager = registerMockFor(StudyCalendarAuthorizationManager.class);
        studySiteDao = registerDaoMockFor(StudySiteDao.class);
        userDao = registerDaoMockFor(UserDao.class);

        service = new SiteService();
        service.setSiteDao(siteDao);
        service.setStudyCalendarAuthorizationManager(authorizationManager);
        service.setStudySiteDao(studySiteDao);
        service.setUserDao(userDao);

        user = Fixtures.createUser(7, "jimbo", 73L, true);
        nu = setId(1, Fixtures.createNamedInstance("Northwestern", Site.class));
        mayo = setId(4, Fixtures.createNamedInstance("Mayo", Site.class));

        expect(userDao.getByName("jimbo")).andReturn(user).anyTimes();
    }

    public void testCreateSite() throws Exception {
        siteDao.save(nu);
        authorizationManager.createProtectionGroup("edu.northwestern.bioinformatics.studycalendar.domain.Site.1");
        replayMocks();

        Site siteCreated = service.createSite(nu);
        verifyMocks();

        assertNotNull("site not returned", siteCreated);
    }

    public void testSaveSiteProtectionGroup() throws Exception {
        authorizationManager.createProtectionGroup("edu.northwestern.bioinformatics.studycalendar.domain.Site.1");
        replayMocks();

        service.saveSiteProtectionGroup("edu.northwestern.bioinformatics.studycalendar.domain.Site.1");
        verifyMocks();
    }

    public void testRemoveProtectionGroupPSCDomainObjects() throws Exception {
        user.addUserRole(createUserRole(user, SUBJECT_COORDINATOR, nu));

        ProtectionGroup expectedPG = createProtectionGroup(1L, "edu.northwestern.bioinformatics.studycalendar.domain.Site.1");

        expect(authorizationManager.getPGByName("edu.northwestern.bioinformatics.studycalendar.domain.Site.1")).andReturn(expectedPG);
        authorizationManager.removeProtectionGroupUsers(asList(user.getCsmUserId().toString()), expectedPG);
        replayMocks();

        service.removeProtectionGroup(nu, user);
        verifyMocks();
    }

    public void testGetSitesForSubjectCoordinators() {
        Set<Site> expectedSites = Collections.singleton(nu);

        Fixtures.setUserRoles(user, SUBJECT_COORDINATOR);
        user.getUserRole(SUBJECT_COORDINATOR).setSites(expectedSites);

        replayMocks();
        Collection<Site> actualSites = service.getSitesForSubjectCoordinator("jimbo");
        verifyMocks();

        assertEquals(expectedSites.size(), actualSites.size());
        assertTrue(actualSites.containsAll(expectedSites));
    }

    public void testGetSitesForUser() throws Exception {
        Fixtures.setUserRoles(user, SUBJECT_COORDINATOR, SITE_COORDINATOR);
        user.getUserRole(SUBJECT_COORDINATOR).setSites(Collections.singleton(nu));
        user.getUserRole(SITE_COORDINATOR).setSites(Collections.singleton(mayo));

        Set<Site> expectedSites = new LinkedHashSet<Site>(Arrays.asList(nu, mayo));

        replayMocks();
        List<Site> actualSites = service.getSitesForUser("jimbo");
        verifyMocks();

        assertEquals(expectedSites.size(), actualSites.size());
        assertTrue(expectedSites.containsAll(actualSites));
    }

    public void testAssignProtectionGroup() throws Exception {
        Role role = SUBJECT_COORDINATOR;
        ProtectionGroup pg = createProtectionGroup((long) mayo.getId(), DomainObjectTools.createExternalObjectId(mayo));

        expect(authorizationManager.getPGByName(pg.getProtectionGroupName())).andReturn(pg);
        authorizationManager.assignProtectionGroupsToUsers(user.getCsmUserId().toString(), pg, role.csmGroup());
        replayMocks();
        service.assignProtectionGroup(mayo, user, role);
        verifyMocks();
    }
}
