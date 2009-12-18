package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.StudyCalendarAuthorizationManager;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.DomainObjectTools.createExternalObjectId;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createProtectionGroup;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.StudySiteConsumer;
import edu.nwu.bioinformatics.commons.StringUtils;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.checkOrder;

import java.util.*;
import static java.util.Arrays.asList;

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
    private SiteDao siteDao;
    private StudyDao studyDao;

    protected void setUp() throws Exception {
        super.setUp();

        siteDao = registerDaoMockFor(SiteDao.class);
        studyDao = registerDaoMockFor(StudyDao.class);
        siteService = registerMockFor(SiteService.class);
        studySiteDao = registerDaoMockFor(StudySiteDao.class);
        studySiteConsumer = registerMockFor(StudySiteConsumer.class);
        authorizationManager = registerMockFor(StudyCalendarAuthorizationManager.class);

        service = new StudySiteService();
        service.setSiteDao(siteDao);
        service.setStudyDao(studyDao);
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
        expectAuthManagerToReturnSites(nu, mayo);
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

    public void testRefreshAssociatedSites() {
        Site uic = createNamedInstance("UIC" , Site.class);
        StudySite provided = createStudySite(all999, uic);

        expect(studySiteConsumer.refresh(all999)).andReturn(asList(provided, studySite1));

        expectAuthManagerToReturnSites(nu, mayo);
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
        service.assignTemplateToSites(study, sitesTest);
        verifyMocks();
    }

    public void testAssignTemplateToSitesRequiresStudy() throws Exception {
        Site site1 = createNamedInstance("aaa", Site.class);
        List<Site> sitesTest = asList(site1);
        try {
            service.assignTemplateToSites(null, sitesTest);
            fail("Expected IllegalArgumentException. Null object is passed instead of study ");
        } catch(IllegalArgumentException ise) {
            assertEquals(TemplateService.STUDY_IS_NULL, ise.getMessage());
        }
    }

    public void testAssignTemplateToSitesRequiresSitesList() throws Exception {
        Study study = createNamedInstance("sldfksdfjk", Study.class);
        try {
            service.assignTemplateToSites(study, null);
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

        siteDao.save(site1);
        studyDao.save(study);
        expectLastCall().anyTimes();
        authorizationManager.removeProtectionGroup(DomainObjectTools.createExternalObjectId(notInUse));
        replayMocks();

        try {
            service.removeTemplateFromSites(study, asList(site1, site2));
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("Cannot remove 1 site (Dartmouth) from study ECOG 1234 because there are subject(s) assigned", scve.getMessage());
        }
        verifyMocks();

        List<Site> remainingSites = study.getSites();
        assertEquals("Removable site not removed", 1, remainingSites.size());
        assertEquals("Wrong site retained", "Dartmouth", remainingSites.get(0).getName());
    }
         public void removeTemplateFromSites(Study studyTemplate, List<Site> sites) {
        List<StudySite> studySites = studyTemplate.getStudySites();
        List<StudySite> toRemove = new LinkedList<StudySite>();
        List<Site> cannotRemove = new LinkedList<Site>();
        for (Site site : sites) {
            for (StudySite studySite : studySites) {
                if (studySite.getSite().equals(site)) {
                    if (studySite.isUsed()) {
                        cannotRemove.add(studySite.getSite());
                    } else {
                        try {
                            authorizationManager.removeProtectionGroup(DomainObjectTools.createExternalObjectId(studySite));
                        } catch (RuntimeException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new StudyCalendarSystemException(e);
                        }
                        toRemove.add(studySite);
                    }
                }
            }
        }
        for (StudySite studySite : toRemove) {
            Site siteAssoc = studySite.getSite();
            siteAssoc.getStudySites().remove(studySite);
            siteDao.save(siteAssoc);
            Study studyAssoc = studySite.getStudy();
            studyAssoc.getStudySites().remove(studySite);
            studyDao.save(studyAssoc);
        }
        if (cannotRemove.size() > 0) {
            StringBuilder msg = new StringBuilder("Cannot remove ")
                    .append(StringUtils.pluralize(cannotRemove.size(), "site"))
                    .append(" (");
            for (Iterator<Site> it = cannotRemove.iterator(); it.hasNext();) {
                Site site = it.next();
                msg.append(site.getName());
                if (it.hasNext()) msg.append(", ");
            }
            msg.append(") from study ").append(studyTemplate.getName())
                    .append(" because there are subject(s) assigned");
            throw new StudyCalendarValidationException(msg.toString());
        }
    }
    ////// Helpers
    private void expectAuthManagerToReturnSites(Site... sites) {
        List<ProtectionGroup> pgs = new ArrayList<ProtectionGroup>();

        for (int i=0; i< sites.length; i++) {
            Site site = setId(i, sites[i]);

            ProtectionGroup pg = pg(createExternalObjectId(site));
            pgs.add(pg);

            expect(siteService.getById(i)).andReturn(site);
        }

        expect(authorizationManager.getSites()).andReturn(pgs);
    }

    private ProtectionGroup pg(String name) {
        ProtectionGroup mayoPG = new ProtectionGroup();
        mayoPG.setProtectionGroupName(name);
        return mayoPG;
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
