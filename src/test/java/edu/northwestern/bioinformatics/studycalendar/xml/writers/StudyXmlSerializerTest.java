package edu.northwestern.bioinformatics.studycalendar.xml.writers;


import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.STUDY;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import static org.dom4j.DocumentHelper.createElement;
import static org.dom4j.DocumentHelper.createQName;
import org.dom4j.Element;
import org.dom4j.QName;
import static org.easymock.EasyMock.expect;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class StudyXmlSerializerTest extends StudyCalendarXmlTestCase {
    private StudyXmlSerializer serializer;
    private Study study;
    private Element element;
    private PlannedCalendar calendar;
    private StudyDao studyDao;
    private PopulationXmlSerializer populationSerializer;
    private Population population;
    private Element ePopulation;
    private PlannedCalendarXmlSerializer plannedCalendarSerializer;
    private AmendmentXmlSerializer amendmentSerializer;
    private AmendmentXmlSerializer developmentAmendmentSerializer;
    private Amendment amendment;
    private Amendment developmentAmendment;
    private Element eAmendment;
    private Element eDevelopmentAmendment;
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    private Element eCalendar;


    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        studyDao = registerDaoMockFor(StudyDao.class);
        amendmentSerializer = registerMockFor(AmendmentXmlSerializer.class);
        populationSerializer = registerMockFor(PopulationXmlSerializer.class);
        plannedCalendarSerializer = registerMockFor(PlannedCalendarXmlSerializer.class);
        developmentAmendmentSerializer=registerMockFor(AmendmentXmlSerializer.class);

        serializer = new StudyXmlSerializer() {
            protected PlannedCalendarXmlSerializer getPlannedCalendarXmlSerializer(Study study) {return plannedCalendarSerializer;}
            protected PopulationXmlSerializer getPopulationXmlSerializer(Study study) {return populationSerializer;}
            protected AmendmentXmlSerializer getAmendmentSerializer(Study study) {return amendmentSerializer;}
            protected AmendmentXmlSerializer getDevelopmentAmendmentSerializer(Study study) {return developmentAmendmentSerializer; }
        };
        serializer.setStudyDao(studyDao);

        calendar = setGridId("grid1", new PlannedCalendar());
        population = createPopulation("MP", "My Population");
        amendment =  createAmendment("Amendment A", createDate(2008, Calendar.JANUARY, 2), true);
        developmentAmendment=createAmendment("Amendment B",createDate(2008, Calendar.FEBRUARY,13),true);

        study = createStudy();

        eCalendar = createCalendarElement(calendar);
        ePopulation = createPopulationElement(population);
        eAmendment = createAmendmentElement(amendment);
        eDevelopmentAmendment = createDevelopmentAmendmentElement(developmentAmendment);
    }

    public void testReadElementWhereElementIsNew() {
        expectResolveStudy("Study A", null);
        expectDeserializePopulation();
        expectDeserializePlannedCalendar();
        expectDeserializeAmendments();
        expectDesearializeDevelopmentAmendment();
        replayMocks();

        Study actual = serializer.readElement(createStudyElement());
        verifyMocks();

        assertEquals("Wrong assigned identifier", "Study A", actual.getAssignedIdentifier());
        assertSame("PlannedCalendar should be the same", calendar, actual.getPlannedCalendar());
        assertSame("Populations should be the same", population, actual.getPopulations().iterator().next());
    }

    public void testReadElementWithInvalidElementName() {
        expect(element.getName()).andReturn("study-snapshot").times(2);
        try {
            replayMocks();
            serializer.readElement(element);
            fail("Exception should be thrown");
            verifyMocks();
        } catch (StudyCalendarValidationException e) {
            assertEquals("Element type is other than <study>", e.getMessage());
        }
    }

    public void testReadElementWithNoDevelopmentOrReleasedAmendments() {
        Element eStudy = createStudyElement();
        eStudy.remove(eAmendment);
        eStudy.remove(eDevelopmentAmendment);

        try {
            replayMocks();
            serializer.readElement(eStudy);
            fail("Exception should be thrown");
            verifyMocks();
        } catch (StudyCalendarValidationException e) {
            assertEquals("<study> must have at minimum an <amendment> or <development-amendment> child", e.getMessage());
        }
    }

    public void testReadElementWhereElementExists() {
        expectResolveStudy("Study A", study);
        expectDeserializeAmendments();
        expectDesearializeDevelopmentAmendment();
        replayMocks();

        Study actual = serializer.readElement(createStudyElement());
        verifyMocks();

        assertSame("Wrong Study", study, actual);
    }

    public void testCreateElement() {
        expectChildrenSerializers();
        replayMocks();

        Element actual = serializer.createElement(study);
        verifyMocks();

        assertEquals("Wrong attribute size", 1, actual.attributeCount());
        assertEquals("Wrong assigned identifier", "Study A", actual.attribute("assigned-identifier").getValue());
        assertEquals("Should have planned calendar child and population child nodes and development-amendment", 4, actual.nodeCount());
        assertNotNull("Planned calendar should exist", actual.element("planned-calendar"));
        assertNotNull("Population should exist", actual.element("population"));
        assertNotNull("Amendment should exist", actual.element("amendment"));
        assertNotNull("Development Amendment should exist", actual.element("development-amendment"));

    }

    ////// Expect helper methods
    private void expectChildrenSerializers() {
        expect(populationSerializer.createElement(population)).andReturn(ePopulation);
        expect(plannedCalendarSerializer.createElement(calendar)).andReturn(eCalendar);
        expect(amendmentSerializer.createElement(amendment)).andReturn(eAmendment);
        expect(developmentAmendmentSerializer.createElement(developmentAmendment)).andReturn(eDevelopmentAmendment);

    }

    private void expectResolveStudy(String name, Study resolved) {
        expect(studyDao.getByAssignedIdentifier(name)).andReturn(resolved);
    }

    private void expectDeserializePlannedCalendar() {
        expect(plannedCalendarSerializer.readElement(eCalendar)).andReturn(calendar);
    }

    private void expectDeserializePopulation() {
        expect(populationSerializer.readElement(ePopulation)).andReturn(population);
    }

    private void expectDesearializeDevelopmentAmendment() {
        expect(developmentAmendmentSerializer.readElement(eDevelopmentAmendment)).andReturn(developmentAmendment);
    }

    private void expectDeserializeAmendments() {
        expect(amendmentSerializer.readElement(eAmendment)).andReturn(amendment);
    }

    ////// Create element helpers
    private Element createStudyElement() {
        QName qStudy = createQName(STUDY.xmlName(), AbstractStudyCalendarXmlSerializer.DEFAULT_NAMESPACE);
        Element eStudy = createElement(qStudy);
        eStudy.addAttribute("assigned-identifier", study.getAssignedIdentifier());
        eStudy.add(eDevelopmentAmendment);
        eStudy.add(eAmendment);
        eStudy.add(ePopulation);
        eStudy.add(eCalendar);
        return eStudy;
    }

    private Element createDevelopmentAmendmentElement(Amendment developmentAmendment) {
        QName qDevelopmentAmendment = createQName(XsdElement.DEVELOPMENT_AMENDMENT.xmlName(), AbstractStudyCalendarXmlSerializer.DEFAULT_NAMESPACE);
        Element elt = createElement(qDevelopmentAmendment);
        elt.addAttribute("name", developmentAmendment.getName());
        elt.addAttribute("date", formatter.format(developmentAmendment.getDate()));
        elt.addAttribute("mandatory", Boolean.toString(developmentAmendment.isMandatory()));
        return elt;
    }

    private Element createAmendmentElement(Amendment amendment) {
        QName qAmendment = createQName("amendment", AbstractStudyCalendarXmlSerializer.DEFAULT_NAMESPACE);
        Element elt = createElement(qAmendment);
        elt.addAttribute("name", amendment.getName());
        elt.addAttribute("date", formatter.format(amendment.getDate()) );
        elt.addAttribute("mandatory", Boolean.toString(amendment.isMandatory()));
        return elt;
    }

    private Element createPopulationElement(Population population) {
        QName qPopulation = createQName("population", AbstractStudyCalendarXmlSerializer.DEFAULT_NAMESPACE);
        Element elt = createElement(qPopulation);
        elt.addAttribute("abbreviation", population.getAbbreviation());
        elt.addAttribute("name", population.getName());
        return elt;
    }

    private Element createCalendarElement(PlannedCalendar calendar) {
        QName qCalendar = createQName("planned-calendar", AbstractStudyCalendarXmlSerializer.DEFAULT_NAMESPACE);
        Element elt = createElement(qCalendar);
        elt.addAttribute("id", calendar.getGridId());
        return elt;
    }

    ////// Create PSC object helpers
    private Study createStudy() {
        Study study = createNamedInstance("Study A", Study.class);
        study.setPlannedCalendar(calendar);
        study.addPopulation(population);
        study.setAmendment(amendment);
        study.setDevelopmentAmendment(developmentAmendment);
        return study;
    }
}
