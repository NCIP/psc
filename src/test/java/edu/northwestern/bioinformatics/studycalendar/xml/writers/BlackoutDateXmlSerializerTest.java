package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer.*;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

import static java.text.MessageFormat.format;

/**
 * @author Saurabh Agrawal
 */
public class BlackoutDateXmlSerializerTest extends StudyCalendarXmlTestCase {
    private BlackoutDateXmlSerializer serializer;
    private Element dayOfTheWeekHolidayElement, relativeRecurringHolidayElement, monthDayHolidayElement;
    private Site site;

    private DayOfTheWeek dayOfTheWeek;

    private RelativeRecurringHoliday relativeRecurringHoliday;

    private MonthDayHoliday monthDayHoliday;

    protected void setUp() throws Exception {
        super.setUp();


        site = createNamedInstance("Northwestern University", Site.class);
        site.setAssignedIdentifier("assigned id");
        site.setId(1);
        serializer = new BlackoutDateXmlSerializer(site);


        dayOfTheWeek = new DayOfTheWeek();
        dayOfTheWeek.setId(1);
        dayOfTheWeek.setDayOfTheWeek("Sunday");
        dayOfTheWeek.setDescription("day of the week");

        relativeRecurringHoliday = new RelativeRecurringHoliday();
        relativeRecurringHoliday.setDayOfTheWeek("Monday");
        relativeRecurringHoliday.setMonth(2);
        relativeRecurringHoliday.setWeekNumber(4);
        relativeRecurringHoliday.setDescription("relative recurring holiday");
        relativeRecurringHoliday.setId(2);

        monthDayHoliday = new MonthDayHoliday();
        monthDayHoliday.setDay(2);
        monthDayHoliday.setMonth(1);
        monthDayHoliday.setYear(2008);
        monthDayHoliday.setDescription("month day holiday");
        monthDayHoliday.setId(3);

        site.getHolidaysAndWeekends().add(dayOfTheWeek);
        site.getHolidaysAndWeekends().add(monthDayHoliday);
        site.getHolidaysAndWeekends().add(relativeRecurringHoliday);

    }

    public void testCreateElement() {
        Element actual = serializer.createElement(dayOfTheWeek);
        assertEquals("Wrong element name", XsdElement.BLACKOUT_DATES.xmlName(), actual.getName());
        assertEquals("Wrong number of children", 1, actual.elements().size());
        Element actualElement = (Element) actual.elements().get(0);


        assertEquals("Wrong day-of-the-week", dayOfTheWeek.getDayOfTheWeek(), actualElement.attributeValue("day-of-the-week"));
        assertEquals("Wrong description", dayOfTheWeek.getDescription(), actualElement.attributeValue("description"));
        assertEquals("Wrong site", site.getId() + "", actualElement.attributeValue("site_id"));

        actualElement = serializer.createElement(relativeRecurringHoliday, true);

        assertEquals("Wrong day-of-the-week", relativeRecurringHoliday.getDayOfTheWeek(), actualElement.attributeValue("day-of-the-week"));
        assertEquals("Wrong description", relativeRecurringHoliday.getDescription(), actualElement.attributeValue("description"));
        assertEquals("Wrong month ", relativeRecurringHoliday.getMonth() + "", actualElement.attributeValue("month"));
        assertEquals("Wrong week-number", relativeRecurringHoliday.getWeekNumber() + "", actualElement.attributeValue("week-number"));


        actualElement = serializer.createElement(monthDayHoliday, true);

        assertEquals("Wrong year", monthDayHoliday.getYear() + "", actualElement.attributeValue("year"));
        assertEquals("Wrong description", monthDayHoliday.getDescription(), actualElement.attributeValue("description"));
        assertEquals("Wrong month ", monthDayHoliday.getMonth() + "", actualElement.attributeValue("month"));
        assertEquals("Wrong day", monthDayHoliday.getDay() + "", actualElement.attributeValue("day"));


    }

    public void testReadElementForHoliday() {

        dayOfTheWeekHolidayElement = serializer.createElement(dayOfTheWeek, true);
        relativeRecurringHolidayElement = serializer.createElement(relativeRecurringHoliday, true);
        monthDayHolidayElement = serializer.createElement(monthDayHoliday, true);

        Holiday newDayOfTheWeekholiday = serializer.readElement(dayOfTheWeekHolidayElement);

        assertEquals("day of week holiday should be the same", dayOfTheWeek, newDayOfTheWeekholiday);

        Holiday newMonthDayHoliday = serializer.readElement(monthDayHolidayElement);

        assertEquals("month day holiday should be the same", monthDayHoliday, newMonthDayHoliday);

        Holiday newRelativeRecurringHoliday = serializer.readElement(relativeRecurringHolidayElement);

        assertEquals("relative recurring holiday should be the same", relativeRecurringHoliday, newRelativeRecurringHoliday);

    }

