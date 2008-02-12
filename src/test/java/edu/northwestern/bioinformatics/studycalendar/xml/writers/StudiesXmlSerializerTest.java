package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import org.dom4j.Element;

import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class StudiesXmlSerializerTest extends StudyCalendarXmlTestCase {
    private StudiesXmlSerializer serializer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        serializer = new StudiesXmlSerializer();
    }

    public void testSerializeCollection() throws Exception {
        Study sA = Fixtures.createNamedInstance("A", Study.class);
        Study sB = Fixtures.createNamedInstance("B", Study.class);

        Element actual = serializer.createDocument(Arrays.asList(sA, sB)).getRootElement();
        assertEquals("studies", actual.getName());
        assertEquals(2, actual.elements().size());
        assertEmbeddedStudyElement("A", (Element) actual.elements().get(0));
        assertEmbeddedStudyElement("B", (Element) actual.elements().get(1));
    }

    private void assertEmbeddedStudyElement(String expectedAssignedIdentifier, Element actual) {
        assertEquals("Wrong element", "study", actual.getName());
        assertEquals("Wrong identifier", expectedAssignedIdentifier,
            actual.attributeValue(StudyXmlSerializer.ASSIGNED_IDENTIFIER));
        assertEquals("Should have no children", 0, actual.elements().size());
    }
}
