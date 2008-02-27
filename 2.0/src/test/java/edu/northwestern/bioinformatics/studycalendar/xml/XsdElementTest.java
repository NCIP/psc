package edu.northwestern.bioinformatics.studycalendar.xml;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import org.dom4j.Element;

/**
 * @author Rhett Sutphin
 */
public class XsdElementTest extends StudyCalendarTestCase {
    public void testXmlNameDefaultsToLCEnumName() throws Exception {
        assertEquals("activity", XsdElement.ACTIVITY.xmlName());
    }
    
    public void testExplicitXmlNameOverrides() throws Exception {
        assertEquals("source", XsdElement.ACTIVITY_SOURCE.xmlName());
    }
    
    public void testCreateBuildsProperlyNamespacedElement() throws Exception {
        Element created = XsdElement.ACTIVITY.create();
        assertNotNull(created);
        assertEquals("Wrong name", "activity", created.getName());
        assertEquals("Wrong namespace", AbstractStudyCalendarXmlSerializer.PSC_NS, created.getNamespaceURI());
    }
}
