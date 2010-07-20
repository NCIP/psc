package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_TEAM_ADMINISTRATOR;
import static org.easymock.classextension.EasyMock.expect;

import static java.util.Calendar.*;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Rhett Sutphin
 */
public class SiteCoordinatorControllerTest extends ControllerTestCase {
    private SiteCoordinatorController controller;

    private UserDao userDao;

    private Study studyA, studyB, studyAB, studyBZ, studyZ;
    private StudySite ssAa, ssBb, ssABa, ssABb, ssBZb, ssBZz, ssZz;
    private Site siteA, siteB, siteZ;
    private User user;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        userDao = registerDaoMockFor(UserDao.class);

        controller = new SiteCoordinatorController();
        controller.setUserDao(userDao);
        applicationSecurityManager.setUserService(registerMockFor(UserService.class));
        controller.setApplicationSecurityManager(applicationSecurityManager);

        studyA  = createNamedInstance("A", Study.class);
        studyB  = createNamedInstance("B", Study.class);
        studyAB = createNamedInstance("AB", Study.class);
        studyBZ = createNamedInstance("BZ", Study.class);
        studyZ  = createNamedInstance("Z", Study.class);

        siteA = createNamedInstance("a", Site.class);
        siteB = createNamedInstance("b", Site.class);
        siteZ = createNamedInstance("z", Site.class);

        ssAa  = createStudySite(studyA, siteA);
        ssBb  = createStudySite(studyB, siteB);
        ssABa = createStudySite(studyAB, siteA);
        ssABb = createStudySite(studyAB, siteB);
        ssBZb = createStudySite(studyBZ, siteB);
        ssBZz = createStudySite(studyBZ, siteZ);
        ssZz  = createStudySite(studyZ, siteZ);

        user = createUser("jimbo", Role.SITE_COORDINATOR, Role.SUBJECT_COORDINATOR);
        user.getUserRole(Role.SITE_COORDINATOR).addSite(siteB);
        user.getUserRole(Role.SITE_COORDINATOR).addSite(siteA);
        user.getUserRole(Role.SUBJECT_COORDINATOR).addSite(siteZ);
        SecurityContextHolderTestHelper.setSecurityContext(user, null);

        expect(applicationSecurityManager.getFreshUser()).andReturn(user).anyTimes();
    }

    public void testAllowedAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations, STUDY_TEAM_ADMINISTRATOR);
    }

    public void testUserInModel() throws Exception {
        Map<String, Object> actualModel = execAndReturnModel();
        assertTrue("Missing user", actualModel.containsKey("user"));
        assertSame("Wrong user", user, actualModel.get("user"));
    }

    @SuppressWarnings({ "unchecked" })
    public void testSitesInModel() throws Exception {
        Map<String, Object> actualModel = execAndReturnModel();
        assertContainsKey("Missing sites", actualModel, "sites");
        Collection<Site> sites = (Collection<Site>) actualModel.get("sites");
        assertNotNull(sites);
        assertContains("Missing site a", sites, siteA);
        assertContains("Missing site b", sites, siteB);
        assertEquals("Wrong number of sites", 2, sites.size());
        Iterator<Site> it = sites.iterator();
        assertEquals("Sites not sorted", siteA, it.next());
        assertEquals("Sites not sorted", siteB, it.next());
        assertFalse(it.hasNext());
    }
    
    @SuppressWarnings({ "unchecked" })
    public void testNoticesForPendingApprovals() throws Exception {
        Amendment gort = new Amendment("Gort");
        Amendment klaatu = new Amendment("Klaatu");
        studyAB.pushAmendment(gort);
        studyAB.pushAmendment(klaatu);

        // All approved at site A
        ssABa.approveAmendment(gort, DateTools.createDate(2004, APRIL, 6));
        ssABa.approveAmendment(klaatu, DateTools.createDate(2004, APRIL, 9));
        // Only gort approved at site B
        ssABb.approveAmendment(gort, DateTools.createDate(2006, MARCH, 8));

        Map<String, Object> actualModel = execAndReturnModel();
        assertTrue("Missing notices", actualModel.containsKey("notices"));
        Map<String, List<SiteCoordinatorController.Notification>> noticeMap
            = (Map<String, List<SiteCoordinatorController.Notification>>) actualModel.get("notices");
        assertContainsKey("Missing approvals notices", noticeMap, "approvals");
        List<SiteCoordinatorController.Notification> approvalNotifications = noticeMap.get("approvals");
        assertEquals("Wrong number of notifications", 1, approvalNotifications.size());

        SiteCoordinatorController.Notification actual = approvalNotifications.get(0);
        assertEquals("Wrong studySite", ssABb, actual.getStudySite());
        assertEquals("Wrong amendment", klaatu, actual.getAmendment());
    }

    @SuppressWarnings({ "unchecked" })
    public void testStudiesInModel() throws Exception {
        Map<String, Object> actualModel = execAndReturnModel();
        assertTrue("Missing model object", actualModel.containsKey("studiesAndSites"));
        Map<Study, Map<Site, StudySite>> actual = (Map<Study, Map<Site, StudySite>>) actualModel.get("studiesAndSites");
        assertNotNull(actual);

        assertContains("Missing study A", actual.keySet(), studyA);
        assertContains("Missing study B", actual.keySet(), studyB);
        assertContains("Missing study AB", actual.keySet(), studyAB);
        assertContains("Missing study BZ", actual.keySet(), studyBZ);
        assertNotContains("Should not have study Z", actual.keySet(), studyZ);
        assertEquals("Wrong number of studies", 4, actual.keySet().size());

        assertEquals("Wrong number of sites for study A", 2, actual.get(studyA).keySet().size());
        assertContainsPair("Wrong study site for site A", actual.get(studyA),  siteA, ssAa);
        assertContainsPair("Wrong study site for site B", actual.get(studyA),  siteB, null);
        assertEquals("Wrong number of sites for study B", 2, actual.get(studyA).keySet().size());
        assertContainsPair("Wrong study site for site A", actual.get(studyB),  siteA, null);
        assertContainsPair("Wrong study site for site B", actual.get(studyB),  siteB, ssBb);
        assertEquals("Wrong number of sites for study AB", 2, actual.get(studyA).keySet().size());
        assertContainsPair("Wrong study site for site A", actual.get(studyAB), siteA, ssABa);
        assertContainsPair("Wrong study site for site B", actual.get(studyAB), siteB, ssABb);
        assertEquals("Wrong number of sites for study BZ", 2, actual.get(studyA).keySet().size());
        assertContainsPair("Wrong study site for site A", actual.get(studyBZ), siteA, null);
        assertContainsPair("Wrong study site for site B", actual.get(studyBZ), siteB, ssBZb);
    }

    @SuppressWarnings({ "unchecked" })
    private Map<String, Object> execAndReturnModel() throws Exception {
        replayMocks();
        Map<String, Object> model = controller.handleRequestInternal(request, response).getModel();
        verifyMocks();
        return model;
    }
}
