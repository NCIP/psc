package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.SpecificDateBlackout;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import static org.easymock.EasyMock.expect;
import org.restlet.data.Status;

/**
 * @author Saurabh Agrawal
 */
public class BlackoutDateResourceTest extends ResourceTestCase<BlackoutDateResource> {

    public static final String SITE_IDENTIFIER = "site_id";

    public static final String BLACKOUT_DATE_IDENTIFIER = "blackoutDateId";

    public static final String SITE_NAME = "site_name";

    private SiteService siteService;

    private Site site;
    private SpecificDateBlackout monthDayHoliday;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        siteService = registerMockFor(SiteService.class);
        request.getAttributes().put(UriTemplateParameters.SITE_IDENTIFIER.attributeName(), SITE_IDENTIFIER);
        request.getAttributes().put(UriTemplateParameters.BLACKOUT_DATE_IDENTIFIER.attributeName(), BLACKOUT_DATE_IDENTIFIER);

        site = Fixtures.createNamedInstance(SITE_NAME, Site.class);
        site.setAssignedIdentifier(SITE_IDENTIFIER);

        monthDayHoliday = new SpecificDateBlackout();
        monthDayHoliday.setDay(2);
        monthDayHoliday.setMonth(1);
        monthDayHoliday.setYear(2008);
        monthDayHoliday.setDescription("month day holiday");
        monthDayHoliday.setId(3);
        site.getBlackoutDates().add(monthDayHoliday);

    }

    @Override
    protected BlackoutDateResource createResource() {
        BlackoutDateResource resource = new BlackoutDateResource();
        resource.setSiteService(siteService);
        return resource;
    }

    public void testDeleteAllowed() throws Exception {
        assertAllowedMethods("DELETE");
    }


    public void testDeleteHolidayWhichDoesNotExists() throws Exception {
        expectFoundSite(site);
        doDelete();
        assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }


    public void testDeleteHolidayWhichExists() throws Exception {
        monthDayHoliday.setGridId("blackoutDateId");
        expectFoundSite(site);
        expect(siteService.createOrUpdateSite(site)).andReturn(site);
        doDelete();
        assertResponseStatus(Status.SUCCESS_OK);
    }


    private void expectFoundSite(Site expectedSite) {
        expect(siteService.getByAssignedIdentifier(SITE_IDENTIFIER)).andReturn(expectedSite);
    }


}

