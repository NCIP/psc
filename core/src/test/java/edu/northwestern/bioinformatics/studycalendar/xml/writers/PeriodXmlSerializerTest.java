package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import static org.easymock.EasyMock.expect;

import static java.util.Collections.emptyList;
import java.util.SortedSet;
import java.util.TreeSet;

public class PeriodXmlSerializerTest extends StudyCalendarXmlTestCase {
    private PeriodXmlSerializer serializer;
    private PeriodDao periodDao;
    private Element element;
    private Period period;
    private PlannedActivityXmlSerializer plannedActivitySerializer;
    private SortedSet<Period> periods;


    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        periodDao = registerDaoMockFor(PeriodDao.class);
        plannedActivitySerializer = registerMockFor(PlannedActivityXmlSerializer.class);

        Study study = createNamedInstance("Study A", Study.class);

        serializer = new PeriodXmlSerializer() {
            protected AbstractPlanTreeNodeXmlSerializer getChildSerializer() {
                return plannedActivitySerializer;
            }
        };
        serializer.setPeriodDao(periodDao);
        serializer.setStudy(study);

        period = setGridId("grid0", Fixtures.createPeriod("Period A", 1, 7, 3));

        periods = new TreeSet<Period>();
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
        assertEquals("Wrong period duration quantity", new Integer(7), actual.getDuration().getQuantity());
        assertEquals("Wrong period start day", new Integer(1), actual.getStartDay());
        assertEquals("Wrong period repetitions", 3, actual.getRepetitions());
    }

    public void testReadElementWithDurationUnitAsMonth() throws Exception {
        expect(element.getName()).andReturn("period");
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(periodDao.getByGridId("grid0")).andReturn(null);
        expect(element.attributeValue("name")).andReturn("Period A");
        expect(element.attributeValue("duration-unit")).andReturn("month");
        expect(element.attributeValue("duration-quantity")).andReturn("7");
        expect(element.attributeValue("start-day")).andReturn("1");
        expect(element.attributeValue("repetitions")).andReturn("3");
        expect(element.elements()).andReturn(emptyList());
        replayMocks();

        Period actual = (Period) serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong grid id", "grid0", actual.getGridId());
        assertEquals("Wrong period name", "Period A", actual.getName());
        assertEquals("Wrong period duration unit", "month", actual.getDuration().getUnit().toString());
        assertEquals("Wrong period duration quantity", new Integer(7), actual.getDuration().getQuantity());
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

    public void testValidateElement() throws Exception {
        Period period = createPeriod();
        Element actual = serializer.createElement(period);
        assertTrue(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));
        period.setGridId("wrong grid id");
        assertFalse(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));


        period = createPeriod();
        assertTrue(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));
        period.setDuration(null);
        assertFalse(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));


        period = createPeriod();
        assertTrue(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));
        period.getDuration().setQuantity(5);
        assertFalse(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));


        period = createPeriod();
        assertTrue(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));
        period.setDuration(null);
        assertFalse(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));


        period = createPeriod();
        assertTrue(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));
        period.getDuration().setUnit(Duration.Unit.fortnight);
        assertFalse(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));


        period = createPeriod();
        assertTrue(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));
        period.setName("wrong name");
        assertFalse(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));


        period = createPeriod();
        assertTrue(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));
        period.setStartDay(7);
        assertFalse(StringUtils.isBlank(serializer.validateElement(period, actual).toString()));

    }

    public void testGetPeriodWithMatchingAttributes() throws Exception {
        Element actual = serializer.createElement(period);

        Period period = createPeriod();
        assertNotNull(serializer.getPeriodWithMatchingGridId(periods, actual));
        assertEquals("serializer must have a matching period", period, serializer.getPeriodWithMatchingGridId(periods, actual));
        period.setGridId("wrong grid id");
        assertNull(serializer.getPeriodWithMatchingGridId(periods, actual));


    }


    private Period createPeriod() {
        Period period = setGridId("grid0", Fixtures.createPeriod("Period A", 1, 7, 3));
        periods.clear();
        periods.add(period);
        return period;
    }
}
