/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.dao.BlackoutDateDao;
import edu.northwestern.bioinformatics.studycalendar.domain.BlackoutDate;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.SpecificDateBlackout;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import org.restlet.data.Method;
import org.restlet.data.Status;

import java.util.ArrayList;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static org.easymock.EasyMock.expect;

/**
 * @author Saurabh Agrawal
 */
public class BlackoutDatesResourceTest extends AuthorizedResourceTestCase<BlackoutDatesResource> {

    public static final String SITE_IDENTIFIER = "site_id";

    public static final String SITE_NAME = "site_name";

    private SiteService siteService;
    private BlackoutDateDao blackoutDateDao;
    private Site site;
    private SpecificDateBlackout monthDayHoliday;
    private List<BlackoutDate> blackoutDates = new ArrayList<BlackoutDate>();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        siteService = registerMockFor(SiteService.class);
        blackoutDateDao = registerDaoMockFor(BlackoutDateDao.class);
        request.getAttributes().put(UriTemplateParameters.SITE_IDENTIFIER.attributeName(), SITE_IDENTIFIER);

        site = Fixtures.createNamedInstance(SITE_NAME, Site.class);
        site.setAssignedIdentifier(SITE_IDENTIFIER);

        monthDayHoliday = new SpecificDateBlackout();
        monthDayHoliday.setDay(2);
        monthDayHoliday.setMonth(1);
        monthDayHoliday.setYear(2008);
        monthDayHoliday.setDescription("month day holiday");
        monthDayHoliday.setId(3);
        monthDayHoliday.setGridId("blackoutDateId");
        monthDayHoliday.setSite(site);
        site.getBlackoutDates().add(monthDayHoliday);
        blackoutDates.add(monthDayHoliday);

    }

    @Override
    protected BlackoutDatesResource createAuthorizedResource() {
        BlackoutDatesResource resource = new BlackoutDatesResource();
        resource.setSiteService(siteService);
        resource.setXmlSerializer(xmlSerializer);
        resource.setBlackoutDateDao(blackoutDateDao);
        return resource;
    }

    public void testGetAndPostAllowed() throws Exception {
        assertAllowedMethods("GET", "POST");
    }
    
    public void testGetWithAuthorizedRoles() {
        assertRolesAllowedForMethod(Method.GET,
            PERSON_AND_ORGANIZATION_INFORMATION_MANAGER,
            DATA_READER);
    }

    public void testPostWithAuthorizedRoles() {
        assertRolesAllowedForMethod(Method.POST,
            PERSON_AND_ORGANIZATION_INFORMATION_MANAGER);
    }

    public void testPostBlackoutDateToSite() throws Exception {
        expectReadXmlFromRequestAs(monthDayHoliday);
        expect(siteService.getByAssignedIdentifier(SITE_IDENTIFIER)).andReturn(site);
        expect(siteService.resolveSiteForBlackoutDate(monthDayHoliday)).andReturn(monthDayHoliday);
        blackoutDateDao.save(monthDayHoliday);

        doPost();
        assertResponseStatus(Status.SUCCESS_CREATED);
        assertEquals(ROOT_URI + "/sites/site_id/blackout-dates/blackoutDateId",
                response.getLocationRef().getTargetRef().toString());
    }

    public void testGet400IfSiteIsNull() throws Exception {
        UriTemplateParameters.SITE_IDENTIFIER.removeFrom(request);

        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    public void testGet400IfSiteIsUnknown() throws Exception {
        request.getAttributes().put(UriTemplateParameters.SITE_IDENTIFIER.attributeName(),"UnknownSite");
        expect(siteService.getByAssignedIdentifier("UnknownSite")).andReturn(null);
        
        doGet();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    public void testGetBlackoutDatesForSite() throws Exception {
        expectFoundSite(site);
        expect(xmlSerializer.createDocumentString(blackoutDates)).andReturn(MOCK_XML);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertResponseIsCreatedXml();
    }

    private void expectFoundSite(final Site expectedSite) {
        expect(siteService.getByAssignedIdentifier(SITE_IDENTIFIER)).andReturn(expectedSite);
    }
}
