package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.ReleasedTemplate;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.StudyCalendarAuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import gov.nih.nci.security.util.ObjectSetUtil;
import static org.easymock.EasyMock.*;
import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.checkOrder;

import static java.util.Arrays.asList;
import java.util.*;

/**
 * @author Rhett Sutphin
 */
public class TemplateServiceTest extends StudyCalendarTestCase {
    private TemplateService service;

    private StudyDao studyDao;
    private SiteDao siteDao;
    private StudySiteDao studySiteDao;
    private DaoFinder daoFinder;
    private StudyCalendarAuthorizationManager authorizationManager;
    private AuthorizationService authorizationService;
    private DeltaDao deltaDao;
    private UserRoleDao userRoleDao;

    private User user;
    private UserRole siteCoordinatorRole;
    private UserRole subjectCoordinatorRole;
    private DeletableDomainObjectDao domainObjectDao;
    private SiteService siteService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        studyDao = registerDaoMockFor(StudyDao.class);
        siteDao = registerDaoMockFor(SiteDao.class);
        studySiteDao = registerDaoMockFor(StudySiteDao.class);
        deltaDao = registerDaoMockFor(DeltaDao.class);
        userRoleDao = registerDaoMockFor(UserRoleDao.class);
        authorizationManager = registerMockFor(StudyCalendarAuthorizationManager.class);
        authorizationService = registerMockFor(AuthorizationService.class);
        daoFinder = registerMockFor(DaoFinder.class);
        domainObjectDao = registerMockFor(DeletableDomainObjectDao.class);
        siteService = registerMockFor(SiteService.class);

        service = new TemplateService();
        service.setStudyDao(studyDao);
        service.setSiteDao(siteDao);
        service.setDeltaDao(deltaDao);
        service.setUserRoleDao(userRoleDao);
        service.setDaoFinder(daoFinder);
        service.setStudyCalendarAuthorizationManager(authorizationManager);
        service.setStudySiteDao(studySiteDao);
        service.setAuthorizationService(authorizationService);
        service.setSiteService(siteService);

