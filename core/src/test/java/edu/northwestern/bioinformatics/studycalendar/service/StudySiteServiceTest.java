package edu.northwestern.bioinformatics.studycalendar.service;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.StudyCalendarAuthorizationManager;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSite;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createStudySite;
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
    private StudySite nu_nu123, nu_all999, mayo_all999;
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

        nu_nu123 = createStudySite(nu123, nu);
        nu_all999 = createStudySite(all999, nu);
        mayo_all999 = createStudySite(all999, mayo);

        studySites = asList(nu_nu123, nu_all999, mayo_all999);

        user  = createNamedInstance("John", User.class);
        UserRole role = createUserRole(user, Role.SUBJECT_COORDINATOR, nu, mayo);
        role.setStudySites(studySites);
        user.addUserRole(role);

    }

    public void testGetAllStudySitesForSubjectCoordinator() {
        List<StudySite> actualStudySites = service.getAllStudySitesForSubjectCoordinator(user);
        assertEquals("Wrong number of Study Sites", studySites.size(), actualStudySites.size());
        assertEquals("Wrong Study Site", nu_nu123, actualStudySites.get(0));
        assertEquals("Wrong Study Site", nu_all999, actualStudySites.get(1));
        assertEquals("Wrong Study Site", mayo_all999, actualStudySites.get(2));
    }

    public void testGetStudySitesForSubjectCoordinatorFromSite() {
        List<StudySite> actualStudySites = service.getStudySitesForSubjectCoordinator(user, mayo);
        assertEquals("Wrong number of Study Sites", 1, actualStudySites.size());
        assertEquals("Wrong Study Site", mayo_all999, actualStudySites.get(0));
    }

    public void testGetStudySitesForSubjectCoordinatorFromStudy() {
        List<StudySite> actualStudySites = service.getStudySitesForSubjectCoordinator(user, all999);
        assertEquals("Wrong number of Study Sites", 2, actualStudySites.size());
        assertEquals("Wrong Study Site", nu_all999, actualStudySites.get(0));
        assertEquals("Wrong Study Site", mayo_all999, actualStudySites.get(1));
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
        Site uicSkel = createSite(null, "UIC"); // Sites returned from the consumer only with assignedIdentifier
        StudySite fromProvider = new StudySite(all999, uicSkel);
        fromProvider.setProvider("alpha");

        Site uic = createSite("UIC North", "UIC");    // Sites returned from the service with assignedIdentifier and name

        expect(studySiteConsumer.refresh(all999)).andReturn(asList(
            fromProvider, nu_all999
        ));

        expect(siteService.getAll()).andReturn(asList(
            nu, uic, mayo
        ));

        StudySite fullStudySite = new StudySite(all999, uic);
        fullStudySite.setProvider("alpha");
        studySiteDao.save(providedStudySiteEq(fullStudySite));

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

    public void testRefreshStudySitesUsingStudy() throws Exception {
        Site uicSkel = createSite(null, "UIC"); // Sites returned from the consumer only with assignedIdentifier
        Study all999Skel = new Study();
        all999Skel.setAssignedIdentifier("ALL999");

        Site uic = createSite("UIC North", "UIC");    // Sites returned from the service with assignedIdentifier and name

        expect(studySiteConsumer.refresh(all999)).andReturn(asList(
            createProvidedStudySite(all999Skel, uicSkel, "alpha"),
            nu_all999
        ));

        expect(siteService.getAll()).andReturn(asList(
            nu, uic, mayo
        ));

        StudySite uic_all999 = createProvidedStudySite(all999Skel, uicSkel, "alpha");
        studySiteDao.save(providedStudySiteEq(uic_all999));

        replayMocks();

        List<StudySite> actual = service.refreshStudySites(all999);
        verifyMocks();

        assertEquals("Wrong number of sites", 3, actual.size());

        assertContains(actual, uic_all999);
        assertContains(actual, nu_all999);
        assertContains(actual, mayo_all999);
    }

    private StudySite createProvidedStudySite(Study study, Site site, String providerName) {
        StudySite studySite = createStudySite(study,  site);
        studySite.setProvider(providerName);
        return studySite;
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

    private static StudySite providedStudySiteEq(StudySite expected) {
        EasyMock.reportMatcher(new ProvidedStudySiteMatcher(expected));
        return null;
    }

    private static class ProvidedStudySiteMatcher implements IArgumentMatcher {
        private StudySite expected;

        public ProvidedStudySiteMatcher(StudySite expected) {
            this.expected = expected;
        }

        public boolean matches(Object object) {
            StudySite actual = (StudySite) object;

            if (!expected.equals(actual)) {
                return false;
            }

            if (expected.getProvider() != null ? !expected.getProvider().equals(actual.getProvider()) : actual.getProvider() != null) {
                return false;
            }

            return true;
        }

        public void appendTo(StringBuffer sb) {
            sb.append("StudySite =").append(expected.toString()).append("Provider =").append(expected.getProvider());
        }
    }
}
