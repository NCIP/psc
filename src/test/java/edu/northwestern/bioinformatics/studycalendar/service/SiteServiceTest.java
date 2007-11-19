package edu.northwestern.bioinformatics.studycalendar.service;

import static java.util.Arrays.asList;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;
import static edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools.createExternalObjectId;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import static org.easymock.classextension.EasyMock.*;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import gov.nih.nci.security.authorization.domainobjects.User;

import java.util.*;


/**
 * @author Padmaja Vedula
 */
public class SiteServiceTest extends StudyCalendarTestCase {
	private SiteDao siteDao;
    private SiteService service;
    private StudyCalendarAuthorizationManager authorizationManager;
    private StudySiteDao studySiteDao;


    protected void setUp() throws Exception {
        super.setUp();
        siteDao = registerMockFor(SiteDao.class);
        authorizationManager = registerMockFor(StudyCalendarAuthorizationManager.class);
        studySiteDao = registerMockFor(StudySiteDao.class);

        service = new SiteService();
        service.setSiteDao(siteDao);
        service.setStudyCalendarAuthorizationManager(authorizationManager);
        service.setStudySiteDao(studySiteDao);

    }
	
    public void testCreateSite() throws Exception {
		Site newSite = setId(1, createNamedInstance("new site", Site.class));
		siteDao.save(newSite);
		authorizationManager.createProtectionGroup("edu.northwestern.bioinformatics.studycalendar.domain.Site.1");
		replayMocks();
		
		Site siteCreated = service.createSite(newSite);
		verifyMocks();

        assertNotNull("site not returned", siteCreated);
        assertEquals("site name not set", "new site", siteCreated.getName());
    }

    public void testSaveSiteProtectionGroup() throws Exception {
        authorizationManager.createProtectionGroup("edu.northwestern.bioinformatics.studycalendar.domain.Site.1");
        replayMocks();

        service.saveSiteProtectionGroup("edu.northwestern.bioinformatics.studycalendar.domain.Site.1");
        verifyMocks();
    }

    public void testGetSitesForSiteCd() throws Exception {
        ProtectionGroup expectedPG = createProtectionGroup(1L, "edu.northwestern.bioinformatics.studycalendar.domain.Site.1");
        List<ProtectionGroup> exptectedPGs = Collections.singletonList(expectedPG);

        Site expectedSite = setId(1, createSite("northwestern"));
        List<Site> expectedSites = Collections.singletonList(expectedSite);

        expect(authorizationManager.getSitePGsForUser("a user")).andReturn(exptectedPGs);
        expect(DomainObjectTools
                .loadFromExternalObjectId("edu.northwestern.bioinformatics.studycalendar.domain.Site.1", siteDao)).andReturn(expectedSite);
        replayMocks();

        List<Site> actualSites = service.getSitesForSiteCd("a user");
        verifyMocks();

        assertEquals(expectedSites.size(), actualSites.size());
        assertEquals(expectedSites.get(0).getName(), actualSites.get(0).getName());
    }

    public void testRemoveProtectionGroupPSCDomainObjects() throws Exception {
        Site expectedSite = setId(1, createSite("northwestern"));

        edu.northwestern.bioinformatics.studycalendar.domain.User user = Fixtures.createUser(1, "John", 1L, true);
        user.addUserRole(createUserRole(user, Role.SUBJECT_COORDINATOR, expectedSite));

        ProtectionGroup expectedPG = createProtectionGroup(1L, "edu.northwestern.bioinformatics.studycalendar.domain.Site.1");

        expect(authorizationManager.getPGByName("edu.northwestern.bioinformatics.studycalendar.domain.Site.1")).andReturn(expectedPG);
        authorizationManager.removeProtectionGroupUsers(asList(user.getCsmUserId().toString()), expectedPG);
        replayMocks();

        service.removeProtectionGroup(expectedSite, user);
        verifyMocks();
    }

