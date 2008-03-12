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
    
    public void testXmlNameDefaultSubsDashesForUnderscores() throws Exception {
        assertEquals("amendment-approvals", XsdElement.AMENDMENT_APPROVALS.xmlName());
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

    public void testGetFromParentWithMultiple() throws Exception {
        Element parent = XsdElement.ACTIVITY_SOURCE.create();
        Element expected = XsdElement.ACTIVITY.create();
        parent.add(expected);
        parent.add(XsdElement.ACTIVITY.create());

        assertSame("First child not returned", expected, XsdElement.ACTIVITY.from(parent));
    }

    public void testGetFromParentWithOne() throws Exception {
        Element parent = XsdElement.ACTIVITY_SOURCE.create();
        Element expected = XsdElement.ACTIVITY.create();
        parent.add(expected);

        assertSame("Not found", expected, XsdElement.ACTIVITY.from(parent));
    }

    public void testGetFromParentWithNone() throws Exception {
        Element parent = XsdElement.ACTIVITY_SOURCE.create();
        parent.add(XsdElement.AMENDMENT.create());

        assertNull("Found something when nothing matched", XsdElement.ACTIVITY.from(parent));
    }

    public void testGetAllFromParentWithMultiple() throws Exception {
        Element parent = XsdElement.ACTIVITY_SOURCE.create();
        Element expected0 = XsdElement.ACTIVITY.create();
        Element expected1 = XsdElement.ACTIVITY.create();
        parent.add(expected0);
        parent.add(expected1);

        assertSame("0th child not returned", expected0, XsdElement.ACTIVITY.allFrom(parent).get(0));
        assertSame("1th child not returned", expected1, XsdElement.ACTIVITY.allFrom(parent).get(1));
    }

    public void testGetAllFromParentWithOne() throws Exception {
        Element parent = XsdElement.ACTIVITY_SOURCE.create();
        Element expected = XsdElement.ACTIVITY.create();
        parent.add(expected);

        assertSame("Not found", expected, XsdElement.ACTIVITY.allFrom(parent).get(0));
    }

    public void testGetAllFromParentWithNone() throws Exception {
        Element parent = XsdElement.ACTIVITY_SOURCE.create();
        parent.add(XsdElement.AMENDMENT.create());

        assertTrue("Found something when nothing matched", XsdElement.ACTIVITY.allFrom(parent).isEmpty());
    }
}
