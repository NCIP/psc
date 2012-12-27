/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

/**
 * @author Saurabh Agrawal
 */
public class SiteXmlSerializer extends AbstractStudyCalendarXmlCollectionSerializer<Site> {

    @Override
    protected XsdElement collectionRootElement() {
        return XsdElement.SITES;
    }

    @Override
    protected XsdElement rootElement() {
        return XsdElement.SITE;
    }

    @Override
    protected Element createElement(final Site site, final boolean inCollection) {
        Element siteElement = rootElement().create();
        SITE_SITE_NAME.addTo(siteElement, site.getName());
        SITE_ASSIGNED_IDENTIFIER.addTo(siteElement, site.getAssignedIdentifier());

        if (site.getProvider() != null && site.getProvider().length() >0) {
            SITE_PROVIDER.addTo(siteElement, site.getProvider());
        }
        return siteElement;
    }

    @Override
    public Site readElement(Element element) {
        String siteName = SITE_SITE_NAME.from(element);
        if (siteName ==  null) {
            throw new StudyCalendarValidationException("Site name is required");
        }
        String siteAssignedIdentifier = SITE_ASSIGNED_IDENTIFIER.from(element);
        Site site = new Site();
        site.setName(siteName);
        if (siteAssignedIdentifier!=null) {
            site.setAssignedIdentifier(siteAssignedIdentifier);
        }
        String provider =  SITE_PROVIDER.from(element);
        if (provider != null && provider.length() > 0) {
            site.setProvider(provider);
        }
        return site;
    }
}
