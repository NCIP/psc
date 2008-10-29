package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.BlackoutDate;
import static edu.northwestern.bioinformatics.studycalendar.test.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.RelativeRecurringBlackout;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.SpecificDateBlackout;
import edu.northwestern.bioinformatics.studycalendar.domain.WeekdayBlackout;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer.*;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

import java.text.MessageFormat;
import java.util.Collections;

/**
 * @author Saurabh Agrawal
 */
public class BlackoutDateXmlSerializerTest extends StudyCalendarXmlTestCase {
    private BlackoutDateXmlSerializer serializer;
    private Element dayOfTheWeekHolidayElement, relativeRecurringHolidayElement, monthDayHolidayElement;
    private Site site;

    private WeekdayBlackout dayOfTheWeek;

    private RelativeRecurringBlackout relativeRecurringHoliday;

    private SpecificDateBlackout monthDayHoliday;

    @Override
    protected void setUp() throws Exception {
        super.setUp();


        site = createNamedInstance("Northwestern University", Site.class);
        site.setAssignedIdentifier("assigned id");
        site.setId(1);
        serializer = new BlackoutDateXmlSerializer(site);


        dayOfTheWeek = new WeekdayBlackout();
        dayOfTheWeek.setId(1);
        dayOfTheWeek.setDayOfTheWeek("Sunday");
        dayOfTheWeek.setDescription("day of the week");

        relativeRecurringHoliday = new RelativeRecurringBlackout();
        relativeRecurringHoliday.setDayOfTheWeek("Monday");
        relativeRecurringHoliday.setMonth(2);
        relativeRecurringHoliday.setWeekNumber(4);
        relativeRecurringHoliday.setDescription("relative recurring holiday");
        relativeRecurringHoliday.setId(2);

        monthDayHoliday = new SpecificDateBlackout();
        monthDayHoliday.setDay(2);
        monthDayHoliday.setMonth(1);
        monthDayHoliday.setYear(2008);
        monthDayHoliday.setDescription("month day holiday");
        monthDayHoliday.setId(3);

        site.getBlackoutDates().add(dayOfTheWeek);
        site.getBlackoutDates().add(monthDayHoliday);
        site.getBlackoutDates().add(relativeRecurringHoliday);

    }

    // TODO: these should be separate tests
    public void testCreateElement() {
        Element actualElement = serializer.createElement(dayOfTheWeek);
        assertEquals("Wrong element name", XsdElement.BLACKOUT_DATE.xmlName(), actualElement.getName());

        assertEquals("Wrong day-of-the-week", dayOfTheWeek.getDayOfTheWeek(), actualElement.attributeValue("day-of-the-week"));
        assertEquals("Wrong description", dayOfTheWeek.getDescription(), actualElement.attributeValue("description"));
        assertEquals("Wrong site", site.getId() + "", actualElement.attributeValue("site-id"));

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

        BlackoutDate newDayOfTheWeekholiday = serializer.readElement(dayOfTheWeekHolidayElement);

        assertEquals("day of week holiday should be the same", dayOfTheWeek, newDayOfTheWeekholiday);

        BlackoutDate newMonthDayHoliday = serializer.readElement(monthDayHolidayElement);

        assertEquals("month day holiday should be the same", monthDayHoliday, newMonthDayHoliday);

        BlackoutDate newRelativeRecurringHoliday = serializer.readElement(relativeRecurringHolidayElement);

        assertEquals("relative recurring holiday should be the same", relativeRecurringHoliday, newRelativeRecurringHoliday);

    }

    public void testReadElementForNonExistingHoliday() {
        site.getBlackoutDates().remove(monthDayHoliday);
        try {
            monthDayHolidayElement = serializer.createElement(monthDayHoliday, true);

            serializer.readElement(monthDayHolidayElement);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException e) {
            assertEquals("No Holday existis with id:" + monthDayHoliday.getId() + " at the site:" + site.getAssignedIdentifier(),
                    e.getMessage());
        }
    }

