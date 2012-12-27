/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import static edu.northwestern.bioinformatics.studycalendar.xml.writers.PopulationXmlSerializer.NAME;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.PopulationXmlSerializer.ABBREVIATION;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import org.dom4j.Element;
import static org.easymock.EasyMock.expect;

public class PopulationXmlSerializerTest extends StudyCalendarXmlTestCase {
    private PopulationXmlSerializer serializer;
    private Element element;
    private Population population;

    protected void setUp() throws Exception {
        super.setUp();
        element = registerMockFor(Element.class);
        serializer = new PopulationXmlSerializer();
        population = new Population();
        population.setAbbreviation("MP");
        population.setName("My Population");
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
