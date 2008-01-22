package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.*;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import static org.easymock.EasyMock.expect;

import static java.text.MessageFormat.format;
import static java.util.Collections.singletonList;

public class StudyXmlSerializerTest extends StudyCalendarXmlTestCase {
    private StudyXmlSerializer serializer;
    private Study study;
    private Element element;
    private PlannedCalendar calendar;
    private StudyDao studyDao;
    private PopulationXmlSerializer populationSerializer;
    private Population population;
    private Element ePopulation;


    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        studyDao = registerDaoMockFor(StudyDao.class);
        populationSerializer = registerMockFor(PopulationXmlSerializer.class);

        serializer = new MockableStudyXmlSerializer();
        serializer.setStudyDao(studyDao);

        calendar = setGridId("grid1", new PlannedCalendar());

        population = createPopulation("MP", "My Population");

        study = createNamedInstance("Study A", Study.class);
        study.setPlannedCalendar(calendar);
        study.addPopulation(population);

        QName qPopulation = DocumentHelper.createQName("population", AbstractStudyCalendarXmlSerializer.DEFAULT_NAMESPACE);
        ePopulation = DocumentHelper.createElement(qPopulation);
        ePopulation.addAttribute("abbreviation", population.getAbbreviation());
        ePopulation.addAttribute("name", population.getName());
    }

    public void testCreateElement() {
        expect(populationSerializer.createElement(population)).andReturn(ePopulation);
        replayMocks();

        Element actual = serializer.createElement(study);
        verifyMocks();

        assertEquals("Wrong attribute size", 1, actual.attributeCount());
        assertEquals("Wrong assigned identifier", "Study A", actual.attribute("assigned-identifier").getValue());
        assertEquals("Should have planned calendar child and population child nodes", 2, actual.nodeCount());
        assertNotNull("Planned calendar should exist", actual.element("planned-calendar"));
        assertNotNull("Population should exist", actual.element("population"));

    }

    public void testReadElementWhereElementIsNew() {
        expect(element.attributeValue("assigned-identifier")).andReturn("Study A");
        expect(studyDao.getByAssignedIdentifier("Study A")).andReturn(null);
        expect(element.elements("planned-calendar")).andReturn(singletonList(element));
        expect(element.attributeValue("id")).andReturn("grid1");
        expect(element.elements("population")).andReturn(singletonList(element));
        expect(populationSerializer.readElement(element)).andReturn(population);
        replayMocks();

        Study actual = serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong assigned identifier", "Study A", actual.getAssignedIdentifier());
        assertEquals("Wrong planned calendar id", "grid1", actual.getPlannedCalendar().getGridId());
        assertSame("Populations should be the same", population, actual.getPopulations().iterator().next());
    }

    public void testReadElementWhereElementExists() {
        expect(element.attributeValue("assigned-identifier")).andReturn("Study A");
        expect(studyDao.getByAssignedIdentifier("Study A")).andReturn(study);
        replayMocks();

        Study actual = serializer.readElement(element);
        verifyMocks();

        assertSame("Wrong Study", study, actual);
    }

    public void testCreateDocument() throws Exception {
        expect(populationSerializer.createElement(population)).andReturn(ePopulation);
        replayMocks();

        Document actual = serializer.createDocument(study);
        verifyMocks();

        assertEquals("Wrong assigned identifier", "Study A", actual.getRootElement().attribute("assigned-identifier").getValue());
        assertEquals("Wrong planned calendar grid id", "grid1", actual.getRootElement().element("planned-calendar").attributeValue("id"));
        assertNotNull("Population element should exist", actual.getRootElement().element("population"));
    }

    public void testCreateDocumentString() throws Exception {
        StringBuffer expected = new StringBuffer();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append(format("<study assigned-identifier=\"{0}\"", study.getAssignedIdentifier()))
                .append(format("       {0}=\"{1}\""     , SCHEMA_NAMESPACE_ATTRIBUTE, PSC_NS))
                .append(format("       {0}=\"{1} {2}\""     , SCHEMA_LOCATION_ATTRIBUTE, PSC_NS, SCHEMA_LOCATION))
                .append(format("       {0}=\"{1}\">"    , XML_SCHEMA_ATTRIBUTE, XSI_NS))
                .append(format(  "<planned-calendar id=\"{0}\"/>", calendar.getGridId()))
                .append(format(  "<population abbreviation=\"{0}\" name=\"{1}\"/>", population.getAbbreviation(), population.getName()))
                .append(       "</study>");

        expect(populationSerializer.createElement(population)).andReturn(ePopulation);
        replayMocks();

        String actual = serializer.createDocumentString(study);
        verifyMocks();

        assertXMLEqual(expected.toString(), actual);
    }

    private class MockableStudyXmlSerializer extends StudyXmlSerializer {

        public PopulationXmlSerializer getPopulationSerializer(Study study) {
            return populationSerializer;
        }
    }
}
