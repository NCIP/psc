package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.*;
import org.dom4j.Document;
import org.dom4j.Element;
import static org.easymock.EasyMock.expect;

import static java.text.MessageFormat.format;
import static java.util.Collections.emptyList;

public class PeriodXmlSerializerTest extends StudyCalendarXmlTestCase {
    private PeriodXmlSerializer periodXmlSerializer;
    private PeriodDao periodDao;
    private Element element;
    private Period period;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        periodDao = registerDaoMockFor(PeriodDao.class);

        periodXmlSerializer = new PeriodXmlSerializer();
        periodXmlSerializer.setPeriodDao(periodDao);

        period = setGridId("grid0", createNamedInstance("Period A", Period.class));
    }

    public void testCreateElementEpoch() {
        Element actual = periodXmlSerializer.createElement(period);

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

        Period actual = (Period) periodXmlSerializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong grid id", "grid0", actual.getGridId());
        assertEquals("Wrong period name", "Period A", actual.getName());
    }

    public void testReadElementExistsEpoch() {
        expect(element.getName()).andReturn("period");
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(periodDao.getByGridId("grid0")).andReturn(period);
        replayMocks();

        Period actual = (Period) periodXmlSerializer.readElement(element);
        verifyMocks();

        assertSame("Wrong Period", period, actual);
    }

    public void testCreateDocumentEpoch() throws Exception {
        Document actual = periodXmlSerializer.createDocument(period);

        assertEquals("Element should be an period", "period", actual.getRootElement().getName());
        assertEquals("Wrong period grid id", "grid0", actual.getRootElement().attributeValue("id"));
        assertEquals("Wrong period name", "Period A", actual.getRootElement().attributeValue("name"));
    }

    public void testCreateDocumentStringEpoch() throws Exception {
        StringBuffer expected = new StringBuffer();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append(format("<period id=\"{0}\" name=\"{1}\"", period.getGridId(), period.getName()))
                .append(format("       {0}=\"{1}\""     , SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS))
                .append(format("       {0}=\"{1} {2}\""     , SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, SCHEMA_LOCATION))
                .append(format("       {0}=\"{1}\"/>"    , XML_SCHEMA_ATTRIBUTE, XSI_NS));

        String actual = periodXmlSerializer.createDocumentString(period);
        assertXMLEqual(expected.toString(), actual);
    }
}
