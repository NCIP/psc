/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.SiteListJsonRepresentation;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.SiteConsumer;
import org.restlet.data.MediaType;
import org.restlet.data.Status;

import java.util.Arrays;
import java.util.List;

import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class ProvidedSitesResourceTest extends AuthorizedResourceTestCase<ProvidedSitesResource>{
    private SiteConsumer siteConsumer;

    public void setUp() throws Exception {
        super.setUp();
        siteConsumer = registerMockFor(SiteConsumer.class);
    }

    @Override
    protected ProvidedSitesResource createAuthorizedResource() {
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

   @SuppressWarnings({"unchecked"})
    public void testGetAllProvidedSitesAsJson() throws Exception {
        String expectedQ = "s";
        QueryParameters.Q.putIn(request, expectedQ);
        setAcceptedMediaTypes(MediaType.APPLICATION_JSON);

        Site site = edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createSite("ECOG-2702");
        List<Site> expectedSites = Arrays.asList(site);

        expect(siteConsumer.search(expectedQ)).andReturn(expectedSites);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertTrue("Response entity is wrong type", response.getEntity() instanceof SiteListJsonRepresentation);
        assertSame("Response entity is for wrong site", expectedSites,
            ((SiteListJsonRepresentation) response.getEntity()).getSites());
    }

}
