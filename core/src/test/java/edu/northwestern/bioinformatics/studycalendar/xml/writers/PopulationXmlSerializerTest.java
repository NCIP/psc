package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import static edu.northwestern.bioinformatics.studycalendar.xml.writers.PopulationXmlSerializer.NAME;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.PopulationXmlSerializer.ABBREVIATION;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import org.dom4j.Element;
import static org.easymock.EasyMock.expect;

public class PopulationXmlSerializerTest extends StudyCalendarXmlTestCase {
    private PopulationXmlSerializer serializer;
    private Element element;
    private Population population;
    private Study study;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);

        study = setId(4, createNamedInstance("Study A", Study.class));

        serializer = new PopulationXmlSerializer();
        serializer.setStudy(study);

        population = new Population();
        population.setAbbreviation("MP");
        population.setName("My Population");
        population.setStudy(study);

    }

    public void testCreateElement() throws Exception {
        Element actual = serializer.createElement(population);

        assertEquals("Wrong attribute size", 2, actual.attributeCount());
        assertEquals("Wrong abbreviation", "MP", actual.attributeValue(ABBREVIATION));
        assertEquals("Wrong name", "My Population", actual.attributeValue(NAME));
    }

    public void testReadElementWherePopulationIsNew() throws Exception {
        expect(element.attributeValue("abbreviation")).andReturn("AP");
        expect(element.attributeValue("name")).andReturn("A Population");
        replayMocks();

        Population actual = serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong name", "A Population", actual.getName());
        assertEquals("Wrong abbreviation", "AP", actual.getAbbreviation());
    }
}
