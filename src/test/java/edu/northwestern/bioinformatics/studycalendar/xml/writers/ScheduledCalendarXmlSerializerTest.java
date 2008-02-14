package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import org.dom4j.Element;

/**
 * @author John Dzak
 */
public class ScheduledCalendarXmlSerializerTest extends StudyCalendarXmlTestCase {
    private ScheduledCalendar schedule;
    private ScheduledCalendarXmlSerializer serializer;

    protected void setUp() throws Exception {
        super.setUp();

        serializer = new ScheduledCalendarXmlSerializer();

        schedule = new ScheduledCalendar();
    }

    public void testCreateElement() {
        Element actual = serializer.createElement(schedule);
        
        assertEquals("Wrong element name", "scheduled-calendar", actual.getName());
    }

    public void testReadElement() {
        assertTrue(true);
    }
}
