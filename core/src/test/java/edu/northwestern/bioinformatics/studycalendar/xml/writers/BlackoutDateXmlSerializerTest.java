/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.BlackoutDate;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.RelativeRecurringBlackout;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.SpecificDateBlackout;
import edu.northwestern.bioinformatics.studycalendar.domain.WeekdayBlackout;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer.*;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import org.dom4j.Element;

import java.text.MessageFormat;
import java.util.Collections;

/**
 * @author Saurabh Agrawal
 */
public class BlackoutDateXmlSerializerTest extends StudyCalendarXmlTestCase {
    private BlackoutDateXmlSerializer serializer;
    private Element monthDayHolidayElement;
    private Site site;
    private WeekdayBlackout dayOfTheWeek;
    private RelativeRecurringBlackout relativeRecurringHoliday;
    private SpecificDateBlackout monthDayHoliday;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        site = createNamedInstance("Northwestern University", Site.class);
        site.setAssignedIdentifier("NU");
        serializer = new BlackoutDateXmlSerializer();
        dayOfTheWeek = new WeekdayBlackout();
        dayOfTheWeek.setDayOfTheWeek("Sunday");
        dayOfTheWeek.setDescription("day of the week");
        dayOfTheWeek.setSite(site);
        dayOfTheWeek.setGridId("1");

        relativeRecurringHoliday = new RelativeRecurringBlackout();
        relativeRecurringHoliday.setDayOfTheWeek("Monday");
        relativeRecurringHoliday.setMonth(2);
        relativeRecurringHoliday.setWeekNumber(4);
        relativeRecurringHoliday.setDescription("relative recurring holiday");
        relativeRecurringHoliday.setSite(site);
        relativeRecurringHoliday.setGridId("2");

