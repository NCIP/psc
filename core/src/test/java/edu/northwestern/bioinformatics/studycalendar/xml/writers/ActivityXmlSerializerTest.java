/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

/**
 * @author Rhett Sutphin
 */
public class ActivityXmlSerializerTest extends StudyCalendarXmlTestCase {
    private static final String SOURCE_NAME = "Unusual";

    private Source source;
    private Activity activity;
    private ActivityType activityType;
    private ActivityXmlSerializer standalone;
    private ActivityXmlSerializer embedded;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        source = Fixtures.createNamedInstance(SOURCE_NAME, Source.class);
        activity = createActivity();
        standalone = new ActivityXmlSerializer(false);
        embedded = new ActivityXmlSerializer(true);
        activityType = Fixtures.createActivityType("ActivityType1");
        activityType.setId(4);
    }


    public void testValidateElementForStandalone() throws Exception {
        Element actual = standalone.createElement(activity);
        assertFalse(standalone.validateElement(null, actual));

        Activity anotherActivity = createActivity();

        assertTrue(standalone.validateElement(activity, actual));
        anotherActivity.setSource(null);
        assertFalse(standalone.validateElement(anotherActivity, actual));


        anotherActivity = createActivity();
        assertTrue(standalone.validateElement(activity, actual));
        anotherActivity.getSource().setName("wrong code");
        assertFalse(standalone.validateElement(anotherActivity, actual));

    }

    public void testValidateElementForEmbeded() throws Exception {
        Element actual = embedded.createElement(activity);
        assertFalse(embedded.validateElement(null, actual));

        Activity anotherActivity = createActivity();
        assertTrue(embedded.validateElement(activity, actual));
        anotherActivity.setCode("wrong code");
        assertFalse(embedded.validateElement(anotherActivity, actual));

        anotherActivity = createActivity();
        assertTrue(embedded.validateElement(activity, actual));
        ActivityType activityType = Fixtures.createActivityType("DISEASE_MEASURE");
        anotherActivity.setType(activityType);
        assertFalse(embedded.validateElement(anotherActivity, actual));

        anotherActivity = createActivity();
        assertTrue(embedded.validateElement(activity, actual));
        anotherActivity.setName("wrong name");
        assertFalse(embedded.validateElement(anotherActivity, actual));

        anotherActivity = createActivity();
        assertTrue(embedded.validateElement(activity, actual));
        anotherActivity.setDescription("wrong desc");
        assertFalse(embedded.validateElement(anotherActivity, actual));


    }

    public void testCreateElementStandalone() throws Exception {
        Element actual = standalone.createElement(activity);
        assertBasicActivityElement(activity, actual);
        assertEquals("Missing source reference", SOURCE_NAME,
                ACTIVITY_SOURCE.from(actual));
    }

    public void testCreateElementEmbedded() throws Exception {
        Element actual = embedded.createElement(activity);
        assertEmbeddedActivityElement(activity, actual);
    }

    public static void assertEmbeddedActivityElement(Activity expectedActivity, Element actualElement) {
        assertBasicActivityElement(expectedActivity, actualElement);
        assertNull("Activity embedded in source should not have source attribute",
                ACTIVITY_SOURCE.from(actualElement));
    }

    private static void assertBasicActivityElement(Activity expectedActivity, Element actualElement) {
        assertEquals("Wrong element name", XsdElement.ACTIVITY.xmlName(), actualElement.getName());
        assertEquals("Should have no children", 0, actualElement.elements().size());
        assertEquals("Wrong code", expectedActivity.getCode(),
                ACTIVITY_CODE.from(actualElement));
        assertEquals("Wrong name", expectedActivity.getName(),
                ACTIVITY_NAME.from(actualElement));
        assertEquals("Wrong desc", expectedActivity.getDescription(),
                ACTIVITY_DESC.from(actualElement));
        assertEquals("Wrong type", expectedActivity.getType().getName() + "",
                ACTIVITY_TYPE.from(actualElement));
    }

    public void testReadEmbeddedElement() throws Exception {
        Element param = XsdElement.ACTIVITY.create();
        ActivityType at = new ActivityType("4");
        at.setId(4);
        ACTIVITY_NAME.addTo(param, "Aleph");
        ACTIVITY_CODE.addTo(param, "A");
        ACTIVITY_DESC.addTo(param, "Infinite");
        ACTIVITY_TYPE.addTo(param, "4");

        replayMocks();
        Activity read = embedded.readElement(param);
        verifyMocks();
        
        assertNotNull(read);
        assertNull(read.getId());
        assertNull(read.getGridId());
        assertEquals("Aleph", read.getName());
        assertEquals("A", read.getCode());
        assertEquals("Infinite", read.getDescription());
        assertNull(read.getSource());
    }

    public void testReadStandaloneElement() throws Exception {
        Element param = XsdElement.ACTIVITY.create();
        ActivityType at = new ActivityType("2");
        at.setId(2);
        ACTIVITY_NAME.addTo(param, "Prime");
        ACTIVITY_CODE.addTo(param, "P");
        ACTIVITY_DESC.addTo(param, "Single");
        ACTIVITY_TYPE.addTo(param, "2");
        ACTIVITY_SOURCE.addTo(param, "Ether");


        replayMocks();
        Activity read = standalone.readElement(param);
        verifyMocks();

        assertNotNull(read);
        assertNull(read.getId());
        assertNull(read.getGridId());
        assertEquals("Prime", read.getName());
        assertEquals("P", read.getCode());
        assertEquals("Single", read.getDescription());
        assertEquals(at, read.getType());

        Source readSource = read.getSource();
        assertNotNull(readSource);
        assertNull(readSource.getId());
        assertNull(readSource.getGridId());
        assertEquals("Ether", readSource.getNaturalKey());
    }

    private Activity createActivity() {
        activityType = Fixtures.createActivityType("PROCEDURE");
        activity = Fixtures.createActivity("Pogo", "PG", source,
                Fixtures.createActivityType("OTHER"), "15 minutes, at least");
        return activity;
    }
}
