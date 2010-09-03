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
            Element roleSite = XsdElement.ROLE_SITE.create();
            if (userRoleMembership.isAllSites()) {
                XsdAttribute.ALL.addTo(roleSite, true);
            } else {
                XsdAttribute.ALL.addTo(roleSite, false);
                for (String siteIdentifier : userRoleMembership.getSiteIdentifiers()) {
                    Element site = XsdElement.SITE.create();
                    XsdAttribute.SITE_ASSIGNED_IDENTIFIER.addTo(site, siteIdentifier);
                    roleSite.add(site);
                }
            }
        userRoleElement.add(roleSite);
        }

        if (userRoleMembership.hasStudyScope()) {
            Element roleStudy = XsdElement.ROLE_STUDY.create();
            if (userRoleMembership.isAllStudies()) {
                XsdAttribute.ALL.addTo(roleStudy, true);
            } else {
                XsdAttribute.ALL.addTo(roleStudy, false);
                for (String studyIdentifier : userRoleMembership.getStudyIdentifiers()) {
                    Element study = XsdElement.STUDY.create();
                    XsdAttribute.STUDY_ASSIGNED_IDENTIFIER.addTo(study, studyIdentifier);
                    roleStudy.add(study);
                }
            }
        userRoleElement.add(roleStudy);
        }
        return userRoleElement;
    }

    @Override
    public SuiteRoleMembership readElement(Element element) {
        throw new UnsupportedOperationException("Functionality to read a user roles does not exist");
    }
}