        monthDayHoliday = new SpecificDateBlackout();
        monthDayHoliday.setDay(2);
        monthDayHoliday.setMonth(1);
        monthDayHoliday.setYear(2008);
        monthDayHoliday.setDescription("month day holiday");
        monthDayHoliday.setSite(site);
        monthDayHoliday.setGridId("3");

    }

    public void testCreateElementForWeekdayBlackout() {
        Element actualElement = serializer.createElement(dayOfTheWeek);
        
        assertEquals("Wrong element name", XsdElement.BLACKOUT_DATE.xmlName(), actualElement.getName());
        assertEquals("Wrong day-of-the-week", dayOfTheWeek.getDayOfTheWeek(), actualElement.attributeValue("day-of-the-week"));
        assertEquals("Wrong description", dayOfTheWeek.getDescription(), actualElement.attributeValue("description"));
        assertEquals("Wrong site", site.getAssignedIdentifier(), actualElement.attributeValue("site-identifier"));
    }

    public void testCreateElementForRelativeRecurringBlackout() throws Exception {
        Element actualElement = serializer.createElement(relativeRecurringHoliday, true);

        assertEquals("Wrong day-of-the-week", relativeRecurringHoliday.getDayOfTheWeek(), actualElement.attributeValue("day-of-the-week"));
        assertEquals("Wrong description", relativeRecurringHoliday.getDescription(), actualElement.attributeValue("description"));
        assertEquals("Wrong month ", relativeRecurringHoliday.getMonth().toString(), actualElement.attributeValue("month"));
        assertEquals("Wrong week-number", relativeRecurringHoliday.getWeekNumber().toString(), actualElement.attributeValue("week-number"));
        assertEquals("Wrong site", site.getAssignedIdentifier(), actualElement.attributeValue("site-identifier"));
    }

    public void testCreateElementForSpecificDateBlackout() throws Exception {
        Element actualElement = serializer.createElement(monthDayHoliday, true);

        assertEquals("Wrong year", monthDayHoliday.getYear().toString(), actualElement.attributeValue("year"));
        assertEquals("Wrong description", monthDayHoliday.getDescription(), actualElement.attributeValue("description"));
        assertEquals("Wrong month ", monthDayHoliday.getMonth().toString(), actualElement.attributeValue("month"));
        assertEquals("Wrong day", monthDayHoliday.getDay().toString(), actualElement.attributeValue("day"));
        assertEquals("Wrong site", site.getAssignedIdentifier(), actualElement.attributeValue("site-identifier"));
    }

    public void testReadElementForWeekdayBlackout() throws Exception {
        Element element = XsdElement.BLACKOUT_DATE.create();
        BLACKOUT_DATE_SITE_ID.addTo(element,"NU");
        BLACKOUT_DATE_DESCRIPTION.addTo(element,"WeekdayBlackout");
        BLACKOUT_DATE_DAY_OF_WEEK.addTo(element,"Monday");
        BlackoutDate read = expectReadElement(element);
        assertNotNull(read);
        assertNull(read.getId());
        assertNull(read.getGridId());
        assertEquals("Wrong day of week","Monday",((WeekdayBlackout)read).getDayOfTheWeek());
        assertEquals("Wrong site identifier",site,read.getSite());
        assertEquals("Wrong Description","WeekdayBlackout",read.getDescription());
    }
    
    public void testReadElementRelativeRecurringBlackout() throws Exception {
        Element element = XsdElement.BLACKOUT_DATE.create();
        BLACKOUT_DATE_SITE_ID.addTo(element,"NU");
        BLACKOUT_DATE_DESCRIPTION.addTo(element,"RelativeRecurringBlackout");
        BLACKOUT_DATE_DAY_OF_WEEK.addTo(element,"Monday");
        BLACKOUT_DATE_MONTH.addTo(element,5);
        BLACKOUT_DATE_WEEK_NUMBER.addTo(element,2);
        BlackoutDate read = expectReadElement(element);
        assertNotNull(read);
        assertNull(read.getId());
        assertNull(read.getGridId());
        assertEquals("Wrong day of week","Monday",((RelativeRecurringBlackout)read).getDayOfTheWeek());
        assertEquals("Wrong Month","5",((RelativeRecurringBlackout)read).getMonth().toString());
        assertEquals("Wrong Week number",2,((RelativeRecurringBlackout)read).getDayOfTheWeekInteger());
        assertEquals("Wrong site identifier",site,read.getSite());
        assertEquals("Wrong Description","RelativeRecurringBlackout",read.getDescription());
    }

    public void testReadElementForSpecificDateBlackout() throws Exception {
        Element element = XsdElement.BLACKOUT_DATE.create();
        BLACKOUT_DATE_SITE_ID.addTo(element,"NU");
        BLACKOUT_DATE_DESCRIPTION.addTo(element,"SpecificDateBlackout");
        BLACKOUT_DATE_MONTH.addTo(element,5);
        BLACKOUT_DATE_DAY.addTo(element,4);
        BLACKOUT_DATE_YEAR.addTo(element,2009);
        BlackoutDate read = expectReadElement(element);
        assertNotNull(read);
        assertNull(read.getId());
        assertNull(read.getGridId());
        assertEquals("Wrong Month","5",((SpecificDateBlackout)read).getMonth().toString());
        assertEquals("Wrong Week number","4",((SpecificDateBlackout)read).getDay().toString());
        assertEquals("Wrong Year","2009",((SpecificDateBlackout)read).getYear().toString());
        assertEquals("Wrong site identifier",site,read.getSite());
        assertEquals("Wrong Description","SpecificDateBlackout",read.getDescription());
    }

    public void testReadElementForNullHolidayId() {
        monthDayHoliday.setId(null);

        monthDayHolidayElement = serializer.createElement(monthDayHoliday, true);

        final BlackoutDate actualBlackoutDate = serializer.readElement(monthDayHolidayElement);
        assertEquals(monthDayHoliday.getDescription(), actualBlackoutDate.getDescription());
        assertTrue(actualBlackoutDate instanceof SpecificDateBlackout);

    }

    public void testCreateOrReadElementForNullElement() {
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

    }

    public void testCreateDocumentStringForMonthDayHoliday() throws Exception {

        StringBuffer expected = new StringBuffer();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        expected.append("<blackout-dates ");
        expected.append(MessageFormat.format("       {0}=\"{1}\"", SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS));
        expected.append(MessageFormat.format("       {0}:{1}=\"{2} {3}\"", SCHEMA_NAMESPACE_ATTRIBUTE, SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, AbstractStudyCalendarXmlSerializer.SCHEMA_LOCATION));
        expected.append(MessageFormat.format("       {0}:{1}=\"{2}\">", SCHEMA_NAMESPACE_ATTRIBUTE, XML_SCHEMA_ATTRIBUTE, XSI_NS));

        expected.append(MessageFormat.format("<blackout-date id=\"{0}\" description=\"{1}\" site-identifier=\"{2}\" day=\"{3}\" month=\"{4}\" year=\"2008\"/>", monthDayHoliday.getGridId(),
                monthDayHoliday.getDescription(), site.getAssignedIdentifier(), monthDayHoliday.getDay(), monthDayHoliday.getMonth()));

        expected.append("</blackout-dates>");

        replayMocks();
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

        expected.append(MessageFormat.format("<blackout-date id=\"{0}\" description=\"{1}\" site-identifier=\"{2}\" day-of-the-week=\"{3}\" week-number=\"{4}\" month=\"{5}\" />",
                relativeRecurringHoliday.getGridId(), relativeRecurringHoliday.getDescription(), site.getAssignedIdentifier(),
                relativeRecurringHoliday.getDayOfTheWeek(), relativeRecurringHoliday.getWeekNumber(), relativeRecurringHoliday.getMonth()));

        expected.append("</blackout-dates>");

        replayMocks();
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

        expected.append(MessageFormat.format("<blackout-date id=\"{0}\" description=\"{1}\" site-identifier=\"{2}\" day-of-the-week=\"{3}\" />", dayOfTheWeek.getGridId(),
                dayOfTheWeek.getDescription(), site.getAssignedIdentifier(), dayOfTheWeek.getDayOfTheWeek()));

        expected.append("</blackout-dates>");

        replayMocks();
        String actual = serializer.createDocumentString(Collections.<BlackoutDate>singleton(dayOfTheWeek));
        verifyMocks();

        assertXMLEqual(expected.toString(), actual);
    }

    // Test Helper Method
    public BlackoutDate expectReadElement(Element element) {
        replayMocks();
        BlackoutDate blackoutDate = serializer.readElement(element);
        verifyMocks();
        return blackoutDate;
    }

}

