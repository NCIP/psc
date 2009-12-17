package edu.northwestern.bioinformatics.studycalendar.service;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.StudyCalendarAuthorizationManager;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import static org.easymock.EasyMock.expect;

import static java.util.Arrays.asList;
import java.util.List;
import java.util.Map;

public class StudySiteServiceTest extends StudyCalendarTestCase {
    private StudySiteService service;
    private SiteService siteService;
    private StudyCalendarAuthorizationManager authorizationManager;

    private User user;
    private Site nu, mayo;
    private List<StudySite> studySites;
    private Study nu123, all999;
    private StudySite studySite0, studySite1, studySite2;

    protected void setUp() throws Exception {
        super.setUp();

        siteService = registerMockFor(SiteService.class);
        authorizationManager = registerMockFor(StudyCalendarAuthorizationManager.class);

        service = new StudySiteService();
        service.setSiteService(siteService);
        service.setStudyCalendarAuthorizationManager(authorizationManager);

        nu = createNamedInstance("Northwestern", Site.class);
        mayo = createNamedInstance("Mayo Clinic" , Site.class);

        nu123 = createNamedInstance("NU123", Study.class);
        all999 = createNamedInstance("ALL999", Study.class);

        studySite0 = createStudySite(nu123, nu);
        studySite1 = createStudySite(all999, nu);
        studySite2 = createStudySite(all999, mayo);

        studySites = asList(studySite0, studySite1, studySite2);

        user  = createNamedInstance("John", User.class);
        UserRole role = createUserRole(user, Role.SUBJECT_COORDINATOR, nu, mayo);
        role.setStudySites(studySites);
        user.addUserRole(role);

    }

    public void testGetAllStudySitesForSubjectCoordinator() {
        List<StudySite> actualStudySites = service.getAllStudySitesForSubjectCoordinator(user);
        assertEquals("Wrong number of Study Sites", studySites.size(), actualStudySites.size());
        assertEquals("Wrong Study Site", studySite0, actualStudySites.get(0));
        assertEquals("Wrong Study Site", studySite1, actualStudySites.get(1));
        assertEquals("Wrong Study Site", studySite2, actualStudySites.get(2));
    }

    public void testGetStudySitesForSubjectCoordinatorFromSite() {
        List<StudySite> actualStudySites = service.getStudySitesForSubjectCoordinator(user, mayo);
        assertEquals("Wrong number of Study Sites", 1, actualStudySites.size());
        assertEquals("Wrong Study Site", studySite2, actualStudySites.get(0));
    }

    public void testGetStudySitesForSubjectCoordinatorFromStudy() {
        List<StudySite> actualStudySites = service.getStudySitesForSubjectCoordinator(user, all999);
        assertEquals("Wrong number of Study Sites", 2, actualStudySites.size());
        assertEquals("Wrong Study Site", studySite1, actualStudySites.get(0));
        assertEquals("Wrong Study Site", studySite2, actualStudySites.get(1));
    }
    
    public void testGetSiteLists() throws Exception {
        ProtectionGroup nuPG = pg("edu.northwestern.bioinformatics.studycalendar.domain.Site.1");
        ProtectionGroup mayoPG = pg("edu.northwestern.bioinformatics.studycalendar.domain.Site.2");

        expect(authorizationManager.getSites()).andReturn(asList(nuPG, mayoPG));

        expect(siteService.getById(1)).andReturn(nu);
        expect(siteService.getById(2)).andReturn(mayo);
        replayMocks();

        Map<String, List<Site>> results = service.getSiteLists(nu123);
        verifyMocks();

        List<Site> assigned = results.get(StudyCalendarAuthorizationManager.ASSIGNED_PGS);
        assertEquals(1, assigned.size());
        assertEquals("Wrong Site", nu, assigned.get(0));

        List<Site> available = results.get(StudyCalendarAuthorizationManager.AVAILABLE_PGS);
        assertEquals(1, available.size());
        assertEquals("Wrong Site", mayo, available.get(0));

    }

    public void testGetSiteListsRequiresStudy() throws Exception {
        try {
            service.getSiteLists(null);
            fail("Expected IllegalArgumentException. Null object is passed instead of studyTemplate ");
        } catch(IllegalArgumentException ise) {
            assertEquals(TemplateService.STUDY_IS_NULL, ise.getMessage());
        }
    }

    public void testGetSitesListsWithSameSiteAvailableAndAssigned() throws Exception {
        Study study = createNamedInstance("Mayo Study", Study.class);
        study.addSite(createNamedInstance("Mayo Clinic", Site.class));

        ProtectionGroup expectedAvailableSitePG0 =
                createProtectionGroup(1L, "edu.northwestern.bioinformatics.studycalendar.domain.Site.0");
        ProtectionGroup expectedAvailableSitePG1 =
                createProtectionGroup(2L, "edu.northwestern.bioinformatics.studycalendar.domain.Site.1");
        List<ProtectionGroup> exptectedAvailableSitePGs = asList(expectedAvailableSitePG0, expectedAvailableSitePG1);
        expect(authorizationManager.getSites()).andReturn(exptectedAvailableSitePGs);
        Site expectedAvailableSite0 = createNamedInstance("Mayo Clinic", Site.class);
        Site expectedAvailableSite1 = createNamedInstance("Northwestern Clinic", Site.class);
        expect(siteService.getById(0)).andReturn(expectedAvailableSite0);
        expect(siteService.getById(1)).andReturn(expectedAvailableSite1);
        replayMocks();

        Map<String, List<Site>> assignedAndAvailableSites = service.getSiteLists(study);
        verifyMocks();

        assertEquals("There should be assigned and available sites", 2, assignedAndAvailableSites.size());

        List<Site> actualAssignedSites = assignedAndAvailableSites.get(StudyCalendarAuthorizationManager.ASSIGNED_PGS);
        assertEquals("Wrong number of assigned sites", 1, actualAssignedSites.size());
        assertEquals("Wrong assigned site", "Mayo Clinic", actualAssignedSites.get(0).getName());

        List<Site> actualAvailableSites = assignedAndAvailableSites.get(StudyCalendarAuthorizationManager.AVAILABLE_PGS);
        assertEquals("Wrong number of available sites", 1, actualAvailableSites.size());
        assertEquals("Wrong available site", "Northwestern Clinic", actualAvailableSites.get(0).getName());
    }

//    public void testRefreshAssociatedSites() {
//        List<Study> actual = service.refreshAssociatedSites()
//    }

    private ProtectionGroup pg(String name) {
        ProtectionGroup mayoPG = new ProtectionGroup();
        mayoPG.setProtectionGroupName(name);
        return mayoPG;
    }
}
