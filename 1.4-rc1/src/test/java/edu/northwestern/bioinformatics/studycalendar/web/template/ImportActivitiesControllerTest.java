package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.fileupload.FileItem;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class ImportActivitiesControllerTest extends ControllerTestCase {
    private ImportActivitiesController controller;
    private ImportActivitiesCommand command;
    List<Activity> activities;
    private static final String TEST_XML = "<sources><source=\"ts\"/></sources>";
    private MockMultipartHttpServletRequest multipartRequest;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        command = registerMockFor(ImportActivitiesCommand.class, ImportActivitiesCommand.class.getMethod("apply"));
        controller = new ImportActivitiesController() {

            protected ImportActivitiesCommand formBackingObject(HttpServletRequest request) throws Exception {
                return command;
            }
        };

        // Stop controller from calling validation
        controller.setValidators(null);

        multipartRequest = new MockMultipartHttpServletRequest();
        multipartRequest.setMethod("POST");
        multipartRequest.setSession(session);

    }

    public void testSubmit() throws Exception {
        assertEquals("Wrong view", "redirectToStudyList", getOnSubmitData().getViewName());
    }

    public void testSubmitWithReturnToPeriodId() throws Exception {
        multipartRequest.setParameter("returnToPeriodId", "1");

        ModelAndView mv = getOnSubmitData();

        assertEquals("Wrong view", "redirectToManagePeriod", mv.getViewName());
        assertNotNull("Period Id is null", mv.getModel().get("id"));
    }

    public void testGet() throws Exception {
        multipartRequest.setMethod("GET");

        ModelAndView mv = controller.handleRequest(multipartRequest, response);

        assertNotNull("View is null", mv.getViewName());
    }

    public void testBindActivitiesXml() throws Exception {
        MultipartFile mockFile = new MockMultipartFile("activitiesFile", TEST_XML.getBytes());
        multipartRequest.addFile(mockFile);

        command.apply();
        replayMocks();

        ModelAndView mv = controller.handleRequest(multipartRequest, response);
        assertNoBindingErrorsFor("activitiesFile", mv.getModel());
        verifyMocks();


        assertNotNull("Activities file should not be null", command.getActivitiesFile());
    }

    protected ModelAndView getOnSubmitData() throws Exception {
        onSubmitDataCalls();
        replayMocks();

        ModelAndView mv = controller.handleRequest(multipartRequest, response);
        verifyMocks();
        return mv;
    }

    protected void onSubmitDataCalls() throws Exception{
        command.apply();
    }
}
