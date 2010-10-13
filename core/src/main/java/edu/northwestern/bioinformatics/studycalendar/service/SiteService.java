package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.BlackoutDate;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.SiteConsumer;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
@Transactional
public class SiteService {
    private SiteDao siteDao;
    private SiteConsumer siteConsumer;

    public Site getById(int id) {
        return nullSafeRefresh(siteDao.getById(id));
    }

    public Site getByAssignedIdentifier(final String assignedIdentifier) {
        return nullSafeRefresh(siteDao.getByAssignedIdentifier(assignedIdentifier));
    }

    private Site nullSafeRefresh(Site site) {
        if (site == null) {
            return null;
        } else {
            return siteConsumer.refresh(site);
        }
    }

    public List<Site> getAll() {
        return siteConsumer.refresh(siteDao.getAll());
    }

    public Site getByName(final String name) {
        return nullSafeRefresh(siteDao.getByName(name));
    }
    
    public Site createOrUpdateSite(Site site) {
        siteDao.save(site);
        return site;
    }

    public void removeSite(final Site site) throws Exception {
        if (!site.hasAssignments()) {
            for (StudySite studySite: site.getStudySites()) {
                studySite.getStudy().getStudySites().remove(studySite);
            }
            siteDao.delete(site);
        }
    }

    /**
     * Creates a new site if existing site is null. Or merge existing site with new site if existing site is not null
     */
    public Site createOrMergeSites(final Site existingSite, final Site newSite) throws Exception {
        if (existingSite == null) {
            return createOrUpdateSite(newSite);
        } else if (existingSite.getProvider() == null){
            Site site = getById(existingSite.getId());
            site.setName(newSite.getName());
            if (newSite.getAssignedIdentifier() != null) site.setAssignedIdentifier(newSite.getAssignedIdentifier());
            return createOrUpdateSite(site);
        } else {
            throw new StudyCalendarSystemException("The provided site %s is not editable", existingSite.getAssignedIdentifier());
        }
    }

    public BlackoutDate resolveSiteForBlackoutDate(BlackoutDate blackoutDate) {
        Site site = siteDao.getByAssignedIdentifier(blackoutDate.getSite().getAssignedIdentifier());
        if (site == null) {
            throw new StudyCalendarValidationException("Site '%s' not found. Please define a site that exists.",
                    blackoutDate.getSite().getAssignedIdentifier());
        }
        blackoutDate.setSite(site);
        return blackoutDate;
    }

    ////// CONFIGURATION

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    @Required
    public void setSiteConsumer(SiteConsumer siteConsumer) {
        this.siteConsumer = siteConsumer;
    }
}
