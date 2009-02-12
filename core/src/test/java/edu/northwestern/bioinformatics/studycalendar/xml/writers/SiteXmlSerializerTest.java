package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;
import static org.easymock.EasyMock.expect;

/**
 * @author Saurabh Agrawal
 */
public class SiteXmlSerializerTest extends StudyCalendarXmlTestCase {
    private SiteXmlSerializer serializer;
    private Site site;
    private SiteDao siteDao;

    protected void setUp() throws Exception {
        super.setUp();

        siteDao = registerMockFor(SiteDao.class);

        serializer = new SiteXmlSerializer();
        serializer.setSiteDao(siteDao);

        site = createNamedInstance("Northwestern University", Site.class);
        site.setAssignedIdentifier("assigned id");
    }

    public void testCreateElement() {
        Element actualElement = serializer.createElement(site);
        assertEquals("Wrong element name", XsdElement.SITE.xmlName(), actualElement.getName());
        assertEquals("Wrong study name", "assigned id", actualElement.attributeValue("assigned-identifier"));
        assertEquals("Wrong site name", "Northwestern University", actualElement.attributeValue("site-name"));
    }

    public void testReadElementForExitingSite() {

        expect(siteDao.getByName(site.getName())).andReturn(site);
        replayMocks();

        Element aElement = createElement(site);
        Site actual = serializer.readElement(aElement);
        verifyMocks();

        assertEquals("Site should be the same", site, actual);
    }

    public void testReadElementForNonExistantSite() {
        Site invalidSite = createNamedInstance("Invalid Site", Site.class);
        Element invalidElement = createElement(invalidSite);

        expect(siteDao.getByName("Invalid Site")).andReturn(null);
        replayMocks();

        try {
            serializer.readElement(invalidElement);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            verifyMocks();
            assertEquals("Site 'Invalid Site' not found. Please define a site that exists.",
                    scve.getMessage());
        }
    }

    //// Test Helper Methods
    private Element createElement(Site aSite) {
        Element elt = new BaseElement(XsdElement.SITE.name());
        elt.addAttribute(SITE_SITE_NAME.name(), aSite.getName());
        elt.addAttribute(SITE_ASSIGNED_IDENTIFIER.name(), site.getAssignedIdentifier());

        return elt;
    }
}

