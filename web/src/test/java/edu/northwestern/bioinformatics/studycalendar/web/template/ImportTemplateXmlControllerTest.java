/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;

import javax.servlet.http.HttpServletRequest;

import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.DATA_IMPORTER;

public class ImportTemplateXmlControllerTest extends ControllerTestCase {
    private ImportTemplateXmlController controller;
    private ImportTemplateXmlCommand command;
    private MockMultipartHttpServletRequest multipartRequest;

    private static final String TEST_XML = "<study assigned-identifier=\"Study A\"><planned-calendar/></study>";

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        command = registerMockFor(ImportTemplateXmlCommand.class, ImportTemplateXmlCommand.class.getMethod("apply"));

        controller = new ImportTemplateXmlController(){
            protected ImportTemplateXmlCommand formBackingObject(HttpServletRequest request) throws Exception {
                return command;
            }
        };

        // Stop controller from calling validation
        controller.setValidators(null);

        multipartRequest = new MockMultipartHttpServletRequest();
        multipartRequest.setMethod("POST");
        multipartRequest.setSession(session);
    }

    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations, DATA_IMPORTER);
    }

    public void testSubmit() throws Exception {
        assertEquals("Wrong view", "redirectToStudyList", getOnSubmitData().getViewName());
    }


    public void testGet() throws Exception {
        multipartRequest.setMethod("GET");

        ModelAndView mv = controller.handleRequest(multipartRequest, response);

        assertNotNull("View is null", mv.getViewName());
    }

    public void testBindActivitiesXml() throws Exception {
        MultipartFile mockFile = new MockMultipartFile("studyXml", TEST_XML.getBytes());
        multipartRequest.addFile(mockFile);

        command.apply();
        replayMocks();

        ModelAndView mv = controller.handleRequest(multipartRequest, response);
        assertNoBindingErrorsFor("studyXml", mv.getModel());
        verifyMocks();


        assertNotNull("Activities file should not be null", command.getStudyXml());
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
