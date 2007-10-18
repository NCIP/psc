package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class SiteCoordinatorDashboardCommand {
    private UserDao userDao;
    private StudyDao studyDao;
    private Map<User,Map<Site, StudyAssignmentCell>> studyAssignmentGrid;
    private SiteDao siteDao;

    public SiteCoordinatorDashboardCommand(UserDao userDao, StudyDao studyDao, SiteDao siteDao) {
        this.userDao  = userDao;
        this.studyDao = studyDao;
        this.siteDao  = siteDao;

        buildStudyAssignmentGrid();
    }

    public void buildStudyAssignmentGrid() {
        studyAssignmentGrid = new HashMap<User,Map<Site, StudyAssignmentCell>>();
        List<User> users = userDao.getAllParticipantCoordinators();
        List<Site> sites = siteDao.getAll();

        for (User user : users) {
            if (!studyAssignmentGrid.containsKey(user)) studyAssignmentGrid.put(user, new HashMap<Site, StudyAssignmentCell>());

            for (Site site : sites) {
                studyAssignmentGrid.get(user).put(site, createStudyAssignmentCell(false));
            }
        }
    }

    public Map<User, Map<Site, StudyAssignmentCell>> getStudyAssignmentGrid() {
        return studyAssignmentGrid;
    }

    public static class StudyAssignmentCell {
        private boolean selected;

        public StudyAssignmentCell(boolean selected) {
            this.selected = selected;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

    }

    protected static StudyAssignmentCell createStudyAssignmentCell(boolean selected) {
        return new StudyAssignmentCell(selected);
    }
}