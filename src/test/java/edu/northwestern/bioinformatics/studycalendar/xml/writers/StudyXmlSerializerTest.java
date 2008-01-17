package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.nwu.bioinformatics.commons.StringUtils;
import org.custommonkey.xmlunit.XMLAssert;
import org.dom4j.Document;
import org.dom4j.Element;
import static org.easymock.EasyMock.expect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;

public class StudyXmlSerializerTest extends StudyCalendarTestCase {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private StudyXmlSerializer serializer;
    private Study study;
    private Element element;


    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        serializer = new StudyXmlSerializer();

        study = setGridId("grid0", createNamedInstance("Study A", Study.class));
    }

    public void testCreateElement() {
        Element actual = serializer.createElement(study);

        assertEquals("Wrong attribute size", 2, actual.attributeCount());
        assertEquals("Wrong assigned identifier", "Study A", actual.attribute("assigned-identifier").getValue());

        //TODO: do we need grid id? or are we using natural key?
        assertEquals("Wrong grid id", "grid0", actual.attribute("id").getValue());

    }

    public void testReadElement() {
        expect(element.attributeValue("assigned-identifier")).andReturn("Study A");
        expect(element.attributeValue("id")).andReturn("grid0");
        replayMocks();

        Study actual = serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong assigned identifier", "Study A", actual.getAssignedIdentifier());

        //TODO: do we need grid id? or are we using natural key?
        assertEquals("Wrong assigned identifier", "grid0", actual.getGridId());
    }

    public void testCreateDocument() throws Exception {
        Document actual = serializer.createDocument(study);

        assertEquals("Wrong assigned identifier", "Study A", actual.getRootElement().attribute("assigned-identifier").getValue());

        //TODO: do we need grid id? or are we using natural key?
        assertEquals("Wrong assigned identifier", "grid0", actual.getRootElement().attribute("id").getValue());
    }

    public void testCreateDocumentString() throws Exception {
        StringBuffer expected = new StringBuffer();
            expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
//               .append(format("<study assigned-identifier=\"{0}\" id=\"{1}\" \n", study.getAssignedIdentifier(), study.getGridId()))
//               .append(format("       {0}=\"{1}\" \n"     , SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS))
//               .append(format("       {0}=\"{1} {2}\" \n"     , SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, SCHEMA_LOCATION))
//               .append(format("       {0}=\"{1}\"/>\n"    , XML_SCHEMA_ATTRIBUTE, XSI_NS))
 ;

        String actual = serializer.createDocumentString(study);
        log.debug("Expected:\n{}", actual);
        assertXMLEqual(expected.toString(), actual);
    }

   private void assertXMLEqual(String expected, String actual) throws SAXException, IOException {
        // XMLUnit's whitespace stripper stopped working at r1976 (of PSC) or so
        // This is a quick-and-dirty (and not necessarily correct) alternative
        String expectedNormalized = StringUtils.normalizeWhitespace(expected);
        String actualNormalized = StringUtils.normalizeWhitespace(actual);
        XMLAssert.assertXMLEqual(expectedNormalized, actualNormalized);
    }

}
