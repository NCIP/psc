package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class NewProvidedSiteControllerTest extends ControllerTestCase {
    private SiteService siteService;
    private NewProvidedSiteController controller;

    public void setUp() throws Exception {
        super.setUp();
        siteService = registerMockFor(SiteService.class);
        controller = new NewProvidedSiteController();
        controller.setSiteService(siteService);
    }

    public void testAddNewProvidedSite() throws Exception {
        String siteName = "Northwestern Uni";
        String assignedIdentifier = "NU";
        Site site = Fixtures.createSite(siteName,assignedIdentifier);
        request.addParameter("name",siteName);
        request.addParameter("assignedIdentifier",assignedIdentifier);
        expect(siteService.createOrUpdateSite(site)).andReturn(null);

        replayMocks();
        controller.handleRequestInternal(request,response);
        verifyMocks();
    }
}
