package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.MonthDayHoliday;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import static org.easymock.EasyMock.expect;
import org.restlet.data.MediaType;
import org.restlet.data.Status;

/**
 * @author Saurabh Agrawal
 */
public class BlackoutDatesResourceTest extends ResourceTestCase<BlackoutDatesResource> {

    public static final String SITE_IDENTIFIER = "site_id";

    public static final String SITE_NAME = "site_name";

    private SiteService siteService;

    private Site site;
    private MonthDayHoliday monthDayHoliday;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        siteService = registerMockFor(SiteService.class);
        request.getAttributes().put(UriTemplateParameters.SITE_IDENTIFIER.attributeName(), SITE_IDENTIFIER);

        site = Fixtures.createNamedInstance(SITE_NAME, Site.class);
        site.setAssignedIdentifier(SITE_IDENTIFIER);

        monthDayHoliday = new MonthDayHoliday();
        monthDayHoliday.setDay(2);
        monthDayHoliday.setMonth(1);
        monthDayHoliday.setYear(2008);
        monthDayHoliday.setDescription("month day holiday");
        monthDayHoliday.setId(3);
        site.getHolidaysAndWeekends().add(monthDayHoliday);

    }

    @Override
    protected BlackoutDatesResource createResource() {
        BlackoutDatesResource resource = new BlackoutDatesResource();
        resource.setSiteService(siteService);
        return resource;
    }

    public void testGetAllowed() throws Exception {
        assertAllowedMethods("GET");
    }


    public void testGetXmlForAllBlackoutDates() throws Exception {
        expect(siteService.getByAssignedIdentifier(SITE_IDENTIFIER)).andReturn(site);

        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "\n" +
                "<blackout-dates xmlns=\"http://bioinformatics.northwestern.edu/ns/psc\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://bioinformatics.northwestern.edu/ns/psc http://bioinformatics.northwestern.edu/ns/psc/psc.xsd\">\n" +
                "  <blackout-date id=\"3\" description=\"month day holiday\" day=\"2\" month=\"1\" year=\"2008\"/>\n" +
                "</blackout-dates>\n";

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        assertEquals("Result is not right content type", MediaType.TEXT_XML, response.getEntity().getMediaType());
        String actualEntityBody = response.getEntity().getText();
        assertEquals("Wrong text", expectedXml, actualEntityBody);
    }

}
