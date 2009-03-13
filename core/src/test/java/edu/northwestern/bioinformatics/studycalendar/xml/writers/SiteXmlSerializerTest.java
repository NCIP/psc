package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Role.SUBJECT_COORDINATOR;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;
import org.dom4j.DocumentHelper;
import static org.easymock.EasyMock.expect;

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

    public void testCreateElementWithStudy() throws Exception {
        Study study = createNamedInstance("Protocol1",Study.class);
        StudySite studySite = Fixtures.createStudySite(study,site);
        StudiesXmlSerializer studiesXmlSerializer = registerMockFor(StudiesXmlSerializer.class);
        serializer.setStudiesXmlSerializer(studiesXmlSerializer);
        UserRole userRole = new UserRole();
        Role role = SUBJECT_COORDINATOR;
        userRole.setRole(role);
        userRole.addStudySite(studySite);
        serializer.setUserRole(userRole);
        Element eStudy = DocumentHelper.createElement("study");
        expect(studiesXmlSerializer.createElement(study)).andReturn(eStudy);

        replayMocks();
        Element actualElement = serializer.createElement(site);
        verifyMocks();


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

}

