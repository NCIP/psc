package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import org.dom4j.Element;

public class PlannedCalendarDeltaXmlSerializerTest extends StudyCalendarXmlTestCase {
    private AbstractDeltaXmlSerializer serializer;
    private Delta plannedCalendarDelta;

    protected void setUp() throws Exception {
        super.setUp();

        serializer = new PlannedCalendarDeltaXmlSerializer();

        plannedCalendarDelta = Delta.createDeltaFor(setGridId("grid1", new PlannedCalendar()), new Add());
        plannedCalendarDelta.setGridId("grid0");
    }

    public void testCreateElement() {
        Element element = serializer.createElement(plannedCalendarDelta);

        assertEquals("Wrong element name", "planned-calendar-delta", element.getName());
        assertEquals("Wrong node id", "grid1", element.attributeValue("node-id"));
        assertEquals("Wrong id", "grid0", element.attributeValue("id"));
    }

    public void testReadElementWithExistingAmendment() {

    }

    public void testReadElementWithNewAmendment() {
        
    }
}
