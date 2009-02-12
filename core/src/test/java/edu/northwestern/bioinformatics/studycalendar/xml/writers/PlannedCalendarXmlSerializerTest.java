package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.PlannedCalendarDao;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import org.dom4j.Element;
import static org.easymock.EasyMock.expect;

import static java.util.Collections.emptyList;

public class PlannedCalendarXmlSerializerTest extends StudyCalendarXmlTestCase {
    private PlannedCalendarXmlSerializer serializer;
    private PlannedCalendarDao plannedCalendarDao;
    private Element element;
    private PlannedCalendar plannedCalendar;
    private EpochXmlSerializer epochSerializer;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        epochSerializer = registerMockFor(EpochXmlSerializer.class);
        plannedCalendarDao = registerDaoMockFor(PlannedCalendarDao.class);

        Study study = createNamedInstance("Study A", Study.class);

        serializer = new PlannedCalendarXmlSerializer(){
            protected EpochXmlSerializer getEpochSerializer() {
                return epochSerializer;
            }
        };
        serializer.setPlannedCalendarDao(plannedCalendarDao);
        serializer.setStudy(study);

        plannedCalendar = setGridId("grid0", new PlannedCalendar());
    }

    public void testCreateElement() {
        Element actual = serializer.createElement(plannedCalendar);

        assertEquals("Wrong attribute size", 1, actual.attributeCount());
        assertEquals("Wrong grid id", "grid0", actual.attribute("id").getValue());
    }

    public void testReadElementWhenCalendarIsNew() {
        expect(element.getName()).andReturn("planned-calendar");
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(plannedCalendarDao.getByGridId("grid0")).andReturn(null);
        expect(element.elements()).andReturn(emptyList());
        replayMocks();

        PlannedCalendar actual = serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong grid id", "grid0", actual.getGridId());
    }

    public void testReadElementWhenCalendarExists() {
        expect(element.getName()).andReturn("planned-calendar");
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(plannedCalendarDao.getByGridId("grid0")).andReturn(plannedCalendar);
        replayMocks();

        PlannedCalendar actual = serializer.readElement(element);
        verifyMocks();

        assertSame("Wrong Planned Calendar", plannedCalendar, actual);
    }
}
