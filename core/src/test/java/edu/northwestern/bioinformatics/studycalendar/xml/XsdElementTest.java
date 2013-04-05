/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

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

    public void testForCorrespondingClassWorksWhenKnown() throws Exception {
        assertEquals(XsdElement.STUDY_SEGMENT, XsdElement.forCorrespondingClass(StudySegment.class));
    }

    public void testForCorrespondingClassWorksOnSubclass() throws Exception {
        assertEquals(XsdElement.EPOCH, XsdElement.forCorrespondingClass(new Epoch() { }.getClass()));
    }

    public void testForCorrespondingClassThrowsIllegalArgumentWhenUnknown() throws Exception {
        try {
            XsdElement.forCorrespondingClass(String.class);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("No XsdElement corresponds to java.lang.String.", iae.getMessage());
        }
    }

    public void testForElementWorksWhenKnown() throws Exception {
        assertEquals(
            XsdElement.PLANNED_ACTIVITY_LABEL, XsdElement.forElement(new DefaultElement("label")));
    }

    public void testForElementThrowsIllegalArgumentExceptionWhenUnknown() throws Exception {
        try {
            XsdElement.forElement(new DefaultElement("foobar"));
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("No XsdElement for element foobar.", iae.getMessage());
        }
    }
}
