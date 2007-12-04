package edu.northwestern.bioinformatics.studycalendar.web.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import static org.easymock.classextension.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class ViewAmendmentsControllerTest extends WebTestCase {
    private ViewAmendmentsController controller;

    private StudyDao studyDao;

    private Study study;
    private User user;
    private Amendment aC, aB, aA;

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
        
        user = createUser("jimbo");
        request.setAttribute("currentUser", user);

        studyDao = registerDaoMockFor(StudyDao.class);

        expect(studyDao.getById(study.getId())).andReturn(study).times(0, 1);
        request.setParameter("study", study.getId().toString());

        controller = new ViewAmendmentsController();
        controller.setStudyDao(studyDao);
        controller.setControllerTools(controllerTools);
    }

    public void testDevelopmentAmendmentIncludedIfPresentAndUserIsStudyCoordinator() throws Exception {
        Amendment dev = new Amendment();
        study.setDevelopmentAmendment(dev);
        setUserRoles(user, Role.STUDY_COORDINATOR);

        AmendmentView actual
            = (AmendmentView) handleAndReturnModel("dev");
        assertNotNull("Dev amendment not present", actual);
        assertSame("Wrong amendment as dev", dev, actual.getAmendment());
    }

    public void testDevelopmentAmendmentNotIncludedIfPresentAndUserIsNotStudyCoordinator() throws Exception {
        Amendment dev = new Amendment();
        study.setDevelopmentAmendment(dev);
        setUserRoles(user, Role.SUBJECT_COORDINATOR);

        assertNull("Should have no dev amendment", handleAndReturnModel("dev"));
    }

    public void testDevelopmentAmendmentNotIncludedIfNotPresentAndUserIsStudyCoordinator() throws Exception {
        study.setDevelopmentAmendment(null);
        setUserRoles(user, Role.STUDY_COORDINATOR);

        assertNull("Should have no dev amendment", handleAndReturnModel("dev"));
    }

    public void testAmendmentsIncludedAndWrapped() throws Exception {

        List<AmendmentView> actual
            = (List<AmendmentView>) handleAndReturnModel("amendments");
        assertNotNull("Amendments not present", actual);
        assertEquals("Wrong number of wrapped amendments", 3, actual.size());
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
        setUserRoles(user, Role.RESEARCH_ASSOCIATE);
        assertSame(aC, handleAndReturnModel("amendment"));
    }

    public void testSelectedAmendmentDefaultsToDevIfStudyCoord() throws Exception {
        Amendment dev = new Amendment();
        study.setDevelopmentAmendment(dev);
        setUserRoles(user, Role.STUDY_COORDINATOR);
        assertSame(dev, handleAndReturnModel("amendment"));
    }

    public void testAmendmentParameterTrumpsDefaultAmendment() throws Exception {
        request.setParameter("amendment", aB.getId().toString());
        assertSame(aB, handleAndReturnModel("amendment"));
    }

    public void testAmendmentParameterTrumpsDefaultAmendmentForStudyCoord() throws Exception {
        Amendment dev = new Amendment();
        study.setDevelopmentAmendment(dev);
        setUserRoles(user, Role.STUDY_COORDINATOR);
        request.setParameter("amendment", aB.getId().toString());
        assertSame(aB, handleAndReturnModel("amendment"));
    }

    public void testSelectedAmendmentWhenItIsTheDevelopmentAmendment() throws Exception {
        Amendment dev = setId(6, new Amendment());
        study.setDevelopmentAmendment(dev);
        setUserRoles(user, Role.STUDY_COORDINATOR);
        request.setParameter("amendment", "6");
        assertSame(dev, handleAndReturnModel("amendment"));
    }
    
    public void testCannotSelectTheDevAmendmentWhenNotStudyCoord() throws Exception {
        Amendment dev = setId(6, new Amendment());
        study.setDevelopmentAmendment(dev);
        setUserRoles(user, Role.SUBJECT_COORDINATOR);
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

    private Object handleAndReturnModel(String key) throws Exception {
        return handleAndReturnModel().get(key);
    }

    @SuppressWarnings({ "unchecked" })
    private Map<String, Object> handleAndReturnModel() throws Exception {
        return (Map<String, Object>) handle().getModel();
    }

    private ModelAndView handle() throws Exception {
        replayMocks();
        ModelAndView result = controller.handleRequest(request, response);
        verifyMocks();
        return result;
    }
}
