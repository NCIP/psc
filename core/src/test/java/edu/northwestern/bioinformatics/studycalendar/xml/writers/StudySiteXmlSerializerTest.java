/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;

/**
 * @author John Dzak
 */
public class StudySiteXmlSerializerTest extends StudyCalendarXmlTestCase {
    private StudySiteXmlSerializer serializer;
    private Element element;
    private Study study;
    private Site site;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        serializer = new StudySiteXmlSerializer();
        study = createNamedInstance("Cancer Study", Study.class);
        site = createSite("Northwestern University", "IL036");

        element = createElement(study, site);
    }

    public void testCreateElement() {
        StudySite studySite = Fixtures.createStudySite(study, site);

        Element actual = serializer.createElement(studySite);

        assertEquals("Wrong element name", "study-site-link", actual.getName());
        assertEquals("Wrong study name", "Cancer Study", actual.attributeValue("study-identifier"));
        assertEquals("Wrong site name", "Northwestern University", actual.attributeValue("site-identifier"));
    }

    public void testReadElement() {
        replayMocks();
        StudySite actual = serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong study identifier", "Cancer Study", actual.getStudy().getAssignedIdentifier());
        assertEquals("Wrong site identifier", "IL036", actual.getSite().getAssignedIdentifier());
    }

    //// Test Helper Methods

    private Element createElement(Study aStudy, Site aSite) {
        Element elt = new BaseElement("study-site-link");
        elt.addAttribute("study-identifier", aStudy.getAssignedIdentifier());
        elt.addAttribute("site-identifier", aSite.getAssignedIdentifier());
        return elt;
    }
}
