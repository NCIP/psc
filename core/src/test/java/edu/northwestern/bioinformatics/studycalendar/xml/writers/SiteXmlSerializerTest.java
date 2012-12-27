/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

/**
 * @author Saurabh Agrawal
 */
public class SiteXmlSerializerTest extends StudyCalendarXmlTestCase {
    private SiteXmlSerializer serializer;
    private Site site;

    protected void setUp() throws Exception {
        super.setUp();

        serializer = new SiteXmlSerializer();

        site = createNamedInstance("Northwestern University", Site.class);
        site.setAssignedIdentifier("assigned id");
    }

    public void testCreateElement() {
        Element actualElement = serializer.createElement(site);
        assertEquals("Wrong element name", XsdElement.SITE.xmlName(), actualElement.getName());
        assertEquals("Wrong study name", "assigned id", actualElement.attributeValue("assigned-identifier"));
        assertEquals("Wrong site name", "Northwestern University", actualElement.attributeValue("site-name"));
    }

    public void testCreateElementWithProvider() throws Exception {
        site.setProvider("site provider");
        Element actualElement = serializer.createElement(site);
        assertNotNull(actualElement.attribute("provider"));
        assertEquals("Wrong provider", "site provider", actualElement.attributeValue("provider"));
    }

    public void testReadElement() {
        Element actual = XsdElement.SITE.create();
        SITE_SITE_NAME.addTo(actual,"site");
        SITE_ASSIGNED_IDENTIFIER.addTo(actual,"siteId");
        Site read = serializer.readElement(actual);
        assertNotNull(read);
        assertNull(read.getId());
        assertNull(null,read.getGridId());
        assertEquals("Wrong site name", "site", read.getName());
        assertEquals("wrong site assigned identifier", "siteId", read.getAssignedIdentifier());
    }

    public void testReadElementWithProvider() throws Exception {
        Element actual = XsdElement.SITE.create();
        SITE_SITE_NAME.addTo(actual,"site");
        SITE_PROVIDER.addTo(actual, "provider");
        Site read = serializer.readElement(actual);
        assertNotNull(read);
        assertEquals("Wrong site provider", "provider", read.getProvider());
    }

}

