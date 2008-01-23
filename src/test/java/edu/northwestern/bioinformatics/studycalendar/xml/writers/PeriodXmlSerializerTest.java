package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import org.dom4j.Element;
import static org.easymock.EasyMock.expect;

import static java.util.Collections.emptyList;

public class PeriodXmlSerializerTest extends StudyCalendarXmlTestCase {
    private PeriodXmlSerializer serializer;
    private PeriodDao periodDao;
    private Element element;
    private Period period;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        periodDao = registerDaoMockFor(PeriodDao.class);

        Study study = createNamedInstance("Study A", Study.class);
        serializer = new PeriodXmlSerializer(study);
        serializer.setPeriodDao(periodDao);

        period = setGridId("grid0", createNamedInstance("Period A", Period.class));
    }

    public void testCreateElementEpoch() {
        Element actual = serializer.createElement(period);

        assertEquals("Wrong attribute size", 2, actual.attributeCount());
        assertEquals("Wrong grid id", "grid0", actual.attribute("id").getValue());
        assertEquals("Wrong period name", "Period A", actual.attribute("name").getValue());
    }

    public void testReadElementEpoch() {
        expect(element.getName()).andReturn("period");
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(periodDao.getByGridId("grid0")).andReturn(null);
        expect(element.attributeValue("name")).andReturn("Period A");
        expect(element.elements()).andReturn(emptyList());
        replayMocks();

        Period actual = (Period) serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong grid id", "grid0", actual.getGridId());
        assertEquals("Wrong period name", "Period A", actual.getName());
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