        user = createUser("jimbo", Role.SITE_COORDINATOR, Role.SUBJECT_COORDINATOR);
        siteCoordinatorRole = user.getUserRole(Role.SITE_COORDINATOR);
        subjectCoordinatorRole = user.getUserRole(Role.SUBJECT_COORDINATOR);
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
            assertEquals(TemplateService.SITES_LIST_IS_NULL, ise.getMessage());
        }
    }

    public void testAssignTemplateToSubjectCoordinatorRequiresSite() throws Exception {
        try {
            service.assignTemplateToSubjectCoordinator(null, null, null);
            fail("Expected IllegalArgumentException. Null object is passed instead of studyTest ");
        } catch(IllegalArgumentException ise) {
            assertEquals(TemplateService.STUDY_IS_NULL, ise.getMessage());
        }
    }

    public void testAssignTemplateToSubjectCoordinatorRequiresStudy() throws Exception {
        Study study = createNamedInstance("sldfksdfjk", Study.class);
        try {
            service.assignTemplateToSubjectCoordinator(study, null, null);
            fail("Expected IllegalArgumentException. Null object is passed instead of siteTest ");
        } catch(IllegalArgumentException ise) {
            assertEquals(TemplateService.SITE_IS_NULL, ise.getMessage());
        }
    }

    public void testAssignTemplateToSubjectCoordinatorRequiresUser() throws Exception {
        Study study = createNamedInstance("sldfksdfjk", Study.class);
        Site  site  = createNamedInstance("asdf", Site.class);
        try {
            service.assignTemplateToSubjectCoordinator(study, site, null);
            fail("Expected IllegalArgumentException. Null object is passed instead of userTest ");
        } catch(IllegalArgumentException ise) {
            assertEquals(TemplateService.USER_IS_NULL, ise.getMessage());
        }
    }

    public void testComparatorForStudyName() throws Exception {
        Study a = createNamedInstance("a", Study.class);
        Study aa = createNamedInstance("aa", Study.class);
        Study A = createNamedInstance("A", Study.class);
        Study AA = createNamedInstance("AA", Study.class);
        Study ab = createNamedInstance("ab", Study.class);
        Study one = createNamedInstance("1", Study.class);
        Study underscoreOne = createNamedInstance("_1", Study.class);
        Study y = createNamedInstance("y", Study.class);
        Study YY = createNamedInstance("YY", Study.class);
        Study m = createNamedInstance("m", Study.class);
        Study MM = createNamedInstance("MM", Study.class);
        Study asdf = createNamedInstance("asdf", Study.class);

        Amendment amendment = createAmendments("a1", "a2", "a3");
        a.setAmendment(amendment);
        aa.setAmendment(amendment);
        A.setAmendment(amendment);
        AA.setAmendment(amendment);
        ab.setAmendment(amendment);
        one.setAmendment(amendment);
        underscoreOne.setAmendment(amendment);
        y.setAmendment(amendment);
        YY.setAmendment(amendment);
        m.setAmendment(amendment);
        MM.setAmendment(amendment);
        asdf.setAmendment(amendment);

        List<Study> allStudies = new ArrayList<Study>();
        allStudies.add(MM);
        allStudies.add(a);
        allStudies.add(ab);
        allStudies.add(one);
        allStudies.add(underscoreOne);
        allStudies.add(y);
        allStudies.add(aa);
        allStudies.add(A);
        allStudies.add(AA);
        allStudies.add(YY);
        allStudies.add(m);
        allStudies.add(asdf);

        List<ReleasedTemplate> releasedTemplates = new ArrayList<ReleasedTemplate>();
        for (Study visibleStudy : allStudies) {
            if (visibleStudy.isReleased()) {
                releasedTemplates.add(new ReleasedTemplate(visibleStudy, true));
            }
        }

        assertEquals("Wrong study before sorting", MM, releasedTemplates.get(0).getStudy());
        assertEquals("Wrong study before sorting", a, releasedTemplates.get(1).getStudy());
        assertEquals("Wrong study before sorting", ab, releasedTemplates.get(2).getStudy());
        assertEquals("Wrong study before sorting", one, releasedTemplates.get(3).getStudy());
        assertEquals("Wrong study before sorting", underscoreOne, releasedTemplates.get(4).getStudy());
        assertEquals("Wrong study before sorting", y, releasedTemplates.get(5).getStudy());
        assertEquals("Wrong study before sorting", aa, releasedTemplates.get(6).getStudy());
        assertEquals("Wrong study before sorting", A, releasedTemplates.get(7).getStudy());
        assertEquals("Wrong study before sorting", AA, releasedTemplates.get(8).getStudy());
        assertEquals("Wrong study before sorting", YY, releasedTemplates.get(9).getStudy());
        assertEquals("Wrong study before sorting", m, releasedTemplates.get(10).getStudy());

        Collections.sort(releasedTemplates, TemplateService.AlphabeticallyOrderedComparator.INSTANCE);

        assertEquals("Wrong study after sorting", one, releasedTemplates.get(0).getStudy());
        assertEquals("Wrong study after sorting", underscoreOne, releasedTemplates.get(1).getStudy());
        assertEquals("Wrong study after sorting", a, releasedTemplates.get(2).getStudy());
        assertEquals("Wrong study after sorting", A, releasedTemplates.get(3).getStudy());
        assertEquals("Wrong study after sorting", aa, releasedTemplates.get(4).getStudy());
        assertEquals("Wrong study after sorting", AA, releasedTemplates.get(5).getStudy());
        assertEquals("Wrong study after sorting", ab, releasedTemplates.get(6).getStudy());
        assertEquals("Wrong study after sorting", asdf, releasedTemplates.get(7).getStudy());
        assertEquals("Wrong study after sorting", m, releasedTemplates.get(8).getStudy());
        assertEquals("Wrong study after sorting", MM, releasedTemplates.get(9).getStudy());
        assertEquals("Wrong study after sorting", y, releasedTemplates.get(10).getStudy());
    }

    public void testRemoveAssignedTemplateFromSubjectCoordinatorRequiresSite() throws Exception {
        try {
            service.removeAssignedTemplateFromSubjectCoordinator(null, null, null);
            fail("Expected IllegalArgumentException. Null object is passed instead of studyTest ");
        } catch(IllegalArgumentException ise) {
            assertEquals(TemplateService.STUDY_IS_NULL, ise.getMessage());
        }
    }

    public void testRemoveAssignedTemplateFromSubjectCoordinatorRequiresStudy() throws Exception {
        Study study = createNamedInstance("sldfksdfjk", Study.class);
        try {
            service.removeAssignedTemplateFromSubjectCoordinator(study, null, null);
            fail("Expected IllegalArgumentException. Null object is passed instead of siteTest ");
        } catch(IllegalArgumentException ise) {
            assertEquals(TemplateService.SITE_IS_NULL, ise.getMessage());
        }
    }

    public void testRemoveAssignedTemplateFromSubjectCoordinatorRequiresUser() throws Exception {
        Study study = createNamedInstance("sldfksdfjk", Study.class);
        Site  site  = createNamedInstance("asdf", Site.class);
        try {
            service.removeAssignedTemplateFromSubjectCoordinator(study, site, null);
            fail("Expected IllegalArgumentException. Null object is passed instead of userTest ");
        } catch(IllegalArgumentException ise) {
            assertEquals(TemplateService.USER_IS_NULL, ise.getMessage());
        }
    }

    public void testAssignTemplateToSubjectCoordinator() throws Exception {
        Site  site          = setId(1, createNamedInstance("Northwestern", Site.class));
        Study study         = setId(1, createNamedInstance("Study A", Study.class));
        StudySite studySite = setId(1, createStudySite(study, site));

        edu.northwestern.bioinformatics.studycalendar.domain.User user =
                setId(1, createNamedInstance("John", edu.northwestern.bioinformatics.studycalendar.domain.User.class));
        user.setCsmUserId(1L);

        UserRole userRole = createUserRole(user, Role.SUBJECT_COORDINATOR, site);
        user.addUserRole(userRole);

        userRoleDao.save(userRole);

        String studySitePGName = DomainObjectTools.createExternalObjectId(studySite);
        List<String> assignedUserId = asList(user.getCsmUserId().toString());
        authorizationManager.createAndAssignPGToUser(assignedUserId, studySitePGName, TemplateService.SUBJECT_COORDINATOR_ACCESS_ROLE);
        replayMocks();

        edu.northwestern.bioinformatics.studycalendar.domain.User actualUser =
                service.assignTemplateToSubjectCoordinator(study, site, user);
        verifyMocks();

        UserRole actualUserRole = actualUser.getUserRole(Role.SUBJECT_COORDINATOR);
        assertEquals("Wrong study site size", 1, actualUserRole.getStudySites().size());
    }

    public void testAssignTemplateToSubjectCoordinatorStudySiteAlreadyExists() throws Exception {
        Site  site          = setId(1, createNamedInstance("Northwestern", Site.class));
        Study study         = setId(1, createNamedInstance("Study A", Study.class));
        StudySite studySite = setId(1, createStudySite(study, site));

        edu.northwestern.bioinformatics.studycalendar.domain.User user =
                setId(1, createNamedInstance("John", edu.northwestern.bioinformatics.studycalendar.domain.User.class));
        user.setCsmUserId(1L);

        UserRole userRole = createUserRole(user, Role.SUBJECT_COORDINATOR, site);
        user.addUserRole(userRole);
        userRole.addStudySite(studySite);

        replayMocks();

        edu.northwestern.bioinformatics.studycalendar.domain.User actualUser =
                service.assignTemplateToSubjectCoordinator(study, site, user);
        verifyMocks();

        UserRole actualUserRole = actualUser.getUserRole(Role.SUBJECT_COORDINATOR);
        assertEquals("Wrong study site size", 1, actualUserRole.getStudySites().size());
    }

    public void testRemoveAssignedTemplateFromSubjectCoordinator() throws Exception {
        Site site0  = setId(1, createNamedInstance("Northwestern", Site.class));
        Site site1  = setId(2, createNamedInstance("Mayo", Site.class));
        Study study = setId(1, createNamedInstance("Study A", Study.class));

        StudySite studySite0 = setId(1, createStudySite(study, site0));
        StudySite studySite1 = setId(2, createStudySite(study, site1));

        edu.northwestern.bioinformatics.studycalendar.domain.User user =
                setId(1, createNamedInstance("John", edu.northwestern.bioinformatics.studycalendar.domain.User.class));
        user.setCsmUserId(1L);

        UserRole userRole = createUserRole(user, Role.SUBJECT_COORDINATOR, site0, site1);
        userRole.addStudySite(studySite0);
        userRole.addStudySite(studySite1);

        user.addUserRole(userRole);

        userRoleDao.save(userRole);

        String studySitePGName = DomainObjectTools.createExternalObjectId(studySite0);
        ProtectionGroup expectedPG = createProtectionGroup(1L, studySitePGName);
        expect(authorizationManager.getPGByName(studySitePGName)).andReturn(expectedPG);
        authorizationManager.removeProtectionGroupUsers(asList(user.getCsmUserId().toString()), expectedPG);
        replayMocks();

        edu.northwestern.bioinformatics.studycalendar.domain.User actualUser
                = service.removeAssignedTemplateFromSubjectCoordinator(study, site0, user);
        verifyMocks();

        UserRole actualUserRole = actualUser.getUserRole(Role.SUBJECT_COORDINATOR);
        assertEquals("Wrong study site0 size", 1, actualUserRole.getStudySites().size());
        assertEquals("Wrong study site", studySite1, actualUserRole.getStudySites().get(0));
    }

    public void testRemoveAssignedTemplateFromSubjectCoordinatorWhenSubjCoordHasAssignments() throws Exception {
        Site nu = setId(1, createNamedInstance("Northwestern", Site.class));
        Site mayo = setId(2, createNamedInstance("Mayo", Site.class));
        Study study = setId(1, createNamedInstance("Study A", Study.class));

        StudySite nuSS = setId(1, createStudySite(study, nu));
        StudySite mayoSS = setId(2, createStudySite(study, mayo));

        StudySubjectAssignment assignment = createAssignment(study, nu, createSubject("Don't", "Care"));

        edu.northwestern.bioinformatics.studycalendar.domain.User subjectCoordinator =
                setId(1, createUser("jimbo", Role.SUBJECT_COORDINATOR));
        subjectCoordinator.setCsmUserId(1L);
        subjectCoordinator.getStudySubjectAssignments().add(assignment);

        UserRole userRole = subjectCoordinator.getUserRole(Role.SUBJECT_COORDINATOR);
        userRole.addStudySite(nuSS);
        userRole.addStudySite(mayoSS);

        replayMocks();
        try {
            service.removeAssignedTemplateFromSubjectCoordinator(study, nu, subjectCoordinator);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("Wrong exception message", 
                "jimbo is still responsible for one or more subjects on Study A at Northwestern.  Please reassign those subjects before removing jimbo from that study and site.",
                scve.getMessage());
        }
    }

    public void testAssignMultipleTemplates() throws Exception {
        Study studyTemplate1 = createNamedInstance("aaa", Study.class);
        Study studyTemplate2 = createNamedInstance("bbb", Study.class);
        Site site1 = setId(1, createNamedInstance("site1", Site.class));

        StudySite studySite1 = setId(1, createStudySite(studyTemplate1, site1));

        List<Study> studyTemplates = asList(studyTemplate1, studyTemplate2);

        String userId = "1";

        List<String> assignedUserIds = new ArrayList<String>();
        assertTrue(assignedUserIds.size() == 0);
        assignedUserIds.add(userId);
        assertTrue(assignedUserIds.size() == 1);

        String studySitePGName = DomainObjectTools.createExternalObjectId(studySite1);

        authorizationManager.createAndAssignPGToUser(assignedUserIds, studySitePGName, TemplateService.SUBJECT_COORDINATOR_ACCESS_ROLE);
        replayMocks();
        service.assignMultipleTemplates(studyTemplates, site1, userId);
        verifyMocks();
    }

    public void testAssignMultipleTemplatesRequiresStudyList() throws Exception {
        Site site1 = setId(1, createNamedInstance("site1", Site.class));
        String userId = "1";
        try {
            service.assignMultipleTemplates(null, site1, userId);
            fail("Expected IllegalArgumentException. Null object is passed instead of studyTemplate ");
        } catch(IllegalArgumentException ise) {
            assertEquals(TemplateService.STUDIES_LIST_IS_NULL, ise.getMessage());
        }
    }

    public void testAssignMultipleTemplatesRequiresSite() throws Exception {
        Study studyTemplate = createNamedInstance("abc", Study.class);
        String userId = "1";
        List<Study> studyTemplates = asList(studyTemplate);
        try {
            service.assignMultipleTemplates(studyTemplates, null, userId);
            fail("Expected IllegalArgumentException. Null object is passed instead of site ");
        } catch(IllegalArgumentException ise) {
            assertEquals(TemplateService.SITE_IS_NULL, ise.getMessage());
        }
    }

    public void testAssignMultipleTemplatesRequiresUserId() throws Exception {
        Site site1 = setId(1, createNamedInstance("site1", Site.class));
        Study studyTemplate = createNamedInstance("abc", Study.class);
        List<Study> studyTemplates = asList(studyTemplate);
        try {
            service.assignMultipleTemplates(studyTemplates, site1, null);
            fail("Expected IllegalArgumentException. Null object is passed instead of userId ");
        } catch(IllegalArgumentException ise) {
            assertEquals(TemplateService.STRING_IS_NULL, ise.getMessage());
        }
    }

    public void testGetSiteLists() throws Exception {
        Map<String, List> siteLists = new HashMap<String, List>();
        List<Site> availableSites = new ArrayList<Site>();
        List<Site> assignedSites = new ArrayList<Site>();

        Study studyTemplate1 = createNamedInstance("aaa", Study.class);

        List<ProtectionGroup> allSitePGs = new ArrayList<ProtectionGroup>();
        expect(authorizationManager.getSites()).andReturn(allSitePGs);

        for (ProtectionGroup site : allSitePGs) {
            Site protectionGroupName = null;
            expect(siteDao.getByName(site.getProtectionGroupName())).andReturn(protectionGroupName);
            availableSites.add(protectionGroupName);
        }
        for (StudySite ss : studyTemplate1.getStudySites()) {
            assignedSites.add(ss.getSite());
        }
        availableSites = (List) ObjectSetUtil.minus(availableSites, assignedSites);
        siteLists.put(StudyCalendarAuthorizationManager.ASSIGNED_PGS, assignedSites);
        siteLists.put(StudyCalendarAuthorizationManager.AVAILABLE_PGS, availableSites);

        replayMocks();
        Map<String, List<Site>> siteListsToCompare;
        siteListsToCompare = service.getSiteLists(studyTemplate1);
        verifyMocks();
        assertEquals(siteLists,siteListsToCompare);

        try {
            service.getSiteLists(null);
            fail("Expected IllegalArgumentException. Null object is passed instead of studyTemplate ");
        } catch(IllegalArgumentException ise) {
            ise.getMessage();
        }
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

    public void testTemplateVisibility() throws Exception {
        Study studyA = createNamedInstance("A", Study.class);
        Study studyB = createNamedInstance("B", Study.class);
        expect(authorizationService.isTemplateVisible(siteCoordinatorRole, studyA)).andReturn(false);
        expect(authorizationService.isTemplateVisible(siteCoordinatorRole, studyB)).andReturn(true);

        replayMocks();
        List<Study> actualStudyTemplates = service.filterForVisibility(asList(studyA, studyB), siteCoordinatorRole);
        verifyMocks();
        assertEquals("Wrong number of studies returned", 1, actualStudyTemplates.size());
        assertSame("Wrong study returned", studyB, actualStudyTemplates.get(0));
    }

    public void testFilterForVizReturnsNothingWithNullRole() throws Exception {
        Study studyTemplate1 = createNamedInstance("aaa", Study.class);
        List<Study> studyTemplates = asList(studyTemplate1);

        replayMocks();
        List<Study> actual = service.filterForVisibility(studyTemplates, null);
        verifyMocks();

        assertEquals(0, actual.size());
    }

    public void testFilterForVizRequiresListOfStudies() throws Exception {
        try {
            service.filterForVisibility(null, subjectCoordinatorRole);
            fail("Exception not thrown");
        } catch(IllegalArgumentException ise) {
            assertEquals(TemplateService.STUDIES_LIST_IS_NULL, ise.getMessage());
        }
    }

    public void testRemoveMultipleTemplates() throws Exception {
        String userId = "123";
        Site site1 = setId(123, createNamedInstance("site1", Site.class));
        Study studyTemplate1 = createNamedInstance("aaa", Study.class);
        StudySite studySite1 = setId(123, createStudySite(studyTemplate1, site1));
        List<Study> studyTemplateList = asList(studyTemplate1);
        List<String> userIds = asList(userId);
        String studySitePGName = DomainObjectTools.createExternalObjectId(studySite1);
        ProtectionGroup studySitePG= new ProtectionGroup();
        studySitePG.setProtectionGroupId(1l);
        expect(authorizationManager.getPGByName(studySitePGName)).andReturn(studySitePG);
        authorizationManager.removeProtectionGroupUsers(userIds, studySitePG);

        replayMocks();
        service.removeMultipleTemplates(studyTemplateList, site1, userId);
        verifyMocks();
    }

    public void testRemoveMultipleTemplatesRequiresListOfStudies() throws Exception {
        Site site1 = setId(123, createNamedInstance("site1", Site.class));
        try {
            service.removeMultipleTemplates(null, site1, "UserName");
            fail("Expected IllegalArgumentException. Null object is passed instead of List of Studies ");
        } catch(IllegalArgumentException ise) {
            assertEquals(TemplateService.STUDIES_LIST_IS_NULL, ise.getMessage());
        }
    }

    public void testRemoveMultipleTemplatesRequiresSite() throws Exception {
        Study studyTemplate1 = createNamedInstance("aaa", Study.class);
        List<Study> studyTemplateList = asList(studyTemplate1);
        try {
            service.removeMultipleTemplates(studyTemplateList, null, "UserName");
            fail("Expected IllegalArgumentException. Null object is passed instead of site ");
        } catch(IllegalArgumentException ise) {
            assertEquals(TemplateService.SITE_IS_NULL, ise.getMessage());
        }
    }

    public void testRemoveMultipleTemplatesRequiresStudy() throws Exception {
        Site site1 = setId(123, createNamedInstance("site1", Site.class));
        Study studyTemplate1 = createNamedInstance("aaa", Study.class);
        List<Study> studyTemplateList = asList(studyTemplate1);
        try {
            service.removeMultipleTemplates(studyTemplateList, site1, null);
            fail("Expected IllegalArgumentException. Null object is passed instead of userId ");
        } catch(IllegalArgumentException ise) {
            assertEquals(TemplateService.STRING_IS_NULL, ise.getMessage());
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

    public void testFindParentWhenImmediatelyAvailable() throws Exception {
        Study study = createBasicTemplate();
        assertSame(study.getPlannedCalendar(),
            service.findParent(study.getPlannedCalendar().getEpochs().get(1)));
        assertSame(study.getPlannedCalendar().getEpochs().get(1),
            service.findParent(study.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0)));
    }
    
    public void testFindParentWhenNotImmediatelyAvailable() throws Exception {
        Study study = createBasicTemplate();
        Epoch e1 = study.getPlannedCalendar().getEpochs().get(1);
        StudySegment e1a0 = e1.getStudySegments().get(0);
        e1a0.setParent(null);
        e1.getStudySegments().remove(e1a0);

        expect(deltaDao.findDeltaWhereAdded(e1a0)).andReturn(Delta.createDeltaFor(e1));
        replayMocks();

        assertSame(e1, service.findParent(e1a0));
        verifyMocks();
    }

    public void testFindParentWhenAddedAsChildOfAnotherAddAndThenRemovedAlone() throws Exception {
        // simulates this process:
        //   Period P0 with child P0E0 added to E1A1 --> Results in Add(P0) in amendment R0
        //   P0E0 removed in amendment R1 --> Results in  Remove(P0E0) and P0E0.period=>null

        Study study = createBasicTemplate();
        Period p = createPeriod("P0", 3, 17, 1);
        PlannedActivity p0e0 = createPlannedActivity("P0E0", 4);
        study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(1).addPeriod(p);

        expect(deltaDao.findDeltaWhereAdded(p0e0)).andReturn(null);
        expect(deltaDao.findDeltaWhereRemoved(p0e0)).andReturn(Delta.createDeltaFor(p, Remove.create(p0e0)));
        replayMocks();

        assertSame(p, service.findParent(p0e0));
        verifyMocks();
    }

    public void testDelete() throws Exception {
        PlannedActivity pa = createPlannedActivity("P0E0", 4);
        PlannedActivityLabel pal = createPlannedActivityLabel("label");
        pa.addPlannedActivityLabel(pal);

        expect(daoFinder.findDao(PlannedActivity.class)).andReturn(domainObjectDao);
        expect(daoFinder.findDao(PlannedActivityLabel.class)).andReturn(domainObjectDao);
        domainObjectDao.delete(pal);
        domainObjectDao.delete(pa);

        replayMocks();
        service.delete(pa);
        verifyMocks();
    }

    public void testFindAncestorWhenPossible() throws Exception {
        Study study = createBasicTemplate();
        Epoch e1 = study.getPlannedCalendar().getEpochs().get(1);
        StudySegment e1a0 = e1.getStudySegments().get(0);

        assertEquals(e1, service.findAncestor(e1a0, Epoch.class));
        assertEquals(study.getPlannedCalendar(), service.findAncestor(e1a0, PlannedCalendar.class));
        assertEquals(study.getPlannedCalendar(), service.findAncestor(e1, PlannedCalendar.class));
    }

    public void testFindAncestorWhenDynamicSubclass() throws Exception {
        Study study = createBasicTemplate();
        Epoch dynamic = new Epoch() { };
        study.getPlannedCalendar().addEpoch(dynamic);

        assertSame(study.getPlannedCalendar(), service.findAncestor(dynamic, PlannedCalendar.class));
    }
    
    public void testFindAncestorWhenNotPossible() throws Exception {
        Study study = createBasicTemplate();
        Epoch e1 = study.getPlannedCalendar().getEpochs().get(1);

        try {
            service.findAncestor(e1, Period.class);
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException expected) {
            assertEquals("Epoch is not a descendant of Period", expected.getMessage());
        }
    }

    public void testFindStudyForPlannedCalendar() throws Exception {
        Study study = createBasicTemplate();
        assertSame(study, service.findStudy(study.getPlannedCalendar()));
    }

    public void testFindStudyForOtherNodes() throws Exception {
        Study study = createBasicTemplate();
        Epoch epoch = study.getPlannedCalendar().getEpochs().get(0);
        assertSame(study, service.findStudy(epoch));
        assertSame(study, service.findStudy(epoch.getStudySegments().get(0)));
    }

    public void testFindEquivalentChildForStudyNode() throws Exception {
        Study study = createBasicTemplate();
        assignIds(study);
        Study studyAsNode = study;
        assertSame(study, service.findEquivalentChild(study, studyAsNode));
    }

    public void testFindEquivalentChildForPopulationNode() throws Exception {
        Study study = createBasicTemplate();
        assignIds(study);
        Population population = new Population();
        population.setName("population name");
        population.setAbbreviation("population abbreviation");
        Set<Population> populations = new HashSet<Population>();
        populations.add(population);
        study.setPopulations(populations);
        assertSame(population, service.findEquivalentChild(study, population));
    }

    public void testFindEquivalentChildWhenActualChild() throws Exception {
        Study study = createBasicTemplate();
        assignIds(study);
        Epoch e = study.getPlannedCalendar().getEpochs().get(1);
        assertSame(e, service.findEquivalentChild(study, e));
    }

    public void testFindEquivalentChildByIdAndType() throws Exception {
        int sameId = 50;
        PlannedActivity expectedNode = setId(sameId, createPlannedActivity("PA0", 3));

        Study study = createBasicTemplate();
        assignIds(study);
        Period p0 = setId(sameId, createPeriod("P0", 1, 14, 4));
        p0.addPlannedActivity(setId(49, createPlannedActivity("PA1", 1)));
        p0.addPlannedActivity(expectedNode);
        study.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0).addPeriod(p0);

        PlannedActivity parameter = setId(sameId, new PlannedActivity());
        // set one of each node to the same id to ensure that type checking is happening
        study.getPlannedCalendar().setId(sameId);
        study.getPlannedCalendar().getEpochs().get(0).setId(sameId);
        study.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0).setId(sameId);

        assertSame(expectedNode, service.findEquivalentChild(study, parameter));
    }

    public void testFindEquivalentChildByGridIdAndType() throws Exception {
        String sameGridId = "gridId50";
        PlannedActivity expectedNode = setGridId(sameGridId, createPlannedActivity("PA0", 3));

        Study study = createBasicTemplate();
        assignIds(study);
        Period p0 = setGridId(sameGridId, createPeriod("P0", 1, 14, 4));
        p0.addPlannedActivity(setGridId("gridId49", createPlannedActivity("PA1", 1)));
        p0.addPlannedActivity(expectedNode);
        study.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0).addPeriod(p0);

        PlannedActivity parameter = setGridId(sameGridId, new PlannedActivity());
        // set one of each node to the same id to ensure that type checking is happening
        study.getPlannedCalendar().setGridId(sameGridId);
        study.getPlannedCalendar().getEpochs().get(0).setGridId(sameGridId);
        study.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0).setGridId(sameGridId);

        assertSame(expectedNode, service.findEquivalentChild(study, parameter));
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
