/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import org.dom4j.Element;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createBasicTemplate;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationScopeMappings.createSuiteRoleMembership;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSite;

/**
 * @author Jalpa Patel
 */
public class UserRoleMembershipsXmlSerializerTest extends StudyCalendarXmlTestCase {
    private UserRoleMembershipsXmlSerializer serializer;
    private Study study;
    private Site site;

    public void setUp() throws Exception {
        super.setUp();
        serializer = new UserRoleMembershipsXmlSerializer();
        study = createBasicTemplate("F");
        site = createSite("NU", "NU01");
        study.addSite(site);
    }

    public void testCreateElementUserRole() throws Exception {
        SuiteRoleMembership mem = createSuiteRoleMembership(PscRole.SYSTEM_ADMINISTRATOR);
        Element actualElement = serializer.createElement(mem);
        assertEquals("Wrong element name", XsdElement.USER_ROLE.xmlName(), actualElement.getName());
        assertEquals("Wrong role name","System Administrator",actualElement.attributeValue("name"));
    }

    public void testCreateElementWithAllSites() throws Exception {
        SuiteRoleMembership mem = createSuiteRoleMembership(PscRole.STUDY_CREATOR).forAllSites();
        Element actualElement = serializer.createElement(mem);
        Element roleSitesElement = actualElement.element(XsdElement.ROLE_SITES.xmlName());
        assertEquals("Wrong element name", XsdElement.ROLE_SITES.xmlName(), roleSitesElement.getName());
        assertEquals("Wrong all site scope", "true", roleSitesElement.attributeValue(XsdAttribute.ALL.xmlName()));
    }

    public void testCreateElementWithAnySites() throws Exception {
        SuiteRoleMembership mem = createSuiteRoleMembership(PscRole.STUDY_CREATOR).forSites(site);
        Element actualElement = serializer.createElement(mem);
        Element roleSitesElement = actualElement.element(XsdElement.ROLE_SITES.xmlName());
        assertEquals("Wrong all site scope", "false", roleSitesElement.attributeValue(XsdAttribute.ALL.xmlName()));
        Element siteElement = roleSitesElement.element(XsdElement.SITE.xmlName());
        assertEquals("Wrong element name", XsdElement.SITE.xmlName(), siteElement.getName());
        assertEquals("Wrong site identifier", site.getAssignedIdentifier(),
                siteElement.attributeValue(XsdAttribute.SITE_ASSIGNED_IDENTIFIER.xmlName()));
    }

    public void testCreateElementWithAllStudies() throws Exception {
        SuiteRoleMembership mem = createSuiteRoleMembership(PscRole.STUDY_CREATOR).forAllStudies();
        Element actualElement = serializer.createElement(mem);
        Element roleStudiesElement = actualElement.element(XsdElement.ROLE_STUDIES.xmlName());
        assertEquals("Wrong element name", XsdElement.ROLE_STUDIES.xmlName(), roleStudiesElement.getName());
        assertEquals("Wrong all study scope", "true", roleStudiesElement.attributeValue(XsdAttribute.ALL.xmlName()));
    }

    public void testCreateElementWithAnyStudies() throws Exception {
        SuiteRoleMembership mem = createSuiteRoleMembership(PscRole.STUDY_CREATOR).forStudies(study);
        Element actualElement = serializer.createElement(mem);
        Element roleStudiesElement = actualElement.element(XsdElement.ROLE_STUDIES.xmlName());
        assertEquals("Wrong all study scope", "false", roleStudiesElement.attributeValue(XsdAttribute.ALL.xmlName()));
        Element studyElement = roleStudiesElement.element(XsdElement.STUDY.xmlName());
        assertEquals("Wrong element name", XsdElement.STUDY.xmlName(), studyElement.getName());
        assertEquals("Wrong study identifier", study.getAssignedIdentifier(),
                studyElement.attributeValue(XsdAttribute.STUDY_ASSIGNED_IDENTIFIER.xmlName()));
    }
}
