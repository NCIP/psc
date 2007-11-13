package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;
import static edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools.createExternalObjectId;
import static edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools.loadFromExternalObjectId;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import static java.util.Arrays.asList;

/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
@Transactional
public class SiteService {
    private SiteDao siteDao;
    private StudySiteDao studySiteDao;
    private StudyCalendarAuthorizationManager authorizationManager;

    public Site createSite(Site site) throws Exception {
        siteDao.save(site);
        saveSiteProtectionGroup(createExternalObjectId(site));
        return site;
    }
    
    protected void saveSiteProtectionGroup(String siteName) throws Exception {
    	authorizationManager.createProtectionGroup(siteName);
    }

    public void assignProtectionGroup(Site site, User user, Role role) throws Exception {
        ProtectionGroup sitePG = authorizationManager.getPGByName(createExternalObjectId(site));
    	authorizationManager.assignProtectionGroupsToUsers(user.getCsmUserId().toString(), sitePG, role.csmRole());
    }

    public void removeProtectionGroup(Site site, User user) throws Exception {
        ProtectionGroup sitePG = authorizationManager.getPGByName(createExternalObjectId(site));
    	authorizationManager.removeProtectionGroupUsers(asList(user.getCsmUserId().toString()), sitePG);
    }

    public List<Site> getSitesForUser(String userName) {
        Set<Site> sites = new LinkedHashSet<Site>();
        sites.addAll(getSitesForSiteCd(userName));
        sites.addAll(getSitesForParticipantCoordinator(userName));

        return new ArrayList<Site>(sites);
    }

    public List<Site> getSitesForSiteCd(String userName) {
        List<ProtectionGroup> sitePGs = authorizationManager.getSitePGsForUser(userName);
        List<Site> sites = new ArrayList<Site>(sitePGs.size());
        for (ProtectionGroup sitePG : sitePGs) {
            sites.add(DomainObjectTools.loadFromExternalObjectId(sitePG.getProtectionGroupName(),siteDao));
        }
        return sites;
    }

    public Collection<Site> getSitesForParticipantCoordinator(String userName) {
        List<ProtectionGroup> studySitePGs = authorizationManager.getStudySitePGsForUser(userName);
        Set<Site> sites = new LinkedHashSet<Site>();
        for (ProtectionGroup studySitePG : studySitePGs) {
            StudySite studySite =
                    loadFromExternalObjectId(studySitePG.getProtectionGroupName(), studySiteDao);
            sites.add(studySite.getSite());
        }
        return sites;
    }

    public Collection<Site> getSitesForParticipantCoordinator(String userName, Study study) {
        Collection<Site> sites = getSitesForParticipantCoordinator(userName);
        Set<Site> sitesForStudy = new HashSet<Site>();
        for (Site site : sites) {
            if (StudySite.findStudySite(study, site) != null) {
                sitesForStudy.add(site);
            }
        }
        return sitesForStudy;
    }

    ////// CONFIGURATION

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    @Required
    public void setStudySiteDao(StudySiteDao studySiteDao) {
        this.studySiteDao = studySiteDao;
    }

    @Required
    public void setStudyCalendarAuthorizationManager(StudyCalendarAuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }
}
