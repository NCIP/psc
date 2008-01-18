package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.SCHEMA_NAMESPACE_ATTRIBUTE;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.PSC_NS;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.SCHEMA_LOCATION_ATTRIBUTE;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.SCHEMA_LOCATION;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.XML_SCHEMA_ATTRIBUTE;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.XSI_NS;
import org.dom4j.Element;
import org.dom4j.Document;
import static org.easymock.EasyMock.expect;

import static java.text.MessageFormat.format;

public class PlanTreeNodeXmlSerializerTest extends StudyCalendarXmlTestCase {

    private PlanTreeNodeXmlSerializer serializer;
    private Element element;
    private Epoch epoch;
    private EpochDao epochDao;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        epochDao = registerDaoMockFor(EpochDao.class);

        serializer = new PlanTreeNodeXmlSerializer();
        serializer.setEpochDao(epochDao);

        epoch = setGridId("grid0", createNamedInstance("Epoch A", Epoch.class));

    }

    public void testCreateElement() {
        Element actual = serializer.createElement(epoch);

        assertEquals("Wrong attribute size", 2, actual.attributeCount());
        assertEquals("Wrong grid id", "grid0", actual.attribute("id").getValue());
        assertEquals("Wrong epoch name", "Epoch A", actual.attribute("name").getValue());
    }

    public void testReadElement() {
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(element.getName()).andReturn("epoch").times(2);
        expect(epochDao.getByGridId("grid0")).andReturn(null);
        expect(element.attributeValue("name")).andReturn("Epoch A");
        replayMocks();

        Epoch actual = (Epoch) serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong grid id", "grid0", actual.getGridId());
        assertEquals("Wrong epoch name", "Epoch A", actual.getName());
    }

    public void testReadElementExists() {
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(element.getName()).andReturn("epoch").times(2);
        expect(epochDao.getByGridId("grid0")).andReturn(epoch);
        replayMocks();

        Epoch actual = (Epoch) serializer.readElement(element);
        verifyMocks();

        assertSame("Wrong Epoch", epoch, actual);
    }

    public void testCreateDocument() throws Exception {
        Document actual = serializer.createDocument(epoch);

        assertEquals("Wrong epoch grid id", "grid0", actual.getRootElement().attributeValue("id"));
        assertEquals("Wrong epoch name", "Epoch A", actual.getRootElement().attributeValue("name"));
    }

    public void testCreateDocumentString() throws Exception {
        StringBuffer expected = new StringBuffer();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append(format("<epoch id=\"{0}\" name=\"{1}\"", epoch.getGridId(), epoch.getName()))
                .append(format("       {0}=\"{1}\""     , SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS))
                .append(format("       {0}=\"{1} {2}\""     , SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, SCHEMA_LOCATION))
                .append(format("       {0}=\"{1}\"/>"    , XML_SCHEMA_ATTRIBUTE, XSI_NS));

        String actual = serializer.createDocumentString(epoch);
        assertXMLEqual(expected.toString(), actual);
    }


}
