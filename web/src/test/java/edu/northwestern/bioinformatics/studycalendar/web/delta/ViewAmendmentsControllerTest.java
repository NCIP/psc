/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.delta;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.PscUserBuilder;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static org.easymock.classextension.EasyMock.*;

/**
 * Note that this class also contains tests for AmendmentView, since it was once a inner class
 * of the target controller.
 *
 * @author Rhett Sutphin
 */
public class ViewAmendmentsControllerTest extends ControllerTestCase {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private ViewAmendmentsController controller;

    private Study study;
    private Amendment aC, aB, aA;
    private Site tucson, flagstaff;
    private StudySite tucsonSS, flagstaffSS;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        study = setId(0, createBasicTemplate("Hoodoo"));
        assignIds(study);

        aC = setId(26, createAmendments("A", "B", "C"));
        aB = setId(25, aC.getPreviousAmendment());
        aA = setId(24, aB.getPreviousAmendment());
        study.setAmendment(aC);

        tucson = createNamedInstance("Tuscon", Site.class);
        flagstaff = createNamedInstance("Flagstaff", Site.class);

        tucsonSS = createStudySite(study, tucson);
        flagstaffSS = createStudySite(study, flagstaff);

        // default user can see everything
        setUser(new PscUserBuilder("jimbo").add(PscRole.STUDY_QA_MANAGER).forAllSites().toUser());

        StudyDao studyDao = registerDaoMockFor(StudyDao.class);

        expect(studyDao.getById(study.getId())).andReturn(study).times(0, 1);
        request.setParameter("study", study.getId().toString());

        controller = new ViewAmendmentsController();
        controller.setStudyDao(studyDao);
        controller.setControllerTools(controllerTools);
    }

    public void testDevelopmentAmendmentIncludedIfPresentAndUserInManagingRole() throws Exception {
        Amendment dev = new Amendment();
        study.setDevelopmentAmendment(dev);

        AmendmentView actual
            = (AmendmentView) handleAndReturnModel("dev");
        assertNotNull("Dev amendment not present", actual);
        assertSame("Wrong amendment as dev", dev, actual.getAmendment());
    }

    public void testDevelopmentAmendmentNotIncludedIfPresentAndUserIsNotStudyCoordinator() throws Exception {
        Amendment dev = new Amendment();
        study.setDevelopmentAmendment(dev);
        setUser(new PscUserBuilder("jo").
            add(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forAllStudies().forAllSites().toUser());

        assertNull("Should have no dev amendment", handleAndReturnModel("dev"));
    }

    public void testDevelopmentAmendmentNotIncludedIfNotPresentAndUserIsStudyCoordinator() throws Exception {
        study.setDevelopmentAmendment(null);

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
        setUser(AuthorizationObjectFactory.createPscUser("jimbo"));

        Amendment dev = new Amendment();
        study.setDevelopmentAmendment(dev);
        assertSame(aC, handleAndReturnModel("amendment"));
    }

    public void testSelectedAmendmentDefaultsToDevIfStudyCoord() throws Exception {
        Amendment dev = new Amendment();
        study.setDevelopmentAmendment(dev);

        assertSame(dev, handleAndReturnModel("amendment"));
    }

    public void testAmendmentParameterTrumpsDefaultAmendment() throws Exception {
        request.setParameter("amendment", aB.getId().toString());
        assertSame(aB, handleAndReturnModel("amendment"));
    }

    public void testAmendmentParameterTrumpsDefaultAmendmentForStudyCoord() throws Exception {
        Amendment dev = new Amendment();
        study.setDevelopmentAmendment(dev);

        request.setParameter("amendment", aB.getId().toString());
        assertSame(aB, handleAndReturnModel("amendment"));
    }

    public void testSelectedAmendmentWhenItIsTheDevelopmentAmendment() throws Exception {
        Amendment dev = setId(6, new Amendment());
        study.setDevelopmentAmendment(dev);

        request.setParameter("amendment", "6");
        assertSame(dev, handleAndReturnModel("amendment"));
    }
    
    public void testCannotSelectTheDevAmendmentWhenNotStudyCoord() throws Exception {
        Amendment dev = setId(6, new Amendment());
        study.setDevelopmentAmendment(dev);
        setUser(new PscUserBuilder("jo").
            add(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forAllStudies().forAllSites().toUser());
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
        setUser(new PscUserBuilder("jo").
            add(PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER).forAllSites().forAllStudies().toUser());

        AmendmentView aBview = handleAndReturnActualAmendmentViews().get(1);
        assertSame("Test setup failure", aB, aBview.getAmendment());
        assertEquals(0, aBview.getApprovals().size());
    }

    public void testAllSiteApprovalsReturnedForStudyAdministrator() throws Exception {
        AmendmentView aBview = handleAndReturnActualAmendmentViews().get(1);
        assertSame("Test setup failure", aB, aBview.getAmendment());
        assertEquals("Wrong number of approvals", 2, aBview.getApprovals().size());
        assertContainsKey("Missing Tucson", aBview.getApprovals(), tucsonSS);
        assertContainsKey("Missing Flagstaff", aBview.getApprovals(), flagstaffSS);
    }

    public void testSiteBasedApprovalsReturnedForSiteScopedParticipationRole() throws Exception {
        study.addManagingSite(tucson);
        setUser(new PscUserBuilder("J").
            add(PscRole.STUDY_QA_MANAGER).forSites(flagstaff).toUser());

        AmendmentView aBview = handleAndReturnActualAmendmentViews().get(1);
        assertSame("Test setup failure", aB, aBview.getAmendment());
        assertEquals("Wrong number of approvals", 1, aBview.getApprovals().size());
        assertContainsKey("Missing Flagstaff", aBview.getApprovals(), flagstaffSS);
    }

    public void testApprovalsNotIncludedForDevelopmentAmendment() throws Exception {
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

    private void setUser(PscUser user) {
        request.setAttribute("currentUser", user);
    }
}
