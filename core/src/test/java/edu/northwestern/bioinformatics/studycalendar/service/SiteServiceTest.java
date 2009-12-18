package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.StudyCalendarAuthorizationManager;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserRoleDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Role.SUBJECT_COORDINATOR;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.SiteConsumer;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import static org.easymock.EasyMock.notNull;
import static org.easymock.classextension.EasyMock.expect;

import java.util.*;
import static java.util.Arrays.asList;


/**
 * @author Padmaja Vedula
 */
public class SiteServiceTest extends StudyCalendarTestCase {
    private SiteDao siteDao;
    private SiteService service;
    private StudyCalendarAuthorizationManager authorizationManager;
    private SiteConsumer siteConsumer;
    private UserService userService;
    private User user;
    private Site nu, mayo;
    private UserRoleDao userRoleDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        siteDao = registerDaoMockFor(SiteDao.class);
        authorizationManager = registerMockFor(StudyCalendarAuthorizationManager.class);
        userService = registerMockFor(UserService.class);
        siteConsumer = registerMockFor(SiteConsumer.class);
        userRoleDao = registerDaoMockFor(UserRoleDao.class);

        service = new SiteService();
        service.setSiteDao(siteDao);
        service.setStudyCalendarAuthorizationManager(authorizationManager);
        service.setUserService(userService);
        service.setSiteConsumer(siteConsumer);
        service.setUserRoleDao(userRoleDao);

        user = edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createUser(7, "jimbo", 73L, true);
        nu = setId(1, Fixtures.createNamedInstance("Northwestern", Site.class));
        mayo = setId(4, Fixtures.createNamedInstance("Mayo", Site.class));

