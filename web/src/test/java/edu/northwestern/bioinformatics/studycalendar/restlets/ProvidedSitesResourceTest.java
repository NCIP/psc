package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.SiteConsumer;
import static org.easymock.EasyMock.expect;
import org.restlet.data.Status;

import java.util.List;
import java.util.Arrays;

/**
 * @author Jalpa Patel
 */
public class ProvidedSitesResourceTest extends ResourceTestCase<ProvidedSitesResource>{
    private SiteConsumer siteConsumer;

    public void setUp() throws Exception {
        super.setUp();
        siteConsumer = registerMockFor(SiteConsumer.class);
    }

    @Override
    protected ProvidedSitesResource createResource() {
        ProvidedSitesResource resource = new ProvidedSitesResource();
        resource.setXmlSerializer(xmlSerializer);
        resource.setSiteConsumer(siteConsumer);
        return resource;
    }

    public void testGetAllowed() throws Exception {
        assertAllowedMethods("GET");
    }

    public void testGetAllSites() throws Exception {
        String searchString = "s";
        QueryParameters.Q.putIn(request, searchString);
        Site site = Fixtures.createSite("site");
        List<Site> expectedSites = Arrays.asList(site);
        expect(siteConsumer.search(searchString)).andReturn(expectedSites);
        expect(xmlSerializer.createDocumentString(expectedSites)).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

}
