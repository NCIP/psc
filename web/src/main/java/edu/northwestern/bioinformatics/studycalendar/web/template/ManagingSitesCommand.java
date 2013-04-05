/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import org.springframework.validation.Errors;

import java.util.*;

/**
 * @author Nataliya Shurupova
 */
public class ManagingSitesCommand implements Validatable {
    private Map<Site, Boolean> userSitesToManageGrid;
    private Set<Site> managingSites;
    private StudyService studyService;
    private List<SuiteRoleMembership> listOfRoles;
    private Study study;
    private Boolean allSitesAccess;
    private Set<Object> selectableSites;
    private SiteDao siteDao;

    public ManagingSitesCommand(Study study, StudyService studyService, SiteDao siteDao, List<SuiteRoleMembership> listOfRoles) {
        this.listOfRoles = listOfRoles;
        this.study = study;
        this.studyService = studyService;
        this.siteDao = siteDao;
        this.selectableSites = buildListOfSiteObjectsBasedOnRole();
        this.managingSites = buildManagingSites();
        this.allSitesAccess = buildAllSitesAccess();
        buildUserSitesGrid();
    }

    public Set<Site> buildManagingSites() {
        managingSites = new HashSet<Site>();
        if (study.isManaged()) {
            managingSites = study.getManagingSites();
        }
        return managingSites;
    }

    public Set<Object> buildListOfSiteObjectsBasedOnRole() {
        List<Site> listOfSiteObj = new ArrayList<Site>();
        for (SuiteRoleMembership role : listOfRoles) {
            if (role != null) {
                if (role.isAllSites()) {
                    listOfSiteObj.addAll(siteDao.getAll());
                } else {
                    // reload sites so that they are from the current hibernate session
                    listOfSiteObj.addAll(siteDao.getByAssignedIdentifiers(role.getSiteIdentifiers()));
                }
            }
        }
        return new LinkedHashSet<Object>(listOfSiteObj);
    }

    public boolean buildAllSitesAccess() {
        allSitesAccess = false;

        for (SuiteRoleMembership role: listOfRoles) {
            if (role != null) {
                if (role.isAllSites()) {
                    if (!allSitesAccess) {
                        allSitesAccess = true;
                    }
                }
            }
        }
        return getAllSitesAccess();
    }


    public void buildUserSitesGrid() {
        userSitesToManageGrid = new LinkedHashMap<Site, Boolean>();
        for (Object site: selectableSites) {
            if (managingSites.contains(site)) {
                userSitesToManageGrid.put((Site)site, true);
            } else {
                userSitesToManageGrid.put((Site)site, false);
            }
        }
    }

    public void apply() {
        Set<Site> sites = userSitesToManageGrid.keySet();
        for (Site site: sites) {
            if (userSitesToManageGrid.get(site)) {
                study.addManagingSite(site);
            } else {
                study.removeManagingSite(site);
            }
        }
        studyService.save(study);
    }

    public void validate(Errors errors) {
        boolean isOneSiteSelected = false;
        if (!allSitesAccess) {
            Set<Site> sites = userSitesToManageGrid.keySet();
            for (Site site: sites) {
                if (userSitesToManageGrid.get(site)) {
                    isOneSiteSelected = true;
                    break;
                }
            }
            if (!isOneSiteSelected) {
                errors.reject("error.no.site.selected.for.managed.study.with.no.all.sites.access");
            }
        }

    }

    ////// BOUND PROPERTIES
    public Map<Site, Boolean> getUserSitesToManageGrid() {
        return userSitesToManageGrid;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public Set<Site> getManagingSites() {
        return managingSites;
    }

    public Boolean getAllSitesAccess() {
        return allSitesAccess;
    }

    public void setAllSitesAccess(Boolean allSitesAccess) {
        this.allSitesAccess = allSitesAccess;
    }

    public Set<Object> getSelectableSites() {
        return selectableSites;
    }
}
