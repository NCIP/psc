package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class SiteCoordinatorDashboardCommand {
    private UserDao userDao;
    private Map<User,Map<Site, StudyAssignmentCell>> studyAssignmentGrid;
    private SiteDao siteDao;
    private Study study;

    public SiteCoordinatorDashboardCommand(UserDao userDao, SiteDao siteDao, Study study) {
        this.userDao  = userDao;
        this.siteDao  = siteDao;
        this.study    = study;

        buildStudyAssignmentGrid();
    }

    public void buildStudyAssignmentGrid() {
        studyAssignmentGrid = new HashMap<User,Map<Site, StudyAssignmentCell>>();
        List<User> users = userDao.getAllParticipantCoordinators();
        List<Site> sites = siteDao.getAll();

        for (User user : users) {
            if (!studyAssignmentGrid.containsKey(user)) studyAssignmentGrid.put(user, new HashMap<Site, StudyAssignmentCell>());

            for (Site site : sites) {

                UserRole participCoordRole = getParticipantCoordinatorUserRole(user);

                if (participCoordRole != null) {
                    studyAssignmentGrid.get(user)
                            .put(site,
                                    createStudyAssignmentCell(isSiteSelected(participCoordRole, study, site),
                                            isSiteAccessAllowed(participCoordRole, site)));
                }
            }
        }
    }

    protected UserRole getParticipantCoordinatorUserRole(User user) {
        for (UserRole userRole : user.getUserRoles()) {
            if (Role.PARTICIPANT_COORDINATOR.equals(userRole.getRole())) {      // There should only be one
                return userRole;                                                // participant coordinator role
            }
        }
        return null;
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