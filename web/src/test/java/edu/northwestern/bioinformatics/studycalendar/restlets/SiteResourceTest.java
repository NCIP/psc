package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.test.ServicedFixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import static org.easymock.EasyMock.expect;
import org.restlet.data.Status;

/**
 * @author Saurabh Agrawal
 */
public class SiteResourceTest extends ResourceTestCase<SiteResource> {

    public static final String SITE_IDENTIFIER = "site_id";

    public static final String SITE_NAME = "site_name";

    private SiteService siteService;

    private Site site;


    @Override
    public void setUp() throws Exception {
        super.setUp();
        siteService = registerMockFor(SiteService.class);
        request.getAttributes().put(UriTemplateParameters.SITE_IDENTIFIER.attributeName(), SITE_IDENTIFIER);

        site = ServicedFixtures.createNamedInstance(SITE_NAME, Site.class);
        site.setAssignedIdentifier(SITE_IDENTIFIER);
    }

    @Override
    protected SiteResource createResource() {
        SiteResource resource = new SiteResource();
        resource.setSiteService(siteService);
        resource.setXmlSerializer(xmlSerializer);
        return resource;
    }

    public void testGetAndPutAndDeleteAllowed() throws Exception {
        assertAllowedMethods("PUT", "GET", "DELETE");
    }

    public void testGetXmlForExistingSite() throws Exception {
        expectFoundSite(site);
        expectObjectXmlized(site);

        doGet();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsCreatedXml();
    }

    public void testGetXmlForNonExistentSiteIs404() throws Exception {
        expectFoundSite(null);

        doGet();

        assertEquals("Result not 'not found'", 404, response.getStatus().getCode());
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
        expectSiteUsedByAssignments(site, true);
        siteService.removeSite(site);
        doDelete();

        assertEquals("Result not success", 200, response.getStatus().getCode());
    }

    private void expectSiteUsedByAssignments(final Site site, final boolean siteUsedByAssignment) {
        expect(siteService.checkIfSiteCanBeDeleted(site)).andReturn(siteUsedByAssignment);
    }

    public void testDeleteExistingSiteWhichIsused() throws Exception {
        expectFoundSite(site);
        expectSiteUsedByAssignments(site, false);
        doDelete();

        assertEquals("Result is success", 400, response.getStatus().getCode());
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

    private void expectCreateOrMergeSite(final Site existingSite, final Site newSite, final Site expectedSite) throws Exception {
        expect(siteService.createOrMergeSites(existingSite, newSite)).andReturn(expectedSite);

    }


    private void expectFoundSite(Site expectedSite) {
        expect(siteService.getByAssignedIdentifier(SITE_IDENTIFIER)).andReturn(expectedSite);
    }


}

