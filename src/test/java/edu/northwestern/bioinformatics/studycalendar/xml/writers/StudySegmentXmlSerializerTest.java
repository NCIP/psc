package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.*;
import org.dom4j.Document;
import org.dom4j.Element;
import static org.easymock.EasyMock.expect;

import static java.text.MessageFormat.format;
import static java.util.Collections.emptyList;

public class StudySegmentXmlSerializerTest extends StudyCalendarXmlTestCase {
    private StudySegmentSerializer studySegmentSerializer;
    private StudySegmentDao studySegmentDao;
    private Element element;
    private StudySegment segment;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        studySegmentDao = registerDaoMockFor(StudySegmentDao.class);

        studySegmentSerializer = new StudySegmentSerializer();
        studySegmentSerializer.setStudySegmentDao(studySegmentDao);

        segment = setGridId("grid0", createNamedInstance("Segment A", StudySegment.class));
    }

    public void testCreateElementEpoch() {
        Element actual = studySegmentSerializer.createElement(segment);

        assertEquals("Wrong attribute size", 2, actual.attributeCount());
        assertEquals("Wrong grid id", "grid0", actual.attribute("id").getValue());
        assertEquals("Wrong segment name", "Segment A", actual.attribute("name").getValue());
    }

    public void testReadElementEpoch() {
        expect(element.getName()).andReturn("study-segment");
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(studySegmentDao.getByGridId("grid0")).andReturn(null);
        expect(element.attributeValue("name")).andReturn("Segment A");
        expect(element.elements()).andReturn(emptyList());
        replayMocks();

        StudySegment actual = (StudySegment) studySegmentSerializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong grid id", "grid0", actual.getGridId());
        assertEquals("Wrong segment name", "Segment A", actual.getName());
    }

    public void testReadElementExistsEpoch() {
        expect(element.getName()).andReturn("study-segment");
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(studySegmentDao.getByGridId("grid0")).andReturn(segment);
        replayMocks();

        StudySegment actual = (StudySegment) studySegmentSerializer.readElement(element);
        verifyMocks();

        assertSame("Wrong Segment", segment, actual);
    }

    public void testCreateDocumentEpoch() throws Exception {
        Document actual = studySegmentSerializer.createDocument(segment);

        assertEquals("Element should be an segment", "study-segment", actual.getRootElement().getName());
        assertEquals("Wrong segment grid id", "grid0", actual.getRootElement().attributeValue("id"));
        assertEquals("Wrong segment name", "Segment A", actual.getRootElement().attributeValue("name"));
    }

    public void testCreateDocumentStringEpoch() throws Exception {
        StringBuffer expected = new StringBuffer();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append(format("<study-segment id=\"{0}\" name=\"{1}\"", segment.getGridId(), segment.getName()))
                .append(format("       {0}=\"{1}\""     , SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS))
                .append(format("       {0}=\"{1} {2}\""     , SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, SCHEMA_LOCATION))
                .append(format("       {0}=\"{1}\"/>"    , XML_SCHEMA_ATTRIBUTE, XSI_NS));

        String actual = studySegmentSerializer.createDocumentString(segment);
        assertXMLEqual(expected.toString(), actual);
    }
}
