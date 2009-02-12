package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityProperty;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.test.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.test.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.test.Fixtures.createActivity;
import static edu.northwestern.bioinformatics.studycalendar.test.Fixtures.createSingleActivityProperty;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import org.dom4j.Element;

/**
 * @author Jalpa Patel
 */
public class ActivityPropertyXmlSerializerTest   extends StudyCalendarXmlTestCase {
    private ActivityPropertyXmlSerializer activityPropertyXmlSerializer;
    private ActivityProperty activityProperty;
    private Activity activity;
    private final String namespace = "URI";

    @Override
    protected void setUp() throws Exception {
           super.setUp();
           activity = setId(20, createActivity("Bone Scan"));
           activityProperty =  Fixtures.createSingleActivityProperty(activity, namespace ,"id.template" ,"templateValue");
           activityPropertyXmlSerializer = new ActivityPropertyXmlSerializer();
       }
    
    public void testCreateElement() throws Exception {
           Element actual =   activityPropertyXmlSerializer.createElement(activityProperty);
           assertEquals("Wrong element name", XsdElement.ACTIVITY_PROPERTY.xmlName(), actual.getName());
           assertEquals("Should have no children", 0, actual.elements().size());
           assertEquals("Wrong property namespace", activityProperty.getNamespace(), ACTIVITY_PROPERTY_NAMESPACE.from(actual));
           assertEquals("Wrong property name", activityProperty.getName(), ACTIVITY_PROPERTY_NAME.from(actual));
           assertEquals("Wrong property value", activityProperty.getValue(), ACTIVITY_PROPERTY_VALUE.from(actual));

    }

    public void testReadElement() throws Exception {
           Element actual = XsdElement.ACTIVITY_PROPERTY.create();
           ACTIVITY_PROPERTY_NAMESPACE.addTo(actual,"URI");
           ACTIVITY_PROPERTY_NAME.addTo(actual,"id.template");
           ACTIVITY_PROPERTY_VALUE.addTo(actual,"templateValue");

           ActivityProperty read = activityPropertyXmlSerializer.readElement(actual);

           assertNotNull(read);
           assertNull(read.getId());
           assertEquals(null, read.getGridId());
           assertEquals("Wrong property namespace", "URI", read.getNamespace());
           assertEquals("Wrong property name", "id.template", read.getName());
           assertEquals("Wrong property value", "templateValue", read.getValue());
    }
}
