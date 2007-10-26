package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.UserRoleDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import static edu.northwestern.bioinformatics.studycalendar.domain.StudySite.findStudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * @author John Dzak
 */
public class SiteCoordinatorDashboardCommand {
    private Map<User,Map<Site, StudyAssignmentCell>> studyAssignmentGrid;
    private Study study;
    private UserRoleDao userRoleDao;
    private TemplateService templateService;
    private User siteCoordinator;
    private SiteService siteService;


    public SiteCoordinatorDashboardCommand(UserRoleDao userRoleDao, TemplateService templateService, SiteService siteService, Study study, User siteCoordinator) {
        this.userRoleDao     = userRoleDao;
        this.templateService = templateService;
        this.study           = study;
        this.siteCoordinator = siteCoordinator;
        this.siteService     = siteService;

        if (study != null) {
            buildStudyAssignmentGrid();
        }
    }
    
    protected void buildStudyAssignmentGrid() {
        studyAssignmentGrid = new HashMap<User,Map<Site, StudyAssignmentCell>>();
        List<Site> participCoordSites = siteService.getSitesForSiteCd(siteCoordinator.getName());
        List<UserRole> userRoles = userRoleDao.getAllParticipantCoordinators();
        List<UserRole> showableUserRoles = new ArrayList<UserRole>();

        for (UserRole userRole : userRoles) {
            for (Site site : userRole.getSites()) {
                if (participCoordSites.contains(site)) {
                    showableUserRoles.add(userRole);
                }
            }
        }


        for (UserRole userRole : showableUserRoles) {
            User user = userRole.getUser();
            if (!studyAssignmentGrid.containsKey(user)) studyAssignmentGrid.put(user, new HashMap<Site, StudyAssignmentCell>());

            for (Site site : participCoordSites) {
                    studyAssignmentGrid.get(user)
                            .put(site,
                                    createStudyAssignmentCell(isSiteSelected(userRole, study, site),
                                            isSiteAccessAllowed(userRole, site)));
            }
        }
    }

    protected boolean isSiteSelected(UserRole userRole, Study study, Site site) {
        return userRole.getStudySites().contains(findStudySite(study, site));
    }

    protected boolean isSiteAccessAllowed(UserRole userRole, Site site) {
        return userRole.getSites().contains(site);
    }

    public Map<User, Map<Site, StudyAssignmentCell>> getStudyAssignmentGrid() {
        return studyAssignmentGrid;
    }

    public void apply() throws Exception {
        for(User user : studyAssignmentGrid.keySet()) {
            for(Site site : studyAssignmentGrid.get(user).keySet()) {
                if (studyAssignmentGrid.get(user).get(site).isSelected()) {
                    templateService.assignTemplateToParticipantCoordinator(study,site, user);
                } else {
                    templateService.removeAssignedTemplateFromParticipantCoordinator(study,site, user);
                }
            }
        }
    }

    public static class StudyAssignmentCell {
        private boolean selected;
        private boolean siteAccessAllowed;

        public StudyAssignmentCell(boolean selected, boolean siteAccessAllowed) {
            this.selected = selected;
            this.siteAccessAllowed = siteAccessAllowed;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isSiteAccessAllowed() {
            return siteAccessAllowed;
        }

        public void setSiteAccessAllowed(boolean siteAccessAllowed) {
            this.siteAccessAllowed = siteAccessAllowed;
        }
    }

    protected static StudyAssignmentCell createStudyAssignmentCell(boolean selected, boolean siteAccessAllowed) {
        return new StudyAssignmentCell(selected, siteAccessAllowed);
    }

    public Study getStudy() {
        return study;
    }

    public User getSiteCoordinator() {
        return siteCoordinator;
    }
}