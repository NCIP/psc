package edu.northwestern.bioinformatics.studycalendar.service;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createStudySite;

import static java.util.Arrays.asList;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createUserRole;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserRoleDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import static org.easymock.EasyMock.*;
import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.checkOrder;

import java.util.*;

import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import gov.nih.nci.security.util.ObjectSetUtil;

/**
 * @author Rhett Sutphin
 */
public class TemplateServiceTest extends StudyCalendarTestCase {
    private TemplateService service;

    private StudyDao studyDao;
    private SiteDao siteDao;
    private StudySiteDao studySiteDao;
    private StudyCalendarAuthorizationManager authorizationManager;
    private SiteService siteService;
    private DeltaDao deltaDao;
    private UserRoleDao userRoleDao;

    private User user;
    private UserRole siteCoordinatorRole;
    private UserRole subjectCoordinatorRole;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        studyDao = registerDaoMockFor(StudyDao.class);
        siteDao = registerDaoMockFor(SiteDao.class);
        studySiteDao = registerDaoMockFor(StudySiteDao.class);
        deltaDao = registerDaoMockFor(DeltaDao.class);
        userRoleDao = registerDaoMockFor(UserRoleDao.class);
        siteService = registerMockFor(SiteService.class);
        authorizationManager = registerMockFor(StudyCalendarAuthorizationManager.class);

        service = new TemplateService();
        service.setStudyDao(studyDao);
        service.setSiteDao(siteDao);
        service.setDeltaDao(deltaDao);
        service.setUserRoleDao(userRoleDao);
        service.setStudyCalendarAuthorizationManager(authorizationManager);
        service.setStudySiteDao(studySiteDao);
        service.setSiteService(siteService);

        user = Fixtures.createUser("jimbo", Role.SITE_COORDINATOR, Role.SUBJECT_COORDINATOR);
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
        Map<String, List> siteListsToCompare;
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
        expect(DomainObjectTools.loadFromExternalObjectId("edu.northwestern.bioinformatics.studycalendar.domain.Site.0", siteDao))
                .andReturn(expectedAvailableSite0);
        expect(DomainObjectTools.loadFromExternalObjectId("edu.northwestern.bioinformatics.studycalendar.domain.Site.1", siteDao))
                .andReturn(expectedAvailableSite1);
        replayMocks();

        Map<String, List> assignedAndAvailableSites = service.getSiteLists(study);
        verifyMocks();

        assertEquals("There should be assigned and available sites", 2, assignedAndAvailableSites.size());

        List<Site> actualAssignedSites = assignedAndAvailableSites.get(StudyCalendarAuthorizationManager.ASSIGNED_PGS);
        assertEquals("Wrong number of assigned sites", 1, actualAssignedSites.size());
        assertEquals("Wrong assigned site", "Mayo Clinic", actualAssignedSites.get(0).getName());

