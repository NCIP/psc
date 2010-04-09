package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import org.dom4j.Element;

import java.util.Arrays;

/**
 * @author Jalpa Patel
 */
public class SourcesXmlSerializerTest  extends StudyCalendarXmlTestCase {
    private SourcesXmlSerializer serializer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        serializer = new SourcesXmlSerializer();
    }

    public void testCreateSingleElement() throws Exception {
        Source source = Fixtures.createSource("TestSource");

        Element actual = serializer.createElement(source);
        assertEquals("source", actual.getName());
        assertEquals("Wrong name", "TestSource",
                XsdAttribute.SOURCE_NAME.from(actual));
    }

    public void testSerializeCollection() throws Exception {
        Source source1 = Fixtures.createSource("TestSource1");
        Source source2 = Fixtures.createSource("TestSource2");

        Element actual = serializer.createDocument(Arrays.asList(source1, source2)).getRootElement();
        assertEquals("sources", actual.getName());
        assertEquals(2, actual.elements().size());
        assertEmbeddedSourceElement("TestSource1", (Element) actual.elements().get(0));
        assertEmbeddedSourceElement("TestSource2", (Element) actual.elements().get(1));
    }

    private void assertEmbeddedSourceElement(String expectedName, Element actual) {
        assertEquals("Wrong element", "source", actual.getName());
        assertEquals("Wrong name", expectedName,
                XsdAttribute.SOURCE_NAME.from(actual));
        assertEquals("Should have no children", 0, actual.elements().size());
    }

    public void testElementWhenManualFlagIsTrue() throws Exception {
        Source source = Fixtures.createSource("TestSource");
        source.setManualFlag(true);
        Element actual = serializer.createElement(source);
        assertEquals("Wrong manual flag value", "true" ,
                XsdAttribute.SOURCE_MANUAL_FLAG.from(actual));
    }
}

