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
            "Harry",
            "H",
            createSource(SOURCE_NAME),
            createActivityType("Circular"),
            "Harry Horse"
        );

        serializer = new ActivityReferenceXmlSerializer();
    }


    public void testCreateElement() throws Exception {
        Element actual = serializer.createElement(activity);
        assertBasicActivityRefElement(activity, actual);
        assertEquals("Missing source reference", SOURCE_NAME,
                ACTIVITY_SOURCE.from(actual));
    }

    private static void assertBasicActivityRefElement(Activity expectedActivity, Element actualElement) {
        assertEquals("Wrong element name", XsdElement.ACTIVITY_REF.xmlName(), actualElement.getName());
        assertEquals("Should have no children", 0, actualElement.elements().size());
        assertEquals("Should have two attributes", 2, actualElement.attributeCount());
        assertEquals("Wrong code", expectedActivity.getCode(),
                ACTIVITY_CODE.from(actualElement));
        assertEquals("Wrong source", expectedActivity.getSource().getName(),
                ACTIVITY_SOURCE.from(actualElement));
    }

//    public void testReadElement() throws Exception {
//        Element param = XsdElement.ACTIVITY.create();
//        ACTIVITY_CODE.addTo(param, "P");
//        ACTIVITY_SOURCE.addTo(param, "Ether");
//        replayMocks();
//        Activity read = serializer.readElement(param);
//        verifyMocks();
//
//        assertNotNull(read);
//        assertNull(read.getId());
//        assertNull(read.getGridId());
//        assertEquals("Prime", read.getName());
//        assertEquals("P", read.getCode());
//        assertEquals("Single", read.getDescription());
//        assertEquals(at, read.getType());
//
//        Source readSource = read.getSource();
//        assertNotNull(readSource);
//        assertNull(readSource.getId());
//        assertNull(readSource.getGridId());
//        assertEquals("Ether", readSource.getNaturalKey());
//    }
}