        List<Site> actualAvailableSites = assignedAndAvailableSites.get(StudyCalendarAuthorizationManager.AVAILABLE_PGS);
        assertEquals("Wrong number of available sites", 1, actualAvailableSites.size());
        assertEquals("Wrong available site", "Northwestern Clinic", actualAvailableSites.get(0).getName());
    }

    public void testGetTemplatesLists() throws Exception {
        Map<String, List> templatesMap = new HashMap<String, List>();
        List<Study> assignedTemplates = new ArrayList<Study>();
        List<Study> availableTemplates;
        List<Study> allTemplates = new ArrayList<Study>();

        Site site1 = setId(1, createNamedInstance("aaa", Site.class));
        Study studyTemplate1 = createNamedInstance("aaa", Study.class);
        StudySite studySite1 = setId(1, createStudySite(studyTemplate1, site1));
        allTemplates.add(site1.getStudySites().get(0).getStudy());

        gov.nih.nci.security.authorization.domainobjects.User subjectCdUser = new gov.nih.nci.security.authorization.domainobjects.User();
        subjectCdUser.setUserId(10001L);
        expect(authorizationManager.isUserPGAssigned(DomainObjectTools.createExternalObjectId(studySite1), subjectCdUser.getUserId().toString())).andReturn(true);
        assignedTemplates.add(studySite1.getStudy());
        availableTemplates = (List) ObjectSetUtil.minus(allTemplates, assignedTemplates);
        templatesMap.put(StudyCalendarAuthorizationManager.ASSIGNED_PES, assignedTemplates);
        templatesMap.put(StudyCalendarAuthorizationManager.AVAILABLE_PES, availableTemplates);

        Map<String, List> templateListToCompare;
        replayMocks();
        templateListToCompare = service.getTemplatesLists(site1, subjectCdUser);
        verifyMocks();
        assertEquals(templatesMap,templateListToCompare);
    }

    public void testGetTemplatesListsRequiresSite() throws Exception {
        gov.nih.nci.security.authorization.domainobjects.User subjectCdUser = new gov.nih.nci.security.authorization.domainobjects.User();
        try {
            service.getTemplatesLists(null, subjectCdUser);
            fail("Expected IllegalArgumentException. Null object is passed instead of site ");
        } catch(IllegalArgumentException ise) {
            assertEquals(TemplateService.SITE_IS_NULL, ise.getMessage());
        }
    }

    public void testGetTemplatesListsRequiresUser() throws Exception {
        Site site1 = setId(1, createNamedInstance("aaa", Site.class));
        try {
            service.getTemplatesLists(site1, null);
            fail("Expected IllegalArgumentException. Null object is passed instead of user ");
        } catch(IllegalArgumentException ise) {
            assertEquals(TemplateService.USER_IS_NULL, ise.getMessage());
        }
    }

    public void testGetSiteProtectionGroup() throws Exception {
        String protectionGroupName = "TestName";
        ProtectionGroup protectionGroup = new ProtectionGroup();
        protectionGroup.setProtectionGroupId(101l);
        protectionGroup.setProtectionGroupName(protectionGroupName);

        ProtectionGroup expectedProtectionGroup = new ProtectionGroup();
        expectedProtectionGroup.setProtectionGroupId(101l);
        expect(authorizationManager.getPGByName(protectionGroupName)).andReturn(expectedProtectionGroup);

        replayMocks();
        ProtectionGroup actualProtectionGroup = service.getSiteProtectionGroup(protectionGroupName);
        verifyMocks();
        assertEquals(expectedProtectionGroup, actualProtectionGroup);
    }

    public void testGetSiteProtectionGroupRequiresExpectedProtectionGroup() throws Exception {
        try {
            service.getSiteProtectionGroup(null);
            fail("Expected IllegalArgumentException. Null object is passed instead of protectionGroup ");
        } catch(IllegalArgumentException ise) {
            assertEquals(TemplateService.STRING_IS_NULL, ise.getMessage());
        }
    }

    public void testCheckOwnership() throws Exception {
        Study studyTemplate1 = createNamedInstance("aaa", Study.class);
        List<Study> studyTemplates = asList(studyTemplate1);
        List<Study> expectedStudyTemplates = new ArrayList<Study>();
        // TODO: temporary
        expect(authorizationManager.checkOwnership(user.getName(), studyTemplates)).andReturn(expectedStudyTemplates);

        replayMocks();
        List<Study> actualStudyTemplates = service.filterForVisibility(studyTemplates, subjectCoordinatorRole);
        verifyMocks();
        assertEquals(expectedStudyTemplates, actualStudyTemplates);
    }

    public void testCheckOwnershipRequiresUserName() throws Exception {
        Study studyTemplate1 = createNamedInstance("aaa", Study.class);
        List<Study> studyTemplates = asList(studyTemplate1);
        try {
            service.filterForVisibility(studyTemplates);
            fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException ise) {
            assertEquals("At least one UserRole is required", ise.getMessage());
        }
    }

    public void testCheckOwnershipRequiresListOfStudies() throws Exception {
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
        Study study = Fixtures.createBasicTemplate();
        assertSame(study.getPlannedCalendar(),
            service.findParent(study.getPlannedCalendar().getEpochs().get(1)));
        assertSame(study.getPlannedCalendar().getEpochs().get(1),
            service.findParent(study.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0)));
    }
    
    public void testFindParentWhenNotImmediatelyAvailable() throws Exception {
        Study study = Fixtures.createBasicTemplate();
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

        Study study = Fixtures.createBasicTemplate();
        Period p = Fixtures.createPeriod("P0", 3, 17, 1);
        PlannedActivity p0e0 = Fixtures.createPlannedActivity("P0E0", 4);
        study.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(1).addPeriod(p);

        expect(deltaDao.findDeltaWhereAdded(p0e0)).andReturn(null);
        expect(deltaDao.findDeltaWhereRemoved(p0e0)).andReturn(Delta.createDeltaFor(p, Remove.create(p0e0)));
        replayMocks();

        assertSame(p, service.findParent(p0e0));
        verifyMocks();
    }

    public void testFindAncestorWhenPossible() throws Exception {
        Study study = Fixtures.createBasicTemplate();
        Epoch e1 = study.getPlannedCalendar().getEpochs().get(1);
        StudySegment e1a0 = e1.getStudySegments().get(0);

        assertEquals(e1, service.findAncestor(e1a0, Epoch.class));
        assertEquals(study.getPlannedCalendar(), service.findAncestor(e1a0, PlannedCalendar.class));
        assertEquals(study.getPlannedCalendar(), service.findAncestor(e1, PlannedCalendar.class));
    }

    public void testFindAncestorWhenDynamicSubclass() throws Exception {
        Study study = Fixtures.createBasicTemplate();
        Epoch dynamic = new Epoch() { };
        study.getPlannedCalendar().addEpoch(dynamic);

        assertSame(study.getPlannedCalendar(), service.findAncestor(dynamic, PlannedCalendar.class));
    }
    
    public void testFindAncestorWhenNotPossible() throws Exception {
        Study study = Fixtures.createBasicTemplate();
        Epoch e1 = study.getPlannedCalendar().getEpochs().get(1);

        try {
            service.findAncestor(e1, Period.class);
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException expected) {
            assertEquals("Epoch is not a descendant of Period", expected.getMessage());
        }
    }

    public void testFindStudyForPlannedCalendar() throws Exception {
        Study study = Fixtures.createBasicTemplate();
        assertSame(study, service.findStudy(study.getPlannedCalendar()));
    }

    public void testFindStudyForOtherNodes() throws Exception {
        Study study = Fixtures.createBasicTemplate();
        Epoch epoch = study.getPlannedCalendar().getEpochs().get(0);
        assertSame(study, service.findStudy(epoch));
        assertSame(study, service.findStudy(epoch.getStudySegments().get(0)));
    }
}
