package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.*;
import org.dom4j.Document;
import org.dom4j.Element;
import static org.easymock.EasyMock.expect;

import static java.text.MessageFormat.format;
import static java.util.Collections.singletonList;

public class StudyXmlSerializerTest extends StudyCalendarXmlTestCase {
    private StudyXmlSerializer serializer;
    private Study study;
    private Element element;
    private PlannedCalendar calendar;
    private StudyDao studyDao;


    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        studyDao = registerDaoMockFor(StudyDao.class);

        serializer = new StudyXmlSerializer();
        serializer.setStudyDao(studyDao);

        calendar = setGridId("grid1", new PlannedCalendar());

        study = createNamedInstance("Study A", Study.class);
        study.setPlannedCalendar(calendar);
    }

    public void testCreateElement() {
        Element actual = serializer.createElement(study);

        assertEquals("Wrong attribute size", 1, actual.attributeCount());
        assertEquals("Wrong assigned identifier", "Study A", actual.attribute("assigned-identifier").getValue());
        assertEquals("Should have planned calendar child", 1, actual.nodeCount());
    }

    public void testReadElement() {
        expect(element.attributeValue("assigned-identifier")).andReturn("Study A");
        expect(studyDao.getByAssignedIdentifier("Study A")).andReturn(null);
        expect(element.elements("planned-calendar")).andReturn(singletonList(element));
        expect(element.attributeValue("id")).andReturn("grid1");
        replayMocks();

        Study actual = serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong assigned identifier", "Study A", actual.getAssignedIdentifier());
        assertEquals("Wrong planned calendar id", "grid1", actual.getPlannedCalendar().getGridId());
    }

    public void testReadElementExists() {
        expect(element.attributeValue("assigned-identifier")).andReturn("Study A");
        expect(studyDao.getByAssignedIdentifier("Study A")).andReturn(study);
        replayMocks();

        Study actual = serializer.readElement(element);
        verifyMocks();

        assertSame("Wrong Study", study, actual);
    }

    public void testCreateDocument() throws Exception {
        Document actual = serializer.createDocument(study);

        assertEquals("Wrong assigned identifier", "Study A", actual.getRootElement().attribute("assigned-identifier").getValue());
        assertEquals("Wrong planned calendar grid id", "grid1", actual.getRootElement().element("planned-calendar").attribute("id").getValue());
    }

    public void testCreateDocumentString() throws Exception {
        StringBuffer expected = new StringBuffer();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append(format("<study assigned-identifier=\"{0}\"", study.getAssignedIdentifier()))
                .append(format("       {0}=\"{1}\""     , SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS))
                .append(format("       {0}=\"{1} {2}\""     , SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, SCHEMA_LOCATION))
                .append(format("       {0}=\"{1}\">"    , XML_SCHEMA_ATTRIBUTE, XSI_NS))
                .append(format(  "<planned-calendar id=\"{0}\"/>", calendar.getGridId()))
                .append(       "</study>");

        String actual = serializer.createDocumentString(study);
        assertXMLEqual(expected.toString(), actual);
    }
}
