/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import java.util.Collection;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.PERSON_AND_ORGANIZATION_INFORMATION_MANAGER;
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

    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations, PERSON_AND_ORGANIZATION_INFORMATION_MANAGER);
    }

    public void testAddNewProvidedSite() throws Exception {
        String siteName = "Northwestern Uni";
        String assignedIdentifier = "NU";
        String provider = "Provider";
        Site site = Fixtures.createSite(siteName,assignedIdentifier);
        site.setProvider(provider);
        request.addParameter("name",siteName);
        request.addParameter("assignedIdentifier",assignedIdentifier);
        request.addParameter("provider", provider);
        expect(siteService.createOrUpdateSite(site)).andReturn(null);

        replayMocks();
        controller.handleRequestInternal(request,response);
        verifyMocks();
    }
}
