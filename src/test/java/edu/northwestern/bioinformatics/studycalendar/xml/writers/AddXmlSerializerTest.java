package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import org.dom4j.Element;

public class AddXmlSerializerTest extends StudyCalendarXmlTestCase {
    private AddXmlSerializer serializer;
    private Add add;

    protected void setUp() throws Exception {
        super.setUp();

        serializer = new AddXmlSerializer();

        add = setGridId("grid0", Add.create(new PlannedCalendar(), 0));
    }

    public void testCreateElement() {
        Element element = serializer.createElement(add);
        
        assertEquals("Wrong grid id", "grid0", element.attributeValue("id"));
        assertEquals("Wrong index", "0", element.attributeValue("index"));
    }
}
