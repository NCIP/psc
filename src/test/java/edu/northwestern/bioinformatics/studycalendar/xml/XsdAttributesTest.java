package edu.northwestern.bioinformatics.studycalendar.xml;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import org.dom4j.Element;
import org.dom4j.Attribute;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultAttribute;

/**
 * @author Rhett Sutphin
 */
public class XsdAttributesTest extends StudyCalendarTestCase {
    public void testFromWhenNotPresent() throws Exception {
        Element elt = new DefaultElement("dc");
        assertNull(XsdAttributes.ACTIVITY_CODE.from(elt));
    }

    public void testFromWhenPresent() throws Exception {
        Element elt = new DefaultElement("activity");
        elt.add(new DefaultAttribute("code", "12"));
        assertEquals("12", XsdAttributes.ACTIVITY_CODE.from(elt));
    }
    
    public void testAddToElement() throws Exception {
        Element elt = new DefaultElement("activity");
        assertNull(elt.attribute("name"));
        XsdAttributes.ACTIVITY_NAME.addTo(elt, "Twelve");
        Attribute actual = elt.attribute("name");
        assertNotNull("Not added", actual);
        assertEquals("Twelve", actual.getValue());
    }
}