    public void testReadElementForNonExistantHoliday() {
        site.getHolidaysAndWeekends().remove(monthDayHoliday);
        try {
            monthDayHolidayElement = serializer.createElement(monthDayHoliday, true);

            serializer.readElement(monthDayHolidayElement);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException e) {
            assertEquals("No Holday existis with id:" + monthDayHoliday.getId() + " at the site:" + site.getId(),
                    e.getMessage());
        }
    }

    public void testReadElementForNullHolidayId() {
        monthDayHoliday.setId(null);

        try {
            monthDayHolidayElement = serializer.createElement(monthDayHoliday, true);

            serializer.readElement(monthDayHolidayElement);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("Element must have id attribute",
                    scve.getMessage());
        }
    }

    public void testCreateOrReadElementForNullSiteOrElement() {
        //first check for null element
        try {
            serializer.createElement(null, true);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("holiday can not be null",
                    scve.getMessage());
        }

        try {
            serializer.readElement(null);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("element can not be null",
                    scve.getMessage());
        }
        //now check for site

        try {
            serializer = new BlackoutDateXmlSerializer(null);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("site can not be null",
                    scve.getMessage());
        }

    }


    public void testCreateDocumentStringForMonthDayHoliday() throws Exception {

        StringBuffer expected = new StringBuffer();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        expected.append("<blackout-dates ");
        expected.append(format("       {0}=\"{1}\"", SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS));
        expected.append(format("       {0}:{1}=\"{2} {3}\"", SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, AbstractStudyCalendarXmlSerializer.SCHEMA_LOCATION));
        expected.append(format("       {0}:{1}=\"{2}\">", SCHEMA_NAMESPACE_ATTRIBUTE, XML_SCHEMA_ATTRIBUTE, XSI_NS));

        expected.append(format("<blackout-date  id=\"{0}\" description=\"{1}\" site_id=\"{2}\" day=\"{3}\" month=\"{4}\" year=\"2008\"/>", monthDayHoliday.getId(),
                monthDayHoliday.getDescription(), site.getId(), monthDayHoliday.getDay(), monthDayHoliday.getMonth()));

        expected.append("</blackout-dates>");


        String actual = serializer.createDocumentString(monthDayHoliday);
        verifyMocks();

        assertXMLEqual(expected.toString(), actual);
    }

    public void testCreateDocumentStringForRelativeRecurringHoliday() throws Exception {

        StringBuffer expected = new StringBuffer();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        expected.append("<blackout-dates ");
        expected.append(format("       {0}=\"{1}\"", SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS));
        expected.append(format("       {0}:{1}=\"{2} {3}\"", SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, AbstractStudyCalendarXmlSerializer.SCHEMA_LOCATION));
        expected.append(format("       {0}:{1}=\"{2}\">", SCHEMA_NAMESPACE_ATTRIBUTE, XML_SCHEMA_ATTRIBUTE, XSI_NS));

        expected.append(format("<blackout-date  id=\"{0}\" description=\"{1}\" site_id=\"{2}\" day-of-the-week=\"{3}\" week-number=\"{4}\" month=\"{5}\" />",
                relativeRecurringHoliday.getId(), relativeRecurringHoliday.getDescription(), site.getId(),
                relativeRecurringHoliday.getDayOfTheWeek(), relativeRecurringHoliday.getWeekNumber(), relativeRecurringHoliday.getMonth()));

        expected.append("</blackout-dates>");


        String actual = serializer.createDocumentString(relativeRecurringHoliday);
        verifyMocks();

        assertXMLEqual(expected.toString(), actual);
    }

    public void testCreateDocumentStringFordayOfWeekHoliday() throws Exception {

        StringBuffer expected = new StringBuffer();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        expected.append("<blackout-dates ");
        expected.append(format("       {0}=\"{1}\"", SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS));
        expected.append(format("       {0}:{1}=\"{2} {3}\"", SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, AbstractStudyCalendarXmlSerializer.SCHEMA_LOCATION));
        expected.append(format("       {0}:{1}=\"{2}\">", SCHEMA_NAMESPACE_ATTRIBUTE, XML_SCHEMA_ATTRIBUTE, XSI_NS));

        expected.append(format("<blackout-date  id=\"{0}\" description=\"{1}\" site_id=\"{2}\" day-of-the-week=\"{3}\" />", dayOfTheWeek.getId(),
                dayOfTheWeek.getDescription(), site.getId(), dayOfTheWeek.getDayOfTheWeek()));

        expected.append("</blackout-dates>");


        String actual = serializer.createDocumentString(dayOfTheWeek);
        verifyMocks();

        assertXMLEqual(expected.toString(), actual);
    }
}

