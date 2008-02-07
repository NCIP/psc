package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;
import static org.easymock.EasyMock.expect;

/**
 * @author John Dzak
 */
public class StudySiteXmlSerializerTest extends StudyCalendarXmlTestCase {
    private StudySiteXmlSerializer serializer;
    private StudySite studySite;
    private Element element;
    private Study study;
    private Site site;
    private StudyDao studyDao;
    private SiteDao siteDao;

    protected void setUp() throws Exception {
        super.setUp();

        studyDao = registerMockFor(StudyDao.class);
        siteDao = registerMockFor(SiteDao.class);

        serializer = new StudySiteXmlSerializer();
        serializer.setStudyDao(studyDao);
        serializer.setSiteDao(siteDao);

        study = createNamedInstance("Cancer Study", Study.class);
        site = createNamedInstance("Northwestern University", Site.class);
        studySite = Fixtures.createStudySite(study, site);

        element = createElement(study, site);
    }

    public void testCreateElement() {
        Element actual = serializer.createElement(studySite);

        assertEquals("Wrong element name", "study-site-link", actual.getName());
        assertEquals("Wrong study name", "Cancer Study", actual.attributeValue("study-name"));
        assertEquals("Wrong site name", "Northwestern University", actual.attributeValue("site-name"));
    }

    public void testReadElementWhereStudySiteExists() {
        StudySite actual = expectStudySiteLookup(study, site, element);

        assertSame("StudySite should be the same", studySite, actual);
    }

     public void testReadElementWhereStudySiteIsNew() {
        Study polioStudy = createNamedInstance("Polio Study", Study.class);
        Element newStudySiteElement = createElement(polioStudy, site);

        StudySite actual = expectStudySiteLookup(polioStudy, site,  newStudySiteElement);

        assertSame("Study should be the same", polioStudy, actual.getStudy());
        assertEquals("Site should be the same", site, actual.getSite());
    }

    public void testReadElementForNonExistantStudy() {
        Study invalidStudy = createNamedInstance("Invalid Study", Study.class);
        Element invalidElement = createElement(invalidStudy, site);

        expect(studyDao.getByAssignedIdentifier("Invalid Study")).andReturn(null);
        replayMocks();

        try {
            serializer.readElement(invalidElement);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("Study 'Invalid Study' not found. Please define a study that exists.",
                scve.getMessage());
        }
    }

    public void testReadElementForNonExistantSite() {
        Site invalidSite = createNamedInstance("Invalid Site", Site.class);
        Element invalidElement = createElement(study, invalidSite);

        expect(studyDao.getByAssignedIdentifier("Cancer Study")).andReturn(study);
        expect(siteDao.getByAssignedIdentifier("Invalid Site")).andReturn(null);
        replayMocks();

        try {
            serializer.readElement(invalidElement);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("Site 'Invalid Site' not found. Please define a site that exists.",
                scve.getMessage());
        }
    }



    private Element createElement(Study aStudy, Site aSite) {
        Element elt = new BaseElement("study-site-link");
        elt.addAttribute("study-name", aStudy.getName());
        elt.addAttribute("site-name", aSite.getName());
        return elt;
    }

    private StudySite expectStudySiteLookup(Study aStudy, Site aSite, Element aElement) {
        expect(studyDao.getByAssignedIdentifier(aStudy.getAssignedIdentifier())).andReturn(aStudy);
        expect(siteDao.getByAssignedIdentifier(aSite.getName())).andReturn(aSite);

        replayMocks();

        StudySite actual = serializer.readElement(aElement);
        verifyMocks();

        return actual;
    }
}
