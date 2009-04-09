package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.SiteProvider;

import java.util.List;
import java.util.ArrayList;

import static org.easymock.EasyMock.expect;
import org.restlet.data.Status;

/**
 * @author Jalpa Patel
 */
public class ProvidedSitesResourceTest extends ResourceTestCase<ProvidedSitesResource>{
    private SiteDao siteDao;
    private SiteProvider siteProvider;

    public void setUp() throws Exception {
        super.setUp();
        siteDao = registerDaoMockFor(SiteDao.class);
        siteProvider = registerMockFor(SiteProvider.class);
    }

    @Override
    protected ProvidedSitesResource createResource() {
        ProvidedSitesResource resource = new ProvidedSitesResource();
        resource.setXmlSerializer(xmlSerializer);
        resource.setSiteDao(siteDao);
        resource.setSiteProvider(siteProvider);
        return resource;
    }

    public void testGetAllowed() throws Exception {
        assertAllowedMethods("GET");
    }

    public void testGetAllSites() throws Exception {
        String searchString = "s";
        QueryParameters.Q.putIn(request, searchString);
        List<Site> sites = new ArrayList<Site>();
        Site site = Fixtures.createSite("site");
        sites.add(site);
        expect(siteDao.searchSitesBySearchText(searchString)).andReturn(sites);
        expect(siteProvider.search(searchString)).andReturn(sites);
        expect(xmlSerializer.createDocumentString(sites)).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

}
