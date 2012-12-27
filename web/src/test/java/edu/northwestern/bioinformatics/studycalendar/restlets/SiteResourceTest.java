/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import org.restlet.data.Method;
import org.restlet.data.Status;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static org.easymock.EasyMock.expect;

/**
 * @author Saurabh Agrawal
 */
public class SiteResourceTest extends AuthorizedResourceTestCase<SiteResource> {
    public static final String SITE_IDENTIFIER = "site_id";
    public static final String SITE_NAME = "site_name";

    private SiteService siteService;

    private Site site;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        siteService = registerMockFor(SiteService.class);
        request.getAttributes().put(UriTemplateParameters.SITE_IDENTIFIER.attributeName(), SITE_IDENTIFIER);

        site = Fixtures.createNamedInstance(SITE_NAME, Site.class);
        site.setAssignedIdentifier(SITE_IDENTIFIER);
    }

    @Override
    protected SiteResource createAuthorizedResource() {
        SiteResource resource = new SiteResource();
        resource.setSiteService(siteService);
        resource.setXmlSerializer(xmlSerializer);
        return resource;
    }

    public void testGetAndPutAndDeleteAllowed() throws Exception {
        expectFoundSite(site);
        replayMocks();
        assertAllowedMethods("PUT", "GET", "DELETE");
    }

    public void testGetWithAuthorizedRoles() {
        expectFoundSite(site);
        replayMocks();
        assertRolesAllowedForMethod(Method.GET, PscRole.valuesWithSiteScoped());
    }

    public void testPutWithAuthorizedRoles() {
        expectFoundSite(site);
        replayMocks();
        assertRolesAllowedForMethod(Method.PUT,
            PERSON_AND_ORGANIZATION_INFORMATION_MANAGER);
    }

    public void testDeleteWithAuthorizedRoles() {
        expectFoundSite(site);
        replayMocks();
        assertRolesAllowedForMethod(Method.DELETE,
            PERSON_AND_ORGANIZATION_INFORMATION_MANAGER);
    }

    public void test400ForNoSiteIdentifier() throws Exception {
        UriTemplateParameters.SITE_IDENTIFIER.removeFrom(request);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST, "No site identifier in the request");
    }

    public void test404ForUnknownSite() throws Exception {
        expectFoundSite(null);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void testGetXmlForExistingSite() throws Exception {
        expectFoundSite(site);
        expectObjectXmlized(site);

        doGet();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsCreatedXml();
    }

    public void testPutNewXml() throws Exception {
        expectFoundSite(null);
        expectObjectXmlized(site);
        expectReadXmlFromRequestAs(site);

        expectCreateOrMergeSite(null, site, site);
        doPut();

        assertResponseStatus(Status.SUCCESS_CREATED);
        assertResponseIsCreatedXml();
    }

    public void testPutExistingSite() throws Exception {
        Site newSite = new Site();
        expectFoundSite(site);
        expectReadXmlFromRequestAs(newSite);
        expectObjectXmlized(newSite);

        expectCreateOrMergeSite(site, newSite, site);
        doPut();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsCreatedXml();
    }

    public void testDeleteExistingSiteWhichIsNotusedAnyWhere() throws Exception {
        expectFoundSite(site);
        siteService.removeSite(site);
        doDelete();

        assertEquals("Result not success", 200, response.getStatus().getCode());
    }

    public void test404WhenDeleteExistingSiteNotFound() throws Exception {
        expectFoundSite(null);

        doDelete();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    public void test400WhenDeleteExistingSiteWhichIsUsed() throws Exception {
        expectFoundSite(site);
        Fixtures.createAssignment(new Study(), site, new Subject());

        doDelete();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST,
                "Can not delete the site site_id because site has some assignments");
    }

    private void expectCreateOrMergeSite(final Site existingSite, final Site newSite, final Site expectedSite) throws Exception {
        expect(siteService.createOrMergeSites(existingSite, newSite)).andReturn(expectedSite);

    }

    private void expectFoundSite(Site expectedSite) {
        expect(siteService.getByAssignedIdentifier(SITE_IDENTIFIER)).andReturn(expectedSite);
    }
}

