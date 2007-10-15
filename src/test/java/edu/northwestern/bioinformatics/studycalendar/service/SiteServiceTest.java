package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;
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
		authorizationManager.createProtectionGroup("edu.northwestern.bioinformatics.studycalendar.domain.Site.1", "BaseSitePG");
		replayMocks();
		
		Site siteCreated = service.createSite(newSite);
		verifyMocks();

        assertNotNull("site not returned", siteCreated);
        assertEquals("site name not set", "new site", siteCreated.getName());
    }

    public void testSaveSiteProtectionGroup() throws Exception {
        authorizationManager.createProtectionGroup("edu.northwestern.bioinformatics.studycalendar.domain.Site.1", SiteService.BASE_SITE_PG);
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

    public void testAssignSiteCoordinators() throws Exception {
        Site expectedSite = setId(1, createSite("northwestern"));

        List<String> expectedCoordinators = Collections.singletonList("john");

        ProtectionGroup expectedPG = createProtectionGroup(1L, "edu.northwestern.bioinformatics.studycalendar.domain.Site.1");

        expect(authorizationManager.getPGByName("edu.northwestern.bioinformatics.studycalendar.domain.Site.1")).andReturn(expectedPG);
        authorizationManager.assignProtectionGroupsToUsers(expectedCoordinators,
                                                           expectedPG,
                                                           SiteService.SITE_COORDINATOR_ACCESS_ROLE);
        replayMocks();

        service.assignSiteCoordinators(expectedSite, expectedCoordinators);
        verifyMocks();
    }

    public void testAssignParticipantCoordinators() throws Exception {
        Site expectedSite = setId(1, createSite("northwestern"));

        List<String> expectedCoordinators = Collections.singletonList("john");

        ProtectionGroup expectedPG = createProtectionGroup(1L, "edu.northwestern.bioinformatics.studycalendar.domain.Site.1");

        expect(authorizationManager.getPGByName("edu.northwestern.bioinformatics.studycalendar.domain.Site.1")).andReturn(expectedPG);
        authorizationManager.assignProtectionGroupsToUsers(expectedCoordinators,
                                                           expectedPG,
                                                           SiteService.PARTICIPANT_COORDINATOR_ACCESS_ROLE);
        replayMocks();

        service.assignParticipantCoordinators(expectedSite, expectedCoordinators);
        verifyMocks();
    }


    public void testRemoveSiteCoordinators() throws Exception {
        Site expectedSite = setId(1, createSite("northwestern"));

        List<String> expectedCoordinators = Collections.singletonList("john");

        ProtectionGroup expectedPG = createProtectionGroup(1L, "edu.northwestern.bioinformatics.studycalendar.domain.Site.1");

        expect(authorizationManager.getPGByName("edu.northwestern.bioinformatics.studycalendar.domain.Site.1")).andReturn(expectedPG);
        authorizationManager.removeProtectionGroupUsers(expectedCoordinators, expectedPG);
        replayMocks();

        service.removeSiteCoordinators(expectedSite, expectedCoordinators);
        verifyMocks();
    }


    public void testRemoveParticipantCoordinators() throws Exception {
        Site expectedSite = setId(1, createSite("northwestern"));

        List<String> expectedCoordinators = Collections.singletonList("john");

        ProtectionGroup expectedPG = createProtectionGroup(1L, "edu.northwestern.bioinformatics.studycalendar.domain.Site.1");

        expect(authorizationManager.getPGByName("edu.northwestern.bioinformatics.studycalendar.domain.Site.1")).andReturn(expectedPG);
        authorizationManager.removeProtectionGroupUsers(expectedCoordinators, expectedPG);
        replayMocks();

        service.removeParticipantCoordinators(expectedSite, expectedCoordinators);
        verifyMocks();
    }

    public void testRemoveResearchAssociates() throws Exception {
        Site expectedSite = setId(1, createSite("northwestern"));

        List<String> expectedCoordinators = Collections.singletonList("john");

        ProtectionGroup expectedPG = createProtectionGroup(1L, "edu.northwestern.bioinformatics.studycalendar.domain.Site.1");

        expect(authorizationManager.getPGByName("edu.northwestern.bioinformatics.studycalendar.domain.Site.1")).andReturn(expectedPG);
        authorizationManager.removeProtectionGroupUsers(expectedCoordinators, expectedPG);
        replayMocks();

        service.removeResearchAssociates(expectedSite, expectedCoordinators);
        verifyMocks();
    }

    public void testGetSiteCoordinatorLists() throws Exception {
        Site expectedSite = setId(1, createSite("northwestern"));

        User myUser = createUser(1L);
        Map<String, List<User>> expectedUserMap = createUserMap(myUser);

        expect(authorizationManager.getUserPGLists(SiteService.SITE_COORDINATOR_GROUP, "edu.northwestern.bioinformatics.studycalendar.domain.Site.1"))
                .andReturn(expectedUserMap);
        replayMocks();

        Map<String, List> actualUserMap = service.getSiteCoordinatorLists(expectedSite);
        verifyMocks();

        assertEquals(expectedUserMap.size(), actualUserMap.size());
        assertEquals(expectedUserMap.get(StudyCalendarAuthorizationManager.ASSIGNED_USERS).size(),
                        actualUserMap.get(StudyCalendarAuthorizationManager.ASSIGNED_USERS).size());
        assertEquals(expectedUserMap.get(StudyCalendarAuthorizationManager.ASSIGNED_USERS).get(0),
                        actualUserMap.get(StudyCalendarAuthorizationManager.ASSIGNED_USERS).get(0));

    }

    public void testGetParticipantCoordinatorLists() throws Exception {
        Site expectedSite = setId(1, createSite("northwestern"));

        User myUser = createUser(1L);
        Map<String, List<User>> expectedUserMap = createUserMap(myUser);

        expect(authorizationManager.getUserPGLists(SiteService.PARTICIPANT_COORDINATOR_GROUP, "edu.northwestern.bioinformatics.studycalendar.domain.Site.1"))
                .andReturn(expectedUserMap);
        replayMocks();

        Map<String, List> actualUserMap =  service.getParticipantCoordinatorLists(expectedSite);
        verifyMocks();

        assertEquals(expectedUserMap.size(), actualUserMap.size());
        assertEquals(expectedUserMap.get(StudyCalendarAuthorizationManager.ASSIGNED_USERS).size(),
                        actualUserMap.get(StudyCalendarAuthorizationManager.ASSIGNED_USERS).size());
        assertEquals(expectedUserMap.get(StudyCalendarAuthorizationManager.ASSIGNED_USERS).get(0),
                        actualUserMap.get(StudyCalendarAuthorizationManager.ASSIGNED_USERS).get(0));
    }

    public void testGetSitesForParticipantCoordinators() {
        ProtectionGroup expectedPG = createProtectionGroup(1L, "edu.northwestern.bioinformatics.studycalendar.domain.StudySite.1");
        List<ProtectionGroup> expectedPGs = Collections.singletonList(expectedPG);

        StudySite expectedStudySite = createStudySite("new york", 1);

        Set<Site> expectedSites = Collections.singleton(expectedStudySite.getSite());

        expect(authorizationManager.getStudySitePGsForUser("john")).andReturn(expectedPGs);
        expect(DomainObjectTools
                .loadFromExternalObjectId("edu.northwestern.bioinformatics.studycalendar.domain.StudySite.1", studySiteDao))
                .andReturn(expectedStudySite);
        replayMocks();

        Collection<Site> actualSites = service.getSitesForParticipantCoordinator("john");
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

    public void testAssignResearchAssociate() throws Exception {
        Site expectedSite = setId(1, createSite("northwestern"));

        List<String> expectedResearchAssociates = Collections.singletonList("john");

        ProtectionGroup expectedPG = createProtectionGroup(1L, "edu.northwestern.bioinformatics.studycalendar.domain.Site.1");

        expect(authorizationManager.getPGByName("edu.northwestern.bioinformatics.studycalendar.domain.Site.1")).andReturn(expectedPG);
        authorizationManager.assignProtectionGroupsToUsers(expectedResearchAssociates,
                                                           expectedPG,
                                                           SiteService.RESEARCH_ASSOCIATE_ACCESS_ROLE);
        replayMocks();

        service.assignSiteResearchAssociates(expectedSite, expectedResearchAssociates);
        verifyMocks();
    }

    public void testRemoveAllSiteRoles() throws Exception{
        Site expectedSite = setId(1, createSite("northwestern"));

        List<String> expectedCoordinators = Collections.singletonList("john");

        ProtectionGroup expectedPG = createProtectionGroup(1L, "edu.northwestern.bioinformatics.studycalendar.domain.Site.1");

        expect(authorizationManager.getPGByName("edu.northwestern.bioinformatics.studycalendar.domain.Site.1")).andReturn(expectedPG).times(3);
        authorizationManager.removeProtectionGroupUsers(expectedCoordinators, expectedPG);
        authorizationManager.removeProtectionGroupUsers(expectedCoordinators, expectedPG);
        authorizationManager.removeProtectionGroupUsers(expectedCoordinators, expectedPG);
        replayMocks();

        service.removeAllSiteRoles(expectedSite, expectedCoordinators);
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

    private User createUser(Long aUserId) {
        User myUser = new User();
        myUser.setUserId(aUserId);
        return myUser;
    }

    private Map<String, List<User>> createUserMap(User aUser) {
        List<User> myUserList       = Collections.singletonList(aUser);
        return Collections.singletonMap(StudyCalendarAuthorizationManager.ASSIGNED_USERS, myUserList);
    }
}
