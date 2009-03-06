package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import org.dom4j.Element;

/**
 * @author Jalpa Patel
 */
public class UserRolesSerializer  extends AbstractStudyCalendarXmlCollectionSerializer<UserRole> {
    private SiteXmlSerializer siteXmlSerializer;
    @Override
    protected XsdElement collectionRootElement() {
        return XsdElement.USER_ROLES;
    }

    @Override
    protected XsdElement rootElement() {
        return XsdElement.USER_ROLE;
    }

    @Override
    protected Element createElement(UserRole userRole, boolean inCollection) {
        Element userRoleElement = rootElement().create();
        XsdAttribute.USER_ROLE_NAME.addTo(userRoleElement, userRole.getRole().getDisplayName());
        if (!userRole.getSites().isEmpty()) {
            siteXmlSerializer.setUserRole(userRole);
            for (Site site : userRole.getSites()) {
                  userRoleElement.add(siteXmlSerializer.createElement(site));
            }
        }
        return userRoleElement;
    }

    @Override
    public UserRole readElement(Element element) {
        return null;
    }

    public void setSiteXmlSerializer(SiteXmlSerializer siteXmlSerializer) {
        this.siteXmlSerializer = siteXmlSerializer;
    }

}
