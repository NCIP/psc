package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.SiteProviderAdapter;
import static org.easymock.EasyMock.expect;
import org.restlet.data.Status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Jalpa Patel
 */
public class ProvidedSitesResourceTest extends ResourceTestCase<ProvidedSitesResource>{
    private SiteDao siteDao;
    private SiteProviderAdapter siteProviderAdapter;

    public void setUp() throws Exception {
        super.setUp();
        siteDao = registerDaoMockFor(SiteDao.class);
        siteProviderAdapter = registerMockFor(SiteProviderAdapter.class);
    }

    @Override
    protected ProvidedSitesResource createResource() {
        ProvidedSitesResource resource = new ProvidedSitesResource();
        resource.setXmlSerializer(xmlSerializer);
        resource.setSiteDao(siteDao);
        resource.setSiteProviderAdapter(siteProviderAdapter);
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
        expect(siteDao.searchSitesBySearchText(searchString)).andReturn(Collections.unmodifiableList(sites));
        expect(siteProviderAdapter.search(searchString)).andReturn(Collections.unmodifiableList(sites));
        expect(xmlSerializer.createDocumentString(sites)).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

}
