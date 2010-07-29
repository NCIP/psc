package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import org.springframework.validation.Errors;
import java.util.*;

/**
 * @author Nataliya Shurupova
 */
public class AssociateSiteCommand implements Validatable {
    private Map<Site, Boolean> userSitesToManageGrid;
    private Set<Object> unique;
    private Set<Site> managingSites;
    private StudyService studyService;
    private Study study;
    private Boolean allSitesAccess;
    

    public AssociateSiteCommand(Study study, StudyService studyService, Set<Object> unique, Set<Site> managingSites, Boolean allSitesAccess) {
        this.unique = unique;
        this.managingSites = managingSites;
        this.studyService = studyService;
        this.study = study;
        this.allSitesAccess = allSitesAccess;
        buildUserSitesGrid();
    }

    public void buildUserSitesGrid() {
        userSitesToManageGrid = new LinkedHashMap<Site, Boolean>();

        for (Object site: unique) {
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
}