    public void testGetSitesForSubjectCoordinators() {
        ProtectionGroup expectedPG = createProtectionGroup(1L, "edu.northwestern.bioinformatics.studycalendar.domain.StudySite.1");
        List<ProtectionGroup> expectedPGs = Collections.singletonList(expectedPG);

        StudySite expectedStudySite = createStudySite("new york", 1);

        Set<Site> expectedSites = Collections.singleton(expectedStudySite.getSite());

        expect(authorizationManager.getStudySitePGsForUser("john")).andReturn(expectedPGs);
        expect(DomainObjectTools
                .loadFromExternalObjectId("edu.northwestern.bioinformatics.studycalendar.domain.StudySite.1", studySiteDao))
                .andReturn(expectedStudySite);
        replayMocks();

        Collection<Site> actualSites = service.getSitesForSubjectCoordinator("john");
        verifyMocks();

        assertEquals(expectedSites.size(), actualSites.size());
        assertTrue(expectedSites.containsAll(actualSites));
    }

    public void testGetSitesForUser() throws Exception {
        ProtectionGroup expectedPG = createProtectionGroup(1L, "edu.northwestern.bioinformatics.studycalendar.domain.Site.1");
        List<ProtectionGroup> exptectedPGs = Collections.singletonList(expectedPG);

        Site expectedSiteForSiteCd = createSite("northwestern");
        List<Site> expectedSitesForSiteCd = Collections.singletonList(expectedSiteForSiteCd);

        Site expectedSiteForParticipCoord = createSite("new york");
        List<Site> expectedSitesForParticipCoord = Collections.singletonList(expectedSiteForParticipCoord);

        StudySite expectedStudySite = createStudySite("new york", 1);

        Set<Site> expectedSites = new LinkedHashSet<Site>();
        expectedSites.addAll(expectedSitesForSiteCd);
        expectedSites.addAll(expectedSitesForParticipCoord);

        expect(authorizationManager.getSitePGsForUser("john")).andReturn(exptectedPGs);
        expect(DomainObjectTools
                .loadFromExternalObjectId("edu.northwestern.bioinformatics.studycalendar.domain.Site.1", siteDao)).andReturn(expectedSiteForSiteCd);
        expect(authorizationManager.getStudySitePGsForUser("john")).andReturn(exptectedPGs);
        expect(studySiteDao.getById(DomainObjectTools.parseExternalObjectId(expectedPG.getProtectionGroupName())))
                .andReturn(expectedStudySite);

        replayMocks();

        
        List<Site> actualSites = service.getSitesForUser("john");
        verifyMocks();

        assertEquals(expectedSites.size(), actualSites.size());
        assertTrue(expectedSites.containsAll(actualSites));
    }

    public void testAssignProtectionGroup() throws Exception {
        Site site = setId(1, Fixtures.createNamedInstance("Mayo Clinic", Site.class));
        edu.northwestern.bioinformatics.studycalendar.domain.User user = Fixtures.createUser(1, "John", 1L, true);
        Role role = Role.SUBJECT_COORDINATOR;
        ProtectionGroup pg = createProtectionGroup(new Long(site.getId()), DomainObjectTools.createExternalObjectId(site));

        expect(authorizationManager.getPGByName(pg.getProtectionGroupName())).andReturn(pg);
    	authorizationManager.assignProtectionGroupsToUsers(user.getCsmUserId().toString(), pg, role.csmRole());
        replayMocks();
        service.assignProtectionGroup(site, user, role);
        verifyMocks();
    }

    /* methods to create objects for mocks */

    private Site createSite(String aSiteName) {
        return createNamedInstance(aSiteName, Site.class);
    }
    
    private StudySite createStudySite(String aSiteName, Integer aId) {
        StudySite expectedStudySite = new StudySite();
        expectedStudySite.setId(aId);
        Site expectedSite = createSite(aSiteName);
        expectedStudySite.setSite(expectedSite);
        return expectedStudySite;
    }
}
