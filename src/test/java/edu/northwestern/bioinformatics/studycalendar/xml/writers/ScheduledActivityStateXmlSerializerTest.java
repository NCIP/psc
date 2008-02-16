package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import edu.nwu.bioinformatics.commons.DateUtils;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;

import static java.util.Calendar.JANUARY;

/**
 * @author John Dzak
 */
public class ScheduledActivityStateXmlSerializerTest extends StudyCalendarXmlTestCase {
    private ScheduledActivityStateXmlSerializer serializer;
    private Scheduled state;

    protected void setUp() throws Exception {
        super.setUp();

        serializer = new ScheduledActivityStateXmlSerializer();

        state = new Scheduled();
        state.setDate(DateUtils.createDate(2008, JANUARY, 5));
        state.setReason("some reason");
    }

    public void testCreateElement() {
        Element actual = serializer.createElement(state);
        assertEquals("Wrong element name", "scheduled-activity-state", actual.getName());
        assertEquals("Wrong reason", "some reason", actual.attributeValue("reason"));
        assertEquals("Wrong date", "2008-01-05", actual.attributeValue("date"));
        assertEquals("Wrong state", "scheduled", actual.attributeValue("state"));
    }

    public void testReadElement() {
        try {
            serializer.readElement(new BaseElement("scheduled-activity-state"));
            fail("Exception should be thrown, method not implemented");
        } catch(UnsupportedOperationException success) {
            assertEquals("Functionality to read a scheduled activity state element does not exist", success.getMessage());
        }
    }
}
