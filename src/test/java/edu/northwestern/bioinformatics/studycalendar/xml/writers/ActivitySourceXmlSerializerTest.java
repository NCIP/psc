package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElements;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.ActivityXmlSerializerTest.assertEmbeddedActivityElement;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class ActivitySourceXmlSerializerTest extends StudyCalendarXmlTestCase {
    private static final String SOURCE_NAME = "Snowflake";
    private ActivitySourceXmlSerializer serializer;
    private Source source;
    private Activity actWalk, actRun, actSleep;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        serializer = new ActivitySourceXmlSerializer();
        source = createNamedInstance(SOURCE_NAME, Source.class);
        actWalk = createActivity("Walk", "W", source, ActivityType.OTHER);
        actRun = createActivity("Run", "R", source, ActivityType.INTERVENTION);
        actSleep = createActivity("Sleep", "S", source, ActivityType.OTHER);
    }

    public void testCreateSingleElement() throws Exception {
        Element actual = serializer.createElement(source);
        assertEquals("Wrong element name", XsdElements.ACTIVITY_SOURCE.xmlName(), actual.getName());
        assertEquals("Wrong name attr", SOURCE_NAME, actual.attributeValue("name"));
        assertEquals("Wrong number of children", 3, actual.elements().size());
        assertEmbeddedActivityElement(actWalk, (Element) actual.elements().get(0));
        assertEmbeddedActivityElement(actRun, (Element) actual.elements().get(1));
        assertEmbeddedActivityElement(actSleep, (Element) actual.elements().get(2));
    }

    public void testCreateCollectionDoc() throws Exception {
        Document actual = serializer.createDocument(
            Arrays.asList(source, createNamedInstance("Other", Source.class)));
        Element root = actual.getRootElement();
        assertEquals("Wrong root element name", XsdElements.ACTIVITY_SOURCES.xmlName(), root.getName());
        assertEquals("Wrong number of children", 2, root.elements().size());
        assertEquals("Children are not source elements",
            XsdElements.ACTIVITY_SOURCE.xmlName(), ((Element) root.elements().get(0)).getName());
        assertEquals("Children are not source elements",
            XsdElements.ACTIVITY_SOURCE.xmlName(), ((Element) root.elements().get(1)).getName());
        assertEquals("Activities not included", 3,
            ((Element) root.elements().get(0)).elements().size());
    }

    public void testReadSingleSource() throws Exception {
        Source read = parseDocumentString(serializer, String.format(
            "<source xmlns='%s' name='test-o'>\n" +
            "  <activity name='one' code='1' type-id='1'/>\n" +
            "  <activity name='two' code='2' type-id='2' description='optional'/>\n" +
            "</source>", AbstractStudyCalendarXmlSerializer.PSC_NS
        ));
        assertNotNull(read);
        assertEquals("Wrong name", "test-o", read.getName());
        assertEquals("Wrong number of activities", 2, read.getActivities().size());

        Activity activity1 = read.getActivities().get(0);
        assertSame("Backref not present in first activity", read, activity1.getSource());
        assertEquals("Wrong name for first activity", "one", activity1.getName());
        assertEquals("Wrong code for first activity", "1", activity1.getCode());
        assertEquals("Wrong code for first activity", ActivityType.getById(1), activity1.getType());
        assertNull("Wrong desc for first activity", activity1.getDescription());

        Activity activity2 = read.getActivities().get(1);
        assertEquals("Backref not present in second activity", read, activity2.getSource());
        assertEquals("Wrong name for second activity", "two", activity2.getName());
        assertEquals("Wrong code for second activity", "2", activity2.getCode());
        assertEquals("Wrong code for second activity", ActivityType.getById(2), activity2.getType());
        assertEquals("Wrong desc for second activity", "optional", activity2.getDescription());
    }
}
