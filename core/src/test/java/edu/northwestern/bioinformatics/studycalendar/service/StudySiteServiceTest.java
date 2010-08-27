package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.StudySiteConsumer;
import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;

import java.util.Collection;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createUserRole;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.addSecondaryIdentifier;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSite;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createStudySite;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import static java.util.Arrays.*;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.checkOrder;

public class StudySiteServiceTest extends StudyCalendarTestCase {
    private StudySiteService service;
    private SiteService siteService;

    private User user;
    private Site nu, mayo;
    private List<StudySite> studySites;
    private Study nu123, all999;
    private StudySite nu_nu123, nu_all999, mayo_all999;
    private StudySiteConsumer studySiteConsumer;
    private StudySiteDao studySiteDao;
    private StudyDao studyDao;

    protected void setUp() throws Exception {
        super.setUp();

        studyDao = registerDaoMockFor(StudyDao.class);
        siteService = registerMockFor(SiteService.class);
        studySiteDao = registerDaoMockFor(StudySiteDao.class);
        studySiteConsumer = registerMockFor(StudySiteConsumer.class);

        service = new StudySiteService();
        service.setStudyDao(studyDao);
        service.setSiteService(siteService);
        service.setStudySiteDao(studySiteDao);
        service.setStudySiteConsumer(studySiteConsumer);

        nu = createSite("Northwestern", "NU", "alpha");
        mayo = createSite("Mayo Clinic", "MAYO" , "alpha");

        nu123 = createStudy("NU123", "alpha");
        all999 = createStudy("ALL999", "alpha");

        nu_nu123 = createStudySite(nu123, nu, "alpha");
        nu_all999 = createStudySite(all999, nu, "alpha");
        mayo_all999 = createStudySite(all999, mayo, "alpha");

        studySites = asList(nu_nu123, nu_all999, mayo_all999);

        user  = createNamedInstance("John", User.class);
        UserRole role = createUserRole(user, Role.SUBJECT_COORDINATOR, nu, mayo);
        role.setStudySites(studySites);
        user.addUserRole(role);
    }

