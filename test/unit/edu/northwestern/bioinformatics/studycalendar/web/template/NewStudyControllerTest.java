package edu.northwestern.bioinformatics.studycalendar.web.template;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import static org.easymock.classextension.EasyMock.*;

import java.util.Map;
import java.util.List;
import java.util.Arrays;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Rhett Sutphin
 */
public class NewStudyControllerTest extends ControllerTestCase {
    private NewStudyController controller;
    private NewStudyCommand command;
    private Study study;
    private StudyDao studyDao;
    private StudySiteDao studySiteDao;
    private SiteDao siteDao;

    protected void setUp() throws Exception {
        super.setUp();
        request.setMethod("GET");
        studyDao = registerMockFor(StudyDao.class);
        studySiteDao = registerMockFor(StudySiteDao.class);
        siteDao = registerMockFor(SiteDao.class);
        study = new Study();
        command = new TestCommand();

        controller = new TestController();
        controller.setStudyDao(studyDao);
        controller.setSiteDao(siteDao);
        controller.setStudySiteDao(studySiteDao);
    }

    public void testReferenceData() throws Exception {
        Map<String, Object> refdata = controller.referenceData(request);
        assertEquals("Wrong action name", "New", refdata.get("action"));
    }

    public void testViewOnGet() throws Exception {
        ModelAndView mv = controller.handleRequest(request, response);
        assertEquals("editStudy", mv.getViewName());
    }

    public void testViewOnGoodSubmit() throws Exception {
        study.setId(14);
        request.setMethod("POST");
        request.addParameter("studyName", "Study of things and stuff");
        ModelAndView mv = controller.handleRequest(request, response);
        assertEquals("redirectToCalendarTemplate", mv.getViewName());
    }

    public void testIdInModelOnGoodSubmit() throws Exception {
        study.setId(14);
        studyDao.save(study);
        expect(siteDao.getDefaultSite()).andReturn(new Site());
        studySiteDao.save((StudySite) notNull());
        replayMocks();

        request.addParameter("studyName", "Study of other things");
        request.addParameter("epochNames[0]", "Eocene");
        request.addParameter("arms[0]", "false");
        Map<String, Object> model = controller.onSubmit(command, new BindException(command, "command")).getModel();
        verifyMocks();

        assertEquals("New study's ID not in model", study.getId(), model.get("id"));
        assertEquals("Something besides the id in the model: " + model, 1, model.size());
    }

    // The binding tests test bind on GET for simplicity; bind on POST should be the same.

    public void testBindHasArms() throws Exception {
        request.addParameter("arms[0]", "true");
        request.addParameter("arms[1]", "false");
        request.addParameter("arms[2]", "true");
        controller.handleRequest(request, response);

        assertTrue(command.getArms().get(0));
        assertFalse(command.getArms().get(1));
        assertTrue(command.getArms().get(2));
    }

    public void testBindStudyName() throws Exception {
        String studyName = "This study right here";
        request.addParameter("studyName", studyName);
        controller.handleRequest(request, response);
        assertEquals(studyName, command.getStudyName());
    }

    public void testBindEpochNames() throws Exception {
        List<String> names = Arrays.asList("Eocene", "Holocene");
        request.addParameter("epochNames[0]", names.get(0));
        request.addParameter("epochNames[1]", names.get(1));

        controller.handleRequest(request, response);
        assertEquals(2, command.getEpochNames().size());
        assertEquals(names.get(0), command.getEpochNames().get(0));
        assertEquals(names.get(1), command.getEpochNames().get(1));
    }

    public void testBindArmNames() throws Exception {
        List<String> names = Arrays.asList("The arm", "An arm");
        request.addParameter("armNames[1][0]", names.get(0));
        request.addParameter("armNames[1][1]", names.get(1));

        controller.handleRequest(request, response);
        List<List<String>> actualArmNames = command.getArmNames();
        assertEquals(0, actualArmNames.get(0).size());
        assertEquals(2, actualArmNames.get(1).size());
        Object s = actualArmNames.get(1).get(0);
        assertEquals(names.get(0), s);
        assertEquals(names.get(1), actualArmNames.get(1).get(1));
    }

    private class TestController extends NewStudyController {
        protected Object formBackingObject(HttpServletRequest request) throws Exception {
            return command;
        }
    }

    private class TestCommand extends NewStudyCommand {
        public Study createStudy() {
            return study;
        }
    }
}
