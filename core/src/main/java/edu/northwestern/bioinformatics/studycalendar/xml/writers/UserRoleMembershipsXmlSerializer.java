/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import org.dom4j.Element;

/**
 * @author Jalpa Patel
 */
public class UserRoleMembershipsXmlSerializer extends AbstractStudyCalendarXmlCollectionSerializer<SuiteRoleMembership> {
    @Override
    protected XsdElement collectionRootElement() {
        return XsdElement.USER_ROLES;
    }

    @Override
    protected XsdElement rootElement() {
        return XsdElement.USER_ROLE;
    }

    @Override
    protected Element createElement(SuiteRoleMembership userRoleMembership, boolean inCollection) {
        Element userRoleElement = rootElement().create();
        XsdAttribute.USER_ROLE_NAME.addTo(userRoleElement, userRoleMembership.getRole().getDisplayName());
        if (userRoleMembership.hasSiteScope()) {
            Element roleSites = XsdElement.ROLE_SITES.create();
            if (userRoleMembership.isAllSites()) {
                XsdAttribute.ALL.addTo(roleSites, true);
            } else {
                XsdAttribute.ALL.addTo(roleSites, false);
                for (String siteIdentifier : userRoleMembership.getSiteIdentifiers()) {
                    Element site = XsdElement.SITE.create();
                    XsdAttribute.SITE_ASSIGNED_IDENTIFIER.addTo(site, siteIdentifier);
                    roleSites.add(site);
                }
            }
        userRoleElement.add(roleSites);
        }

        if (userRoleMembership.hasStudyScope()) {
            Element roleStudies = XsdElement.ROLE_STUDIES.create();
            if (userRoleMembership.isAllStudies()) {
                XsdAttribute.ALL.addTo(roleStudies, true);
            } else {
                XsdAttribute.ALL.addTo(roleStudies, false);
                for (String studyIdentifier : userRoleMembership.getStudyIdentifiers()) {
                    Element study = XsdElement.STUDY.create();
                    XsdAttribute.STUDY_ASSIGNED_IDENTIFIER.addTo(study, studyIdentifier);
                    roleStudies.add(study);
                }
            }
        userRoleElement.add(roleStudies);
        }
        return userRoleElement;
    }

    @Override
    public SuiteRoleMembership readElement(Element element) {
        throw new UnsupportedOperationException("Functionality to read a user roles does not exist");
    }
}
