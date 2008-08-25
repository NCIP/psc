package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Saurabh Agrawal
 */
public class SiteXmlSerializer extends AbstractStudyCalendarXmlCollectionSerializer<Site> {

    private final String SITE_NOT_FOUND_MESSAGE = "Site '%s' not found. Please define a site that exists.";

    private SiteDao siteDao;

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
        SITE_SITE_NM.addTo(siteElement, site.getName());
        SITE_ASSIGNED_IDENTIFIER.addTo(siteElement, site.getAssignedIdentifier());
        return siteElement;
    }

    @Override
    public Site readElement(Element element) {
        String siteName = element.attributeValue(SITE_SITE_NM.name());
        Site site = siteDao.getByName(siteName);
        if (site == null) {
            throw new StudyCalendarValidationException(SITE_NOT_FOUND_MESSAGE, siteName);
        }
        return site;
    }

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }
}
