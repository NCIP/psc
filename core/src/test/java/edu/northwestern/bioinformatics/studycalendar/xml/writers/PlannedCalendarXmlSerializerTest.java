package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setGridId;
import org.dom4j.Element;
import static org.easymock.EasyMock.expect;

import static java.util.Collections.emptyList;

public class PlannedCalendarXmlSerializerTest extends StudyCalendarXmlTestCase {
    private PlannedCalendarXmlSerializer serializer;
    private Element element;
    private PlannedCalendar plannedCalendar;
    private EpochXmlSerializer epochSerializer;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        epochSerializer = registerMockFor(EpochXmlSerializer.class);
        Study study = createNamedInstance("Study A", Study.class);

        serializer = new PlannedCalendarXmlSerializer(){
            protected EpochXmlSerializer getEpochSerializer() {
                return epochSerializer;
            }
        };
        serializer.setStudy(study);

        plannedCalendar = setGridId("grid0", new PlannedCalendar());
    }

    public void testCreateElement() {
        Element actual = serializer.createElement(plannedCalendar);

        assertEquals("Wrong attribute size", 1, actual.attributeCount());
        assertEquals("Wrong grid id", "grid0", actual.attribute("id").getValue());
    }

    public void testReadElement() {
        expect(element.getName()).andReturn("planned-calendar");
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(element.elements()).andReturn(emptyList());
        replayMocks();

        PlannedCalendar actual = serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong grid id", "grid0", actual.getGridId());
    }
}