        expect(userService.getUserByName("jimbo")).andStubReturn(user);
    }

    public void testCreateSite() throws Exception {
        siteDao.save(nu);
        expect(authorizationManager.getProtectionGroup((DomainObject) notNull())).andReturn(null);
        expect(authorizationManager.createProtectionGroup(nu)).andReturn(
            createProtectionGroup("edu.northwestern.bioinformatics.studycalendar.domain.Site.1")
        );
        replayMocks();

        Site siteCreated = service.createOrUpdateSite(nu);
        verifyMocks();

        assertNotNull("site not returned", siteCreated);
    }

    public void testSaveSiteProtectionGroup() throws Exception {
        expect(authorizationManager.createProtectionGroup(nu)).andReturn(
            createProtectionGroup("edu.northwestern.bioinformatics.studycalendar.domain.Site.1")
        );
        replayMocks();


        service.saveSiteProtectionGroup(nu);
        verifyMocks();
    }

    public void testRemoveProtectionGroupPSCDomainObjects() throws Exception {
        user.addUserRole(createUserRole(user, SUBJECT_COORDINATOR, nu));

        ProtectionGroup expectedPG = createProtectionGroup(1L, "edu.northwestern.bioinformatics.studycalendar.domain.Site.1");

        expect(authorizationManager.getProtectionGroup((DomainObject) notNull())).andReturn(expectedPG);
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

    public void testAssignProtectionGroup() throws Exception {
        Role role = SUBJECT_COORDINATOR;
        ProtectionGroup pg = createProtectionGroup((long) mayo.getId(), DomainObjectTools.createExternalObjectId(mayo));

        expect(authorizationManager.getProtectionGroup((DomainObject) notNull())).andReturn(pg);
        authorizationManager.assignProtectionGroupsToUsers(user.getCsmUserId().toString(), pg, role.csmGroup());
        replayMocks();
        service.assignProtectionGroup(mayo, user, role);
        verifyMocks();
    }

    public void testRemoveRemoveableSite() throws Exception {
        Site site = new Site();
        site.setId(1);
        expect(userRoleDao.getUserRolesForSite(site)).andReturn(null);
        authorizationManager.removeProtectionGroup("edu.northwestern.bioinformatics.studycalendar.domain.Site.1");
        siteDao.delete(site);

        replayMocks();
        service.removeSite(site);
        verifyMocks();
    }
    
    public void testRemoveSiteWhenSiteMayNotBeRemoved() throws Exception {
        Site site = setId(4, new Site());
        Fixtures.createAssignment(new Study(), site, new Subject());

        replayMocks(); // expect nothing to happen
        service.removeSite(site);
        verifyMocks();
    }

    public void testCreateOrMergeSiteForCreateSite() throws Exception {
        siteDao.save(nu);
        expect(authorizationManager.getProtectionGroup((DomainObject) notNull())).andReturn(null);
        expect(authorizationManager.createProtectionGroup(nu)).andReturn(
            createProtectionGroup("edu.northwestern.bioinformatics.studycalendar.domain.Site.1")
        );
        replayMocks();


        Site newSite = service.createOrMergeSites(null, nu);
        assertEquals(newSite.getName(), nu.getName());
        verifyMocks();
    }

    public void testCreateOrMergeSiteForMergeSite() throws Exception {
        nu.setId(1);
        Site newSite = new Site();
        newSite.setName("new Name");
        siteDao.save(nu);
        expect(authorizationManager.getProtectionGroup((DomainObject) notNull())).andReturn(new ProtectionGroup());
        expect(siteDao.getById(1)).andReturn(nu);
        expect(siteConsumer.refresh(nu)).andReturn(nu);

        replayMocks();


        Site mergedSite = service.createOrMergeSites(nu, newSite);
        verifyMocks();
        assertEquals("new Name", mergedSite.getName());
        assertEquals(mergedSite, nu);
    }

    public void testGetByIdRefreshesSite() throws Exception {
        nu.setId(1);
        expect(siteDao.getById(1)).andReturn(nu);
        expect(siteConsumer.refresh(nu)).andReturn(nu);
        replayMocks();

        assertSame(nu, service.getById(1));
        verifyMocks();
    }

    public void testGetByIdForUnknownReturnsNull() throws Exception {
        expect(siteDao.getById(-1)).andReturn(null);
        replayMocks();

        assertNull(service.getById(-1));
        verifyMocks();
    }

    public void testGetByAssignedIdentRefreshesSite() throws Exception {
        expect(siteDao.getByAssignedIdentifier("NU")).andReturn(nu);
        expect(siteConsumer.refresh(nu)).andReturn(nu);
        replayMocks();

        assertSame(nu, service.getByAssignedIdentifier("NU"));
        verifyMocks();
    }
    
    public void testGetByAssignedIdentForUnknownReturnsNull() throws Exception {
        expect(siteDao.getByAssignedIdentifier("elf")).andReturn(null);
        replayMocks();

        assertNull(service.getByAssignedIdentifier("elf"));
        verifyMocks();
    }

    public void testGetByNameRefreshesSite() throws Exception {
        expect(siteDao.getByName("Northwestern")).andReturn(nu);
        expect(siteConsumer.refresh(nu)).andReturn(nu);
        replayMocks();

        assertSame(nu, service.getByName("Northwestern"));
        verifyMocks();
    }

    public void testGetByNameForUnknownReturnsNull() throws Exception {
        expect(siteDao.getByAssignedIdentifier("xyz")).andReturn(null);
        replayMocks();

        assertNull(service.getByAssignedIdentifier("xyz"));
        verifyMocks();
    }

    public void testGetAllRefreshes() throws Exception {
        List<Site> expected = Arrays.asList(nu, mayo);
        expect(siteDao.getAll()).andReturn(expected);
        expect(siteConsumer.refresh(expected)).andReturn(expected);
        replayMocks();

        List<Site> actual = service.getAll();
        assertSame("Wrong 0", nu, actual.get(0));
        assertSame("Wrong 1", mayo, actual.get(1));
        verifyMocks();
    }

    public void testMergeSiteForProvidedSite() throws Exception {
        nu.setId(1);
        nu.setProvider("Provider");
        Site newSite = new Site();
        try {
            service.createOrMergeSites(nu, newSite);
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException e) {
            assertEquals("The provided site Northwestern is not editable", e.getMessage());
        }
    }

    public void testDeleteSiteWhenSiteHasUserRole() throws Exception {

        Site site = createNamedInstance("Northwestern", Site.class);
        site.setId(11);
        UserRole userRole = createUserRole(user, Role.SUBJECT_COORDINATOR, site);
        expect(userRoleDao.getUserRolesForSite(site)).andReturn(Arrays.asList(userRole));
        authorizationManager.removeProtectionGroup("edu.northwestern.bioinformatics.studycalendar.domain.Site.11");
        siteDao.delete(site);

        replayMocks();
        service.removeSite(site);
        verifyMocks();
        assertFalse("UserRole shouldn't contain site", userRole.getSites().contains(site));
    }

    public void testDeleteSiteWhenSiteHasStudySiteRelation() throws Exception {
        Study study = createNamedInstance("Study A", Study.class);
        Site site = createNamedInstance("Northwestern", Site.class);
        site.setId(12);
        createStudySite(study, site);
        authorizationManager.removeProtectionGroup("edu.northwestern.bioinformatics.studycalendar.domain.Site.12");
        expect(userRoleDao.getUserRolesForSite(site)).andReturn(null);
        siteDao.delete(site);

        replayMocks();
        service.removeSite(site);
        verifyMocks();
    }
}
