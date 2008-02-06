package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.ActivityXmlSerializerTest.assertEmbeddedActivityElement;
import org.dom4j.Element;

import java.util.Arrays;
import java.util.List;

/**
 * @author Saurabh Agrawal
 */
public class ActivitySourcesXmlSerializerTest extends StudyCalendarXmlTestCase {
    private ActivitySourcesXmlSerializer serializer;
    private Activity actWalk, actRun, actSleep;
    private static final String SOURCE_NAME = "Snowflake";

    private Source source, otherSource;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        serializer = new ActivitySourcesXmlSerializer();
        source = createNamedInstance(SOURCE_NAME, Source.class);
        otherSource = createNamedInstance("Other", Source.class);

        actWalk = Fixtures.createActivity("Walk", "W", source, ActivityType.OTHER);
        actRun = Fixtures.createActivity("Run", "R", source, ActivityType.INTERVENTION);
        actSleep = Fixtures.createActivity("Sleep", "S", otherSource, ActivityType.OTHER);


    }

    public void testCreateElement() throws Exception {


        List<Source> sourceList = Arrays.asList(source, otherSource);
        Element actual = serializer.createElement(
                sourceList);
        assertEquals("Wrong root element name", XsdElement.ACTIVITY_SOURCES.xmlName(), actual.getName());
        assertEquals("Wrong number of children", 2, actual.elements().size());
        assertEquals("Children are not source elements",
                XsdElement.ACTIVITY_SOURCE.xmlName(), ((Element) actual.elements().get(0)).getName());
        assertEquals("Children are not source elements",
                XsdElement.ACTIVITY_SOURCE.xmlName(), ((Element) actual.elements().get(1)).getName());

        List activityElements = ((Element) actual.elements().get(0)).elements();

        assertEquals("Activities not included", 2,
                activityElements.size());
        assertEmbeddedActivityElement(actWalk, (Element) activityElements.get(0));
        assertEmbeddedActivityElement(actRun, (Element) activityElements.get(1));

        activityElements = ((Element) actual.elements().get(1)).elements();
        assertEquals("Activities not included", 1,
                activityElements.size());
        assertEmbeddedActivityElement(actSleep, (Element) activityElements.get(0));

    }

    public void testReadNotImplemented() throws Exception {
        try {
            serializer.readElement(null);
            fail("This serializer is write-only");
        } catch (UnsupportedOperationException e) {
            //expected 
        }
    }
}
