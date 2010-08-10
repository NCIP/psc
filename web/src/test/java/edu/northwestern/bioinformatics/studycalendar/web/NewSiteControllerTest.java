package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import gov.nih.nci.cabig.ctms.suite.authorization.ScopeType;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.PERSON_AND_ORGANIZATION_INFORMATION_MANAGER;
import static org.easymock.EasyMock.expect;

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

    public void testAuthorizedRoles() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        String[] siteId = {nu.getId().toString()};
        params.put("id", siteId);
        expect(siteService.getById(nu.getId())).andReturn(nu);

        replayMocks();

        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, params);
        assertRolesAllowed(actualAuthorizations, PERSON_AND_ORGANIZATION_INFORMATION_MANAGER);
        assertSiteScopedRolesAllowed(actualAuthorizations, nu, PERSON_AND_ORGANIZATION_INFORMATION_MANAGER);
    }

    public void testAuthorizedRolesWithMalformedId() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        String[] siteId = {"not an id"};
        params.put("id", siteId);
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, params);
        assertOnlyAllScopedRolesAllowed(actualAuthorizations, ScopeType.SITE, PERSON_AND_ORGANIZATION_INFORMATION_MANAGER);
    }

    public void testAuthorizedRolesWithoutAnId() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, params);
        assertOnlyAllScopedRolesAllowed(actualAuthorizations, ScopeType.SITE, PERSON_AND_ORGANIZATION_INFORMATION_MANAGER);
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
