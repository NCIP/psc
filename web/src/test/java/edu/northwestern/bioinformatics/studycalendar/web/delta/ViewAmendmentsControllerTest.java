package edu.northwestern.bioinformatics.studycalendar.web.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import static edu.northwestern.bioinformatics.studycalendar.domain.Role.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_SUBJECT_CALENDAR_MANAGER;
import static org.easymock.classextension.EasyMock.expect;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.web.servlet.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Map;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class ViewAmendmentsControllerTest extends ControllerTestCase {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private ViewAmendmentsController controller;

    private StudyDao studyDao;

    private Study study;
    private Amendment aC, aB, aA;
    private Site tucson, flagstaff;
    private StudySite tucsonSS, flagstaffSS;
    private User user;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        study = setId(0, createBasicTemplate());
        study.setName("Hoodoo");
        assignIds(study);

        aC = setId(26, createAmendments("A", "B", "C"));
        aB = setId(25, aC.getPreviousAmendment());
        aA = setId(24, aB.getPreviousAmendment());
        study.setAmendment(aC);

        tucson = createNamedInstance("Tuscon", Site.class);
        flagstaff = createNamedInstance("Flagstaff", Site.class);

        tucsonSS = createStudySite(study, tucson);
        flagstaffSS = createStudySite(study, flagstaff);

        user = createUser("jimbo");
        request.setAttribute("currentUser", user);

        studyDao = registerDaoMockFor(StudyDao.class);

        expect(studyDao.getById(study.getId())).andReturn(study).times(0, 1);
        request.setParameter("study", study.getId().toString());

        controller = new ViewAmendmentsController();
        controller.setStudyDao(studyDao);
        controller.setControllerTools(controllerTools);
    }
    
    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations, STUDY_SUBJECT_CALENDAR_MANAGER);
    }

    public void testDevelopmentAmendmentIncludedIfPresentAndUserIsStudyCoordinator() throws Exception {
        Amendment dev = new Amendment();
        study.setDevelopmentAmendment(dev);
        setUserRoles(user, STUDY_COORDINATOR);

        AmendmentView actual
            = (AmendmentView) handleAndReturnModel("dev");
        assertNotNull("Dev amendment not present", actual);
        assertSame("Wrong amendment as dev", dev, actual.getAmendment());
    }

    public void testDevelopmentAmendmentNotIncludedIfPresentAndUserIsNotStudyCoordinator() throws Exception {
        Amendment dev = new Amendment();
        study.setDevelopmentAmendment(dev);
        setUserRoles(user, SUBJECT_COORDINATOR);

        assertNull("Should have no dev amendment", handleAndReturnModel("dev"));
    }

    public void testDevelopmentAmendmentNotIncludedIfNotPresentAndUserIsStudyCoordinator() throws Exception {
        study.setDevelopmentAmendment(null);
        setUserRoles(user, STUDY_COORDINATOR);

        assertNull("Should have no dev amendment", handleAndReturnModel("dev"));
    }

    public void testAmendmentsIncludedAndWrapped() throws Exception {
        List<AmendmentView> actual = handleAndReturnActualAmendmentViews();
        assertEquals("Wrong amendment for zeroth view", aC, actual.get(0).getAmendment());
        assertEquals("Wrong amendment for first view", aB, actual.get(1).getAmendment());
        assertEquals("Wrong amendment for second view", aA, actual.get(2).getAmendment());
    }

    public void testStudyIncluded() throws Exception {
        assertSame(study, handleAndReturnModel("study"));
    }

    public void testSelectedAmendmentDefaultsToFirstReleased() throws Exception {
        Amendment dev = new Amendment();
        study.setDevelopmentAmendment(dev);
        assertSame(aC, handleAndReturnModel("amendment"));
    }

    public void testSelectedAmendmentDefaultsToDevIfStudyCoord() throws Exception {
        Amendment dev = new Amendment();
        study.setDevelopmentAmendment(dev);
        setUserRoles(user, STUDY_COORDINATOR);
        assertSame(dev, handleAndReturnModel("amendment"));
    }

    public void testAmendmentParameterTrumpsDefaultAmendment() throws Exception {
        request.setParameter("amendment", aB.getId().toString());
        assertSame(aB, handleAndReturnModel("amendment"));
    }

    public void testAmendmentParameterTrumpsDefaultAmendmentForStudyCoord() throws Exception {
        Amendment dev = new Amendment();
        study.setDevelopmentAmendment(dev);
        setUserRoles(user, STUDY_COORDINATOR);
        request.setParameter("amendment", aB.getId().toString());
        assertSame(aB, handleAndReturnModel("amendment"));
    }

    public void testSelectedAmendmentWhenItIsTheDevelopmentAmendment() throws Exception {
        Amendment dev = setId(6, new Amendment());
        study.setDevelopmentAmendment(dev);
        setUserRoles(user, STUDY_COORDINATOR);
        request.setParameter("amendment", "6");
        assertSame(dev, handleAndReturnModel("amendment"));
    }
    
    public void testCannotSelectTheDevAmendmentWhenNotStudyCoord() throws Exception {
        Amendment dev = setId(6, new Amendment());
        study.setDevelopmentAmendment(dev);
        setUserRoles(user, SUBJECT_COORDINATOR);
        request.setParameter("amendment", "6");
        assertNull(handle());
        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }
    
    public void test404ForUnmatchedAmendment() throws Exception {
        request.setParameter("amendment", "88");
        assertNull(handle());
        assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
        assertEquals("The amendment with id 88 (if any) is not part of the study Hoodoo", response.getErrorMessage());
    }

    public void testNoSiteApprovalsReturnedForStudyCoord() throws Exception {
        setUserRoles(user, STUDY_COORDINATOR);
        AmendmentView aBview = handleAndReturnActualAmendmentViews().get(1);
        assertSame("Test setup failure", aB, aBview.getAmendment());
        assertEquals(0, aBview.getApprovals().size());
    }

    public void testAllSiteApprovalsReturnedForStudyAdministrator() throws Exception {
        setUserRoles(user, STUDY_ADMIN);

        AmendmentView aBview = handleAndReturnActualAmendmentViews().get(1);
        assertSame("Test setup failure", aB, aBview.getAmendment());
        assertEquals("Wrong number of approvals", 2, aBview.getApprovals().size());
        assertContainsKey("Missing Tucson", aBview.getApprovals(), tucsonSS);
        assertContainsKey("Missing Flagstaff", aBview.getApprovals(), flagstaffSS);
    }

    public void testSiteBasedApprovalsReturnedForSiteCoordinator() throws Exception {
        setUserRoles(user, SITE_COORDINATOR);
        user.getUserRole(SITE_COORDINATOR).addSite(flagstaff);

        AmendmentView aBview = handleAndReturnActualAmendmentViews().get(1);
        assertSame("Test setup failure", aB, aBview.getAmendment());
        assertEquals("Wrong number of approvals", 1, aBview.getApprovals().size());
        assertContainsKey("Missing Flagstaff", aBview.getApprovals(), flagstaffSS);
    }

    public void testStudySiteBasedApprovalsReturnedForSubjectCoordinator() throws Exception {
        setUserRoles(user, SUBJECT_COORDINATOR);
        user.getUserRole(SUBJECT_COORDINATOR).addStudySite(tucsonSS);

        AmendmentView aBview = handleAndReturnActualAmendmentViews().get(1);
        assertSame("Test setup failure", aB, aBview.getAmendment());
        assertEquals("Wrong number of approvals", 1, aBview.getApprovals().size());
        assertContainsKey("Missing Tucson", aBview.getApprovals(), tucsonSS);
    }

    public void testApprovalsNotIncludedForDevelopmentAmendment() throws Exception {
        setUserRoles(user, STUDY_ADMIN, STUDY_COORDINATOR);
        Amendment dev = setId(6, new Amendment());
        study.setDevelopmentAmendment(dev);

        AmendmentView devView = (AmendmentView) handleAndReturnModel("dev");
        assertNotNull("dev not present", devView);
        assertEquals("Wrong number of approvals", 0, devView.getApprovals().size());
    }

    @SuppressWarnings({ "unchecked" })
    private List<AmendmentView> handleAndReturnActualAmendmentViews() throws Exception {
        List<AmendmentView> actual
            = (List<AmendmentView>) handleAndReturnModel("amendments");
        assertNotNull("Amendments not present", actual);
        assertEquals("Wrong number of wrapped amendments", 3, actual.size());
        return actual;
    }

    private Object handleAndReturnModel(String key) throws Exception {
        return handleAndReturnModel().get(key);
    }

    @SuppressWarnings({ "unchecked" })
    private Map<String, Object> handleAndReturnModel() throws Exception {
        Map<String, Object> model = (Map<String, Object>) handle().getModel();
        log.debug("Model: {}", model);
        return model;
    }

    private ModelAndView handle() throws Exception {
        replayMocks();
        ModelAndView result = controller.handleRequest(request, response);
        verifyMocks();
        return result;
    }
}
