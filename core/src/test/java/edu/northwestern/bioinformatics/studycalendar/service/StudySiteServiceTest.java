package edu.northwestern.bioinformatics.studycalendar.service;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.StudyCalendarAuthorizationManager;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSite;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.StudySiteConsumer;
import static org.easymock.EasyMock.expect;
import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.checkOrder;

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
    private StudySiteConsumer studySiteConsumer;
    private StudySiteDao studySiteDao;

    protected void setUp() throws Exception {
        super.setUp();

        siteService = registerMockFor(SiteService.class);
        studySiteDao = registerDaoMockFor(StudySiteDao.class);
        studySiteConsumer = registerMockFor(StudySiteConsumer.class);
        authorizationManager = registerMockFor(StudyCalendarAuthorizationManager.class);

        service = new StudySiteService();
        service.setSiteService(siteService);
        service.setStudySiteDao(studySiteDao);
        service.setStudySiteConsumer(studySiteConsumer);
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
        expect(siteService.getAll()).andReturn(asList(nu, mayo));
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

        Site expectedAvailableSite0 = createNamedInstance("Mayo Clinic", Site.class);
        Site expectedAvailableSite1 = createNamedInstance("Northwestern Clinic", Site.class);
        expect(siteService.getAll()).andReturn(asList(expectedAvailableSite0, expectedAvailableSite1));
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

    public void testRefreshAssociatedSites() {
        Site uicTemplate = new Site();
        uicTemplate.setAssignedIdentifier("UIC");   // Sites returned from the provider only have assignedIdentifier set
        StudySite fromProvider = new StudySite(all999, uicTemplate);

        Site uic = createSite("UIC", "UIC");
        StudySite fromService = new StudySite(all999, uic); // Site service will return sites w/ name and assigned identifier

        expect(studySiteConsumer.refresh(all999)).andReturn(asList(fromProvider, studySite1));
        expect(siteService.getAll()).andReturn(asList(nu, uic, mayo));
        studySiteDao.save(fromService);

        replayMocks();
        
        List<Site> actual = service.refreshAssociatedSites(all999);
        verifyMocks();

        assertEquals("Wrong number of sites", 3, actual.size());

        assertContains(actual, nu);
        assertContains(actual, uic);
        assertContains(actual, mayo);
    }

    public void testAssignTemplateToSites() throws Exception {
        Study study = createNamedInstance("sldfksdfjk", Study.class);
        Site site1 = createNamedInstance("aaa", Site.class);
        Site site2 = createNamedInstance("bbb", Site.class);
        List<Site> sitesTest = asList(site1, site2);

        checkOrder(studySiteDao, true);

        studySiteDao.save(studySiteEq(study, site1));
        studySiteDao.save(studySiteEq(study, site2));

        replayMocks();
        service.assignStudyToSites(study, sitesTest);
        verifyMocks();
    }

    public void testAssignTemplateToSitesRequiresStudy() throws Exception {
        Site site1 = createNamedInstance("aaa", Site.class);
        List<Site> sitesTest = asList(site1);
        try {
            service.assignStudyToSites(null, sitesTest);
            fail("Expected IllegalArgumentException. Null object is passed instead of study ");
        } catch(IllegalArgumentException ise) {
            assertEquals(TemplateService.STUDY_IS_NULL, ise.getMessage());
        }
    }

    public void testAssignTemplateToSitesRequiresSitesList() throws Exception {
        Study study = createNamedInstance("sldfksdfjk", Study.class);
        try {
            service.assignStudyToSites(study, null);
            fail("Expected IllegalArgumentException. Null object is passed instead of sitesTest ");
        } catch(IllegalArgumentException ise) {
            assertEquals(StudySiteService.SITES_LIST_IS_NULL, ise.getMessage());
        }
    }

    public void testCannotRemoveStudySiteWithAssociatedAssignments() throws Exception {
        Study study = createNamedInstance("ECOG 1234", Study.class);
        Site site1 = setId(1, createNamedInstance("Mayo", Site.class));
        Site site2 = createNamedInstance("Dartmouth", Site.class);
        StudySite notInUse = setId(10, createStudySite(study, site1));
        StudySite inUse = setId(11, createStudySite(study, site2));
        inUse.getStudySubjectAssignments().add(new StudySubjectAssignment());

        authorizationManager.removeProtectionGroup(DomainObjectTools.createExternalObjectId(notInUse));
        studySiteDao.delete(notInUse);
        replayMocks();

        service.removeStudyFromSites(study, asList(site1, site2));
        verifyMocks();

        List<Site> remainingSites = study.getSites();
        assertEquals("Removable site not removed", 1, remainingSites.size());
        assertEquals("Wrong site retained", "Dartmouth", remainingSites.get(0).getName());
    }

    ////// CUSTOM MATCHERS
    private static StudySite studySiteEq(Study expectedStudy, Site expectedSite) {
        EasyMock.reportMatcher(new StudySiteMatcher(expectedStudy, expectedSite));
        return null;
    }

    private static class StudySiteMatcher implements IArgumentMatcher {
        private Study expectedStudy;
        private Site expectedSite;

        public StudySiteMatcher(Study expectedStudy, Site expectedSite) {
            this.expectedStudy = expectedStudy;
            this.expectedSite = expectedSite;
        }

        public boolean matches(Object object) {
            StudySite actual = (StudySite) object;

            if (expectedStudy.equals(actual.getStudy())) {
                if (expectedSite.equals(actual.getSite())) {
                    return true;
                }
            }

            return false;
        }

        public void appendTo(StringBuffer sb) {
            sb.append("StudySite with study=").append(expectedStudy).append(" and site=").append(expectedSite);
        }
    }
}
