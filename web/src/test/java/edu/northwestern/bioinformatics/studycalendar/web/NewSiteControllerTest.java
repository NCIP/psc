package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setId;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.validation.Errors;
import static org.easymock.EasyMock.expect;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Jalpa Patel
 */
public class NewSiteControllerTest  extends ControllerTestCase {
    private NewSiteController controller = new NewSiteController();
    private SiteService siteService;
    private NewSiteCommand command;
    private Site nu;

    protected void setUp() throws Exception {
        super.setUp();
        nu = setId(1, Fixtures.createNamedInstance("Northwestern", Site.class));
        siteService = registerMockFor(SiteService.class);
        command = new NewSiteCommand(nu, siteService);
        controller.setSiteService(siteService);
    }

    public void testReferenceDataForCreateAction() throws Exception {
        Map<String, Object> refdata = controller.referenceData(request,command,null);
        assertEquals("Action does not match","Create",refdata.get("action"));
    }

    public void testReferenceDataForEditAction() throws Exception {
        request.addParameter("id","2");
        Map<String, Object> refdata = controller.referenceData(request,command,null);
        assertEquals("Action does not match","Edit",refdata.get("action"));
    }

    public void testReferenceDataForSite() throws Exception {
        command.setSite(nu);
        Map<String, Object> refdata = controller.referenceData(request,command,null);
        assertEquals("Site does not match", command.getSite(), refdata.get("site"));
    }

    public void testFormView() throws Exception {
        assertEquals("Form view does not exist","createSite", controller.getFormView());
    }

    public void testOnSubmit() throws Exception {
        NewSiteController mockableController = new MockableCommandController();
        expect(command.createSite()).andReturn(null);
        replayMocks();

        ModelAndView mv = mockableController.handleRequest(request, response);
        verifyMocks();

        assertEquals("Wrong view", "sites", ((RedirectView)mv.getView()).getUrl());
    }

    private class MockableCommandController extends NewSiteController{
        public MockableCommandController() {
            setSiteService(siteService);
            setValidateOnBinding(false);
        }
        @Override
        protected Object formBackingObject(HttpServletRequest request) throws Exception {
            return command;
        }
        @Override
        protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest, Object oCommand, Errors errors) throws Exception {
            return null;
        }
    }

}
