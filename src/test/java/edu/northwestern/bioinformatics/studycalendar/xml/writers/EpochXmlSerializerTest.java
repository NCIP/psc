package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.*;
import org.dom4j.Document;
import org.dom4j.Element;
import static org.easymock.EasyMock.expect;

import static java.text.MessageFormat.format;
import static java.util.Collections.emptyList;

public class EpochXmlSerializerTest extends StudyCalendarXmlTestCase {
    private EpochXmlSerializer epochSerializer;
    private EpochDao epochDao;
    private Element element;
    private Epoch epoch;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        epochDao = registerDaoMockFor(EpochDao.class);

        epochSerializer = new EpochXmlSerializer();
        epochSerializer.setEpochDao(epochDao);

        epoch = setGridId("grid0", createNamedInstance("Epoch A", Epoch.class));
    }

    public void testCreateElementEpoch() {
        Element actual = epochSerializer.createElement(epoch);

        assertEquals("Wrong attribute size", 2, actual.attributeCount());
        assertEquals("Wrong grid id", "grid0", actual.attribute("id").getValue());
        assertEquals("Wrong epoch name", "Epoch A", actual.attribute("name").getValue());
    }

    public void testReadElementEpoch() {
        expect(element.getName()).andReturn("epoch");
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(epochDao.getByGridId("grid0")).andReturn(null);
        expect(element.attributeValue("name")).andReturn("Epoch A");
        expect(element.elements()).andReturn(emptyList());
        replayMocks();

        Epoch actual = (Epoch) epochSerializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong grid id", "grid0", actual.getGridId());
        assertEquals("Wrong epoch name", "Epoch A", actual.getName());
    }

    public void testReadElementExistsEpoch() {
        expect(element.getName()).andReturn("epoch");
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(epochDao.getByGridId("grid0")).andReturn(epoch);
        replayMocks();

        Epoch actual = (Epoch) epochSerializer.readElement(element);
        verifyMocks();

        assertSame("Wrong Epoch", epoch, actual);
    }

    public void testCreateDocumentEpoch() throws Exception {
        Document actual = epochSerializer.createDocument(epoch);

        assertEquals("Element should be an epoch", "epoch", actual.getRootElement().getName());
        assertEquals("Wrong epoch grid id", "grid0", actual.getRootElement().attributeValue("id"));
        assertEquals("Wrong epoch name", "Epoch A", actual.getRootElement().attributeValue("name"));
    }

    public void testCreateDocumentStringEpoch() throws Exception {
        StringBuffer expected = new StringBuffer();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append(format("<epoch id=\"{0}\" name=\"{1}\"", epoch.getGridId(), epoch.getName()))
                .append(format("       {0}=\"{1}\""     , SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS))
                .append(format("       {0}=\"{1} {2}\""     , SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, SCHEMA_LOCATION))
                .append(format("       {0}=\"{1}\"/>"    , XML_SCHEMA_ATTRIBUTE, XSI_NS));

        String actual = epochSerializer.createDocumentString(epoch);
        assertXMLEqual(expected.toString(), actual);
    }
}