    public void testReadElementForNullHolidayId() {
        monthDayHoliday.setId(null);

        monthDayHolidayElement = serializer.createElement(monthDayHoliday, true);

        final BlackoutDate actualBlackoutDate = serializer.readElement(monthDayHolidayElement);
        assertEquals(monthDayHoliday.getDescription(), actualBlackoutDate.getDescription());
        assertTrue(actualBlackoutDate instanceof SpecificDateBlackout);

    }

    public void testCreateOrReadElementForNullSiteOrElement() {
        //first check for null element
        try {
            serializer.createElement(null, true);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("blackoutDate can not be null",
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
        expected.append(MessageFormat.format("       {0}=\"{1}\"", SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS));
        expected.append(MessageFormat.format("       {0}:{1}=\"{2} {3}\"", SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, AbstractStudyCalendarXmlSerializer.SCHEMA_LOCATION));
        expected.append(MessageFormat.format("       {0}:{1}=\"{2}\">", SCHEMA_NAMESPACE_ATTRIBUTE, XML_SCHEMA_ATTRIBUTE, XSI_NS));

        expected.append(MessageFormat.format("<blackout-date id=\"{0}\" description=\"{1}\" site-id=\"{2}\" day=\"{3}\" month=\"{4}\" year=\"2008\"/>", monthDayHoliday.getId(),
                monthDayHoliday.getDescription(), site.getId(), monthDayHoliday.getDay(), monthDayHoliday.getMonth()));

        expected.append("</blackout-dates>");


        String actual = serializer.createDocumentString(Collections.<BlackoutDate>singleton(monthDayHoliday));
        verifyMocks();

        assertXMLEqual(expected.toString(), actual);
    }

    public void testCreateDocumentStringForRelativeRecurringHoliday() throws Exception {

        StringBuffer expected = new StringBuffer();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        expected.append("<blackout-dates ");
        expected.append(MessageFormat.format("       {0}=\"{1}\"", SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS));
        expected.append(MessageFormat.format("       {0}:{1}=\"{2} {3}\"", SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, AbstractStudyCalendarXmlSerializer.SCHEMA_LOCATION));
        expected.append(MessageFormat.format("       {0}:{1}=\"{2}\">", SCHEMA_NAMESPACE_ATTRIBUTE, XML_SCHEMA_ATTRIBUTE, XSI_NS));

        expected.append(MessageFormat.format("<blackout-date id=\"{0}\" description=\"{1}\" site-id=\"{2}\" day-of-the-week=\"{3}\" week-number=\"{4}\" month=\"{5}\" />",
                relativeRecurringHoliday.getId(), relativeRecurringHoliday.getDescription(), site.getId(),
                relativeRecurringHoliday.getDayOfTheWeek(), relativeRecurringHoliday.getWeekNumber(), relativeRecurringHoliday.getMonth()));

        expected.append("</blackout-dates>");


        String actual = serializer.createDocumentString(Collections.<BlackoutDate>singleton(relativeRecurringHoliday));
        verifyMocks();

        assertXMLEqual(expected.toString(), actual);
    }

    public void testCreateDocumentStringFordayOfWeekHoliday() throws Exception {

        StringBuffer expected = new StringBuffer();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        expected.append("<blackout-dates ");
        expected.append(MessageFormat.format("       {0}=\"{1}\"", SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS));
        expected.append(MessageFormat.format("       {0}:{1}=\"{2} {3}\"", SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, AbstractStudyCalendarXmlSerializer.SCHEMA_LOCATION));
        expected.append(MessageFormat.format("       {0}:{1}=\"{2}\">", SCHEMA_NAMESPACE_ATTRIBUTE, XML_SCHEMA_ATTRIBUTE, XSI_NS));

        expected.append(MessageFormat.format("<blackout-date id=\"{0}\" description=\"{1}\" site-id=\"{2}\" day-of-the-week=\"{3}\" />", dayOfTheWeek.getId(),
                dayOfTheWeek.getDescription(), site.getId(), dayOfTheWeek.getDayOfTheWeek()));

        expected.append("</blackout-dates>");


        String actual = serializer.createDocumentString(Collections.<BlackoutDate>singleton(dayOfTheWeek));
        verifyMocks();

        assertXMLEqual(expected.toString(), actual);
    }
}

