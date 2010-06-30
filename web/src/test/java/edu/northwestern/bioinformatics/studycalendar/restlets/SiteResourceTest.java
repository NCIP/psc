package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import org.restlet.data.Status;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createUser;
import static edu.northwestern.bioinformatics.studycalendar.domain.Role.*;
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
        assertAllowedMethods("PUT", "GET", "DELETE");
    }

    public void testGetXmlForExistingSite() throws Exception {
        setLegacyCurrentUser(createUser("studyCo", STUDY_ADMIN));
        expectFoundSite(site);
        expectObjectXmlized(site);

        doGet();

        assertEquals("Result not success", 200, response.getStatus().getCode());
        assertResponseIsCreatedXml();
    }

    public void testGet403ForUnauthorizedUser() throws Exception {
        setLegacyCurrentUser(createUser("subjectCo",SUBJECT_COORDINATOR));
        expectFoundSite(site);
        expectObjectXmlized(site);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_FORBIDDEN);
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
        siteService.removeSite(site);
        doDelete();

        assertEquals("Result not success", 200, response.getStatus().getCode());
    }

    public void testDeleteExistingSiteWhichIsused() throws Exception {
        expectFoundSite(site);
        Fixtures.createAssignment(new Study(), site, new Subject());
        
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

