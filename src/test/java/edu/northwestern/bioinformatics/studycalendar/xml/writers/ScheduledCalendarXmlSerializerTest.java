package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;

/**
 * @author John Dzak
 */
public class ScheduledCalendarXmlSerializerTest extends StudyCalendarXmlTestCase {
    private ScheduledCalendar schedule;
    private ScheduledCalendarXmlSerializer serializer;

    protected void setUp() throws Exception {
        super.setUp();

        serializer = new ScheduledCalendarXmlSerializer();

        StudySubjectAssignment assignment = setGridId("assignment-grid-0", new StudySubjectAssignment());

        schedule = setGridId("schedule-grid-0", new ScheduledCalendar());
        schedule.setAssignment(assignment);
    }

    public void testCreateElement() {
        Element actual = serializer.createElement(schedule);
        
        assertEquals("Wrong element name", "scheduled-calendar", actual.getName());
        assertEquals("Wrong id", "schedule-grid-0", actual.attributeValue("id"));
        assertEquals("Wrong assignment id", "assignment-grid-0", actual.attributeValue("assignment-id"));
    }

    public void testReadElement() {
        try {
            serializer.readElement(new BaseElement("schedule"));
            fail("Exception should be thrown, method not implemented");
        } catch(UnsupportedOperationException success) {
            assertEquals("Functionality to read a schedule element does not exist", success.getMessage());
        }
    }
}