    private Study createStudy(String assignedIdentifier, String provider) {
        Study s = createNamedInstance(assignedIdentifier, Study.class);
        s.setProvider(provider);
        return s;
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

    public void testRefreshAssociatedSites() {
        Site uicSkel = createSite(null, "UIC"); // Sites returned from the consumer only with assignedIdentifier

        expect(studySiteConsumer.refreshSites(asList(all999))).andReturn(asList(asList(
            providedStudySite(all999, uicSkel, "alpha"),
            nu_all999
        )));

        Site uic = createSite("UIC North", "UIC", "alpha");
        expect(siteService.getAll()).andReturn(asList(
            nu, uic, mayo
        ));

        StudySite uic_all999 = providedStudySite(all999, uic, "alpha");
        studySiteDao.save(providedStudySiteEq(
            uic_all999)
        );

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
            assertEquals("Study is null", ise.getMessage());
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

        studySiteDao.delete(notInUse);
        replayMocks();

        service.removeStudyFromSites(study, asList(site1, site2));
        verifyMocks();

        List<Site> remainingSites = study.getSites();
        assertEquals("Removable site not removed", 1, remainingSites.size());
        assertEquals("Wrong site retained", "Dartmouth", remainingSites.get(0).getName());
    }

    public void testRefreshStudySitesForStudyReturningExistingStudySites() throws Exception {
        Site nuProvided = createSite(null, "Northwestern University"); // Provided Instances only have assigned identifier populated

        expect(studySiteConsumer.refreshSites(asList(nu123))).andReturn(asList(asList(
            providedStudySite(nu123, nuProvided, "alpha")
        )));

        expect(siteService.getAll()).andReturn(asList(
            nu, mayo
        ));

        replayMocks();

        List<StudySite> actual = service.refreshStudySitesForStudy(nu123);
        verifyMocks();

        assertEquals("Wrong number of sites", 1, actual.size());

        assertContains(actual, nu_nu123);
    }

    public void testRefreshStudySitesForStudyReturningNewStudySites() throws Exception {
        Site nuProvided = createSite(null, "NU"); // Provided Instances only have assigned identifier populated
        Site mayoProvided = createSite(null, "MAYO");

        expect(studySiteConsumer.refreshSites(asList(nu123))).andReturn(asList(asList(
            providedStudySite(nu123, nuProvided, "alpha"),
            providedStudySite(nu123, mayoProvided, "alpha")
        )));

        expect(siteService.getAll()).andReturn(asList(
            nu, mayo
        ));

        StudySite mayo_nu123 = providedStudySite(nu123, mayo, "alpha");
        studySiteDao.save(providedStudySiteEq(
            mayo_nu123
        ));

        replayMocks();

        List<StudySite> actual = service.refreshStudySitesForStudy(nu123);
        verifyMocks();

        assertEquals("Wrong number of sites", 2, actual.size());

        assertContains(actual, nu_nu123);
        assertContains(actual, mayo_nu123);
    }

    public void testRefreshStudySitesForStudyReturningNewStudySiteWithDiffProvider() throws Exception {
        Site uicProvided = createSite(null, "UIC"); // Provided Instances only have assigned identifier populated

        expect(studySiteConsumer.refreshSites(asList(nu123))).andReturn(asList(asList(
            providedStudySite(nu123, uicProvided, "alpha")
        )));


        Site uic = createSite("UIC North", "UIC", "beta");
        expect(siteService.getAll()).andReturn(asList(
            nu, uic
        ));

        replayMocks();

        List<StudySite> actual = service.refreshStudySitesForStudy(nu123);
        verifyMocks();

        assertEquals("Wrong number of sites", 1, actual.size());

        assertContains(actual, nu_nu123);
    }

    public void testRefreshStudySitesForStudyWithConsumerReturningNull() throws Exception {
        expect(studySiteConsumer.refreshSites(asList(nu123))).andReturn(asList((List<StudySite>) null));

        expect(siteService.getAll()).andReturn(asList(
            nu, mayo
        ));

        replayMocks();

        List<StudySite> actual = service.refreshStudySitesForStudy(nu123);
        verifyMocks();

        assertEquals("Wrong number of sites", 1, actual.size());
        assertContains(actual, nu_nu123);
    }

    public void testRefreshStudySitesForSiteWithConsumerReturningNull() throws Exception {
        expect(studySiteConsumer.refreshStudies(asList(nu))).andReturn(asList((List<StudySite>) null));

        expect(studyDao.getAll()).andReturn(asList(
            all999, nu123
        ));

        replayMocks();

        List<StudySite> actual = service.refreshStudySitesForSite(nu);
        verifyMocks();

        assertEquals("Wrong number of sites", 2, actual.size());
        assertContains(actual, nu_nu123);
        assertContains(actual, nu_all999);
    }

    public void testRefreshStudySitesForSiteAndReceivingExistingStudySite() throws Exception {
        Study nu123Provided = new Study();
        addSecondaryIdentifier(nu123Provided , "Mock Provider Identifier", "NU123");

        expect(studySiteConsumer.refreshStudies(asList(nu))).andReturn(asList(asList(
            providedStudySite(nu123Provided , nu, "alpha")
        )));

        expect(studyDao.getAll()).andReturn(asList(
            all999, nu123
        ));

        replayMocks();

        List<StudySite> actual = service.refreshStudySitesForSite(nu);
        verifyMocks();

        assertEquals("Wrong number of sites", 2, actual.size());

        assertContains(actual, nu_nu123);
        assertContains(actual, nu_all999);
    }

    public void testRefreshStudySitesForSiteAndReceivingNewStudySite() throws Exception {
        Study nu123Provided = createNamedInstance("NU123", Study.class);
        addSecondaryIdentifier(nu123Provided, "Mock Provider Identifier", "NU123");

        Study wo222Provided = createNamedInstance("WO222 Study", Study.class);
        addSecondaryIdentifier(wo222Provided, "Mock Provider Identifier", "WO222");

        expect(studySiteConsumer.refreshStudies(asList(nu))).andReturn(asList(asList(
            providedStudySite(nu123Provided , nu, "alpha"),
            providedStudySite(wo222Provided, nu, "alpha")
        )));

        Study wo222 = createStudy("WO222 Study", "alpha");
        addSecondaryIdentifier(wo222, "Mock Provider Identifier", "WO222");

        expect(studyDao.getAll()).andReturn(asList(
            wo222, nu123
        ));

        StudySite nu_wo222 = providedStudySite(wo222, nu, "alpha");
        studySiteDao.save(providedStudySiteEq(
            nu_wo222
        ));

        replayMocks();

        List<StudySite> actual = service.refreshStudySitesForSite(nu);
        verifyMocks();

        assertEquals("Wrong number of sites", 3, actual.size());
        assertContains(actual, nu_nu123);
        assertContains(actual, nu_all999);
        assertContains(actual, nu_wo222);

        assertEquals("Wrong number of sites", 3, nu.getStudySites().size());
        assertContains(nu.getStudySites(), nu_nu123);
        assertContains(nu.getStudySites(), nu_all999);
        assertContains(nu.getStudySites(), nu_wo222);
    }

    public void testRefreshStudySitesForSiteAndReceivingNewStudySiteWithDiffProvider() throws Exception {
        Study nu123Provided = createNamedInstance("NU123", Study.class);
        addSecondaryIdentifier(nu123Provided, "Mock Provider Identifier", "NU123");

        Study wo222Provided = createNamedInstance("WO222 Study", Study.class);
        addSecondaryIdentifier(wo222Provided, "Mock Provider Identifier", "WO222");

        expect(studySiteConsumer.refreshStudies(asList(nu))).andReturn(asList(asList(
            providedStudySite(nu123Provided , nu, "alpha"),
            providedStudySite(wo222Provided, nu, "beta")
        )));

        Study wo222 = createStudy("WO222 Study", "alpha");
        addSecondaryIdentifier(wo222, "Mock Provider Identifier", "WO222");

        expect(studyDao.getAll()).andReturn(asList(
            wo222, nu123
        ));

        replayMocks();

        List<StudySite> actual = service.refreshStudySitesForSite(nu);
        verifyMocks();

        assertEquals("Wrong number of sites", 2, actual.size());
        assertContains(actual, nu_nu123);
        assertContains(actual, nu_all999);

        assertEquals("Wrong number of sites", 2, nu.getStudySites().size());
        assertContains(nu.getStudySites(), nu_nu123);
        assertContains(nu.getStudySites(), nu_all999);
    }

    public void testResolveStudySiteWhenStudyNotFound() throws Exception {
        expect(studyDao.getByAssignedIdentifier("NU123")).andReturn(null);
        replayMocks();
        try {
            service.resolveStudySite(nu_nu123);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("Study 'NU123' not found. Please define a study that exists.", scve.getMessage());
        }
    }

    public void testResolveStudySiteWhenSiteNotFound() throws Exception {
        expect(studyDao.getByAssignedIdentifier("NU123")).andReturn(nu123);
        expect(siteService.getByAssignedIdentifier("NU")).andReturn(null);
        replayMocks();
        try {
            service.resolveStudySite(nu_nu123);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("Site 'NU' not found. Please define a site that exists.", scve.getMessage());
        }
    }

    public void testResolveStudySiteWhenNewStudySite() throws Exception {
        expect(studyDao.getByAssignedIdentifier("NU123")).andReturn(nu123);
        expect(siteService.getByAssignedIdentifier("NU")).andReturn(nu);
        StudySite newStudySite = new StudySite(nu123, nu);
        replayMocks();
        StudySite actual = service.resolveStudySite(newStudySite);
        verifyMocks();

        assertSame("Study is not same", nu123, actual.getStudy());
        assertSame("Site is not same", nu, actual.getSite());
    }
    
    public void testResolveStudySiteWhenExistingStudySite() throws Exception {
        expect(studyDao.getByAssignedIdentifier("NU123")).andReturn(nu123);
        expect(siteService.getByAssignedIdentifier("NU")).andReturn(nu);
        replayMocks();
        StudySite actual = service.resolveStudySite(nu_nu123);
        verifyMocks();
        assertSame("StudySite is not same", nu_nu123, actual);
    }

    public void testProvidedStudyMatcherUsingSameProviders() {
        Study s1 = new Study();
        addSecondaryIdentifier(s1, "foo", "bar");
        s1.setProvider("alpha");

        Study s2 = new Study();
        addSecondaryIdentifier(s2, "foo", "bar");
        s2.setProvider("alpha");

        boolean result = StudySiteService.StudySecondaryIdentifierMatcher.instance().match(s1, s2);
        assertTrue("Study Sites should match", result);
    }

   
    public void testProvidedStudyMatcherUsingSameStudySecondaryIdentifiers() {
        Study s1 = new Study();
        addSecondaryIdentifier(s1, "foo", "bar");

        Study s2 = new Study();
        addSecondaryIdentifier(s2, "foo", "bar");

        boolean result = StudySiteService.StudySecondaryIdentifierMatcher.instance().match(s1, s2);
        assertTrue("Study Sites should match", result);
    }

    public void testProvidedStudyMatcherUsingDiffStudySecondaryIdentifiers() {
        Study s1 = new Study();
        addSecondaryIdentifier(s1, "foo", "bar");

        Study s2 = new Study();
        addSecondaryIdentifier(s2, "foo", "baz");

        boolean result = StudySiteService.StudySecondaryIdentifierMatcher.instance().match(s1, s2);
        assertFalse("Study Sites should match", result);
    }

    class TestMatcher implements StudySiteService.CollectionMatcher {
        public boolean match(Object o1, Object o2) {
            return o1.equals(o2);
        }
    }

    public void testCollectionUtilsPlusMatchWithMatch() {
        Collection result = StudySiteService.CollectionUtilsPlus.matching(asList("foo"), asList("foo"), new TestMatcher());
        assertEquals("Wrong size", 1, result.size());
    }

    public void testCollectionUtilsPlusMatchWithNoMatch() {
        Collection result = StudySiteService.CollectionUtilsPlus.matching(asList("foo"), asList("bar"), new TestMatcher());
        assertEquals("Wrong size", 0, result.size());
    }

    ////// HELPER METHODS
    private StudySite providedStudySite(Study study, Site site, String providerName) {
        StudySite studySite = new StudySite(study,  site);
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
