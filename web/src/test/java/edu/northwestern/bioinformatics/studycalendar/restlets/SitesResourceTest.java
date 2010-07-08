package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static org.easymock.EasyMock.expect;
import org.restlet.data.Method;
import org.restlet.data.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Saurabh Agrawal
 */
public class SitesResourceTest extends AuthorizedResourceTestCase<SitesResource> {


    private SiteDao siteDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        siteDao = registerDaoMockFor(SiteDao.class);
    }

    @Override
    protected SitesResource createAuthorizedResource() {
        SitesResource resource = new SitesResource();
        resource.setSiteDao(siteDao);
        resource.setXmlSerializer(xmlSerializer);
        return resource;
    }

    public void testGetAllowed() throws Exception {
        assertAllowedMethods("GET");
    }

    public void testGetWithAuthorizedRoles() {
        assertRolesAllowedForMethod(Method.GET,
            PERSON_AND_ORGANIZATION_INFORMATION_MANAGER,
            STUDY_SITE_PARTICIPATION_ADMINISTRATOR,
            USER_ADMINISTRATOR,
            DATA_READER);
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
