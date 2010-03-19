package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;

/**
 * @author John Dzak
 */
public class StudySiteXmlSerializerTest extends StudyCalendarXmlTestCase {
    private StudySiteXmlSerializer serializer;
    private Element element;
    private Study study;
    private Site site;

    protected void setUp() throws Exception {
        super.setUp();

        serializer = new StudySiteXmlSerializer();
        study = createNamedInstance("Cancer Study", Study.class);
        site = createNamedInstance("Northwestern University", Site.class);

        element = createElement(study, site);
    }

    public void testCreateElement() {
        StudySite studySite = Fixtures.createStudySite(study, site);

        Element actual = serializer.createElement(studySite);

        assertEquals("Wrong element name", "study-site-link", actual.getName());
        assertEquals("Wrong study name", "Cancer Study", actual.attributeValue("study-name"));
        assertEquals("Wrong site name", "Northwestern University", actual.attributeValue("site-name"));
    }

    public void testReadElement() {
        StudySite studySite = Fixtures.createStudySite(study, site);

        replayMocks();
        StudySite actual = serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong StudySite", studySite, actual);
        assertEquals("Wrong Study Name", study.getAssignedIdentifier(), actual.getStudy().getAssignedIdentifier());
        assertEquals("Wrong Site Name", site.getName(), actual.getSite().getName());
    }

    //// Test Helper Methods
    private Element createElement(Study aStudy, Site aSite) {
        Element elt = new BaseElement("study-site-link");
        elt.addAttribute("study-name", aStudy.getName());
        elt.addAttribute("site-name", aSite.getName());
        return elt;
    }
}
