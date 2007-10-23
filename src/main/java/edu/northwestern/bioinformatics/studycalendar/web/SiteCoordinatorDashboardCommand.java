package edu.northwestern.bioinformatics.studycalendar.web;

import static edu.northwestern.bioinformatics.studycalendar.domain.UserRole.findByRole;
import static edu.northwestern.bioinformatics.studycalendar.domain.StudySite.findStudySite;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserRoleDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;

import java.util.*;

/**
 * @author John Dzak
 */
public class SiteCoordinatorDashboardCommand {
    private Map<User,Map<Site, StudyAssignmentCell>> studyAssignmentGrid;
    private SiteDao siteDao;
    private Study study;
    private UserRoleDao userRoleDao;

    public SiteCoordinatorDashboardCommand(SiteDao siteDao, UserRoleDao userRoleDao, Study study) {
        this.siteDao      = siteDao;
        this.userRoleDao  = userRoleDao;
        this.study        = study;

        buildStudyAssignmentGrid();
    }
    
    protected void buildStudyAssignmentGrid() {
        studyAssignmentGrid = new HashMap<User,Map<Site, StudyAssignmentCell>>();
        List<Site> sites    = siteDao.getAll();
        List<UserRole> usersRoles = userRoleDao.getAllParticipantCoordinators();

        for (UserRole userRole : usersRoles) {
            User user = userRole.getUser();
            if (!studyAssignmentGrid.containsKey(user)) studyAssignmentGrid.put(user, new HashMap<Site, StudyAssignmentCell>());

            for (Site site : sites) {
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

    public void apply() {
        for(User user : studyAssignmentGrid.keySet()) {
            UserRole userRole = findByRole(user.getUserRoles(), Role.PARTICIPANT_COORDINATOR);
            userRole.clearStudySites();
            for(Site site : studyAssignmentGrid.get(user).keySet()) {
                if (studyAssignmentGrid.get(user).get(site).isSelected()) {
                    userRole.addStudySite(findStudySite(study, site));
                }
            }
            userRoleDao.save(userRole);
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
}