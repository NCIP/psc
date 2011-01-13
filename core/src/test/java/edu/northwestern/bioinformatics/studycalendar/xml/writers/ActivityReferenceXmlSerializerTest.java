package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createActivityType;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSource;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.ACTIVITY_CODE;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.ACTIVITY_SOURCE;

public class ActivityReferenceXmlSerializerTest extends StudyCalendarXmlTestCase {
    private static final String SOURCE_NAME = "Carousel";
    private Activity activity;
    private ActivityReferenceXmlSerializer serializer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        activity = Fixtures.createActivity(
            "Barney",
            "B",
            createSource(SOURCE_NAME),
            createActivityType("Fun"),
            "Barney Blue Horse"
        );

        serializer = new ActivityReferenceXmlSerializer();
    }


    public void testCreateElement() throws Exception {
        Element actual = serializer.createElement(activity);
        assertBasicActivityElement(activity, actual);
        assertEquals("Missing source reference", SOURCE_NAME,
                ACTIVITY_SOURCE.from(actual));
    }

    private static void assertBasicActivityElement(Activity expectedActivity, Element actualElement) {
        assertEquals("Wrong element name", XsdElement.ACTIVITY.xmlName(), actualElement.getName());
        assertEquals("Should have no children", 0, actualElement.elements().size());
        assertEquals("Wrong code", expectedActivity.getCode(),
                ACTIVITY_CODE.from(actualElement));
        assertEquals("Wrong source", expectedActivity.getSource().getName(),
                ACTIVITY_SOURCE.from(actualElement));
    }
}
