package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserRoleDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void buildStudyAssignmentGrid() {
        studyAssignmentGrid = new HashMap<User,Map<Site, StudyAssignmentCell>>();
        List<Site> sites    = siteDao.getAll();
        List<UserRole> usersRoles = userRoleDao.getAllParticipantCoordinatorUserRoles();

        for (UserRole userRole : usersRoles) {
            if (!studyAssignmentGrid.containsKey(userRole)) studyAssignmentGrid.put(userRole.getUser(), new HashMap<Site, StudyAssignmentCell>());

            for (Site site : sites) {
                    studyAssignmentGrid.get(userRole.getUser())
                            .put(site,
                                    createStudyAssignmentCell(isSiteSelected(userRole, study, site),
                                            isSiteAccessAllowed(userRole, site)));
            }
        }
    }

    protected boolean isSiteSelected(UserRole userRole, Study study, Site site) {
        for (StudySite studySite : userRole.getStudySites()) {
            if (site.equals(studySite.getSite()) && study.equals(studySite.getStudy())) return true;
        }
        return false;
    }

    protected boolean isSiteAccessAllowed(UserRole userRole, Site site) {
        return userRole.getSites().contains(site);
    }

    public Map<User, Map<Site, StudyAssignmentCell>> getStudyAssignmentGrid() {
        return studyAssignmentGrid;
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
}