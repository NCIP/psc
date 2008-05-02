package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import org.dom4j.Element;
import static org.easymock.EasyMock.expect;

import static java.util.Collections.emptyList;

public class PeriodXmlSerializerTest extends StudyCalendarXmlTestCase {
    private PeriodXmlSerializer serializer;
    private PeriodDao periodDao;
    private Element element;
    private Period period;
    private PlannedActivityXmlSerializer plannedActivitySerializer;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        periodDao = registerDaoMockFor(PeriodDao.class);
        plannedActivitySerializer = registerMockFor(PlannedActivityXmlSerializer.class);

        Study study = createNamedInstance("Study A", Study.class);
        
        serializer = new PeriodXmlSerializer(){
            protected AbstractPlanTreeNodeXmlSerializer getChildSerializer() {
                return plannedActivitySerializer;
            }
        };
        serializer.setPeriodDao(periodDao);
        serializer.setStudy(study);

        period = setGridId("grid0", Fixtures.createPeriod("Period A", 1, 7, 3));
    }

    public void testCreateElementEpoch() {
        Element actual = serializer.createElement(period);

        assertEquals("Wrong attribute size", 6, actual.attributeCount());
        assertEquals("Wrong grid id", "grid0", actual.attribute("id").getValue());
        assertEquals("Wrong period name", "Period A", actual.attribute("name").getValue());
        assertEquals("Wrong period duration unit", "day", actual.attributeValue("duration-unit"));
        assertEquals("Wrong period duration quantity", "7", actual.attributeValue("duration-quantity"));
        assertEquals("Wrong period start day", "1", actual.attributeValue("start-day"));
        assertEquals("Wrong period repetitions", "3", actual.attributeValue("repetitions"));

    }

    public void testReadElementEpoch() {
        expect(element.getName()).andReturn("period");
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(periodDao.getByGridId("grid0")).andReturn(null);
        expect(element.attributeValue("name")).andReturn("Period A");
        expect(element.attributeValue("duration-unit")).andReturn("day");
        expect(element.attributeValue("duration-quantity")).andReturn("7");
        expect(element.attributeValue("start-day")).andReturn("1");
        expect(element.attributeValue("repetitions")).andReturn("3");
        expect(element.elements()).andReturn(emptyList());
        replayMocks();

        Period actual = (Period) serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong grid id", "grid0", actual.getGridId());
        assertEquals("Wrong period name", "Period A", actual.getName());
        assertEquals("Wrong period duration unit", "day", actual.getDuration().getUnit().toString());
        assertEquals("Wrong period duration quantity",  new Integer(7), actual.getDuration().getQuantity());
        assertEquals("Wrong period start day", new Integer(1), actual.getStartDay());
        assertEquals("Wrong period repetitions", 3, actual.getRepetitions());
    }

    public void testReadElementExistsEpoch() {
        expect(element.getName()).andReturn("period");
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(periodDao.getByGridId("grid0")).andReturn(period);
        replayMocks();

        Period actual = (Period) serializer.readElement(element);
        verifyMocks();

        assertSame("Wrong Period", period, actual);
    }
}
