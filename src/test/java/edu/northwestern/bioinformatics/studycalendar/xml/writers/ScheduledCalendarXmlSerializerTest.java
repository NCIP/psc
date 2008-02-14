package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;

/**
 * @author John Dzak
 */
public class ScheduledCalendarXmlSerializerTest extends StudyCalendarXmlTestCase {
    private ScheduledCalendar schedule;

    protected void setUp() throws Exception {
        super.setUp();

        schedule = new ScheduledCalendar();
    }

    public void testCreateElement() {
        assertTrue(true);
//        serializer.createElement(schedule);
    }

    public void testReadElement() {
        assertTrue(true);
    }
}
