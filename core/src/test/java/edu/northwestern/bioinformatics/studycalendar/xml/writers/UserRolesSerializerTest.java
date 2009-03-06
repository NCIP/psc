package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Role.SUBJECT_COORDINATOR;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;
import org.dom4j.DocumentHelper;
import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class UserRolesSerializerTest  extends StudyCalendarXmlTestCase {
    private UserRolesSerializer serializer;
    private Role role;
    private UserRole userRole;
    private Site site;
    private SiteXmlSerializer siteXmlSerializer;
    private Element eSite;

    public void setUp() throws Exception {
        super.setUp();
        serializer = new UserRolesSerializer();
        siteXmlSerializer =  registerMockFor(SiteXmlSerializer.class);
        serializer.setSiteXmlSerializer(siteXmlSerializer);
        userRole = new UserRole();
        role = SUBJECT_COORDINATOR;
        site = Fixtures.createSite("NMH");
        userRole.setRole(role);
    }

    public void testCreateElementUserRole() throws Exception {
        Element actualElement = serializer.createElement(userRole);
        assertEquals("Wrong element name", XsdElement.USER_ROLE.xmlName(), actualElement.getName());
        assertEquals("Wrong role name","Subject coordinator",actualElement.attributeValue("name"));
    }

    public void testCreateElementUserRoleWithSite() throws Exception {
        userRole.addSite(site);
        eSite = DocumentHelper.createElement("site");
        expect(siteXmlSerializer.createElement(site)).andReturn(eSite);
        siteXmlSerializer.setUserRole(userRole);

        replayMocks();
        Element actualElement = serializer.createElement(userRole);
        verifyMocks();
    }
}
