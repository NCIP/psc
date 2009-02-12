package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import static org.easymock.EasyMock.expect;
import org.restlet.data.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Saurabh Agrawal
 */
public class SitesResourceTest extends ResourceTestCase<SitesResource> {


    private SiteDao siteDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        siteDao = registerDaoMockFor(SiteDao.class);
    }

    @Override
    protected SitesResource createResource() {
        SitesResource resource = new SitesResource();
        resource.setSiteDao(siteDao);
        resource.setXmlSerializer(xmlSerializer);
        return resource;
    }

    public void testGetAllowed() throws Exception {
        assertAllowedMethods("GET");
    }


    public void testGetXmlForAllActivities() throws Exception {
        List<Site> sites = new ArrayList<Site>();
        Site site = new Site();
        sites.add(site);
        expect(siteDao.getAll()).andReturn(sites);

        expect(xmlSerializer.createDocumentString(sites)).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

}
