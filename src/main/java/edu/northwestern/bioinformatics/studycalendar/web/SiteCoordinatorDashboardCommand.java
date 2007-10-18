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
    private Map<User,Map<Site, Boolean>> studyAssignmentGrid;
    private SiteDao siteDao;

    public SiteCoordinatorDashboardCommand(UserDao userDao, StudyDao studyDao, SiteDao siteDao) {
        this.userDao  = userDao;
        this.studyDao = studyDao;
        this.siteDao  = siteDao;

        buildStudyAssignmentGrid();
    }

    public void buildStudyAssignmentGrid() {
        studyAssignmentGrid = new HashMap<User,Map<Site, Boolean>>();
        List<User> users = userDao.getAllParticipantCoordinators();
        List<Site> sites = siteDao.getAll();

        for (User user : users) {
            if (!studyAssignmentGrid.containsKey(user)) studyAssignmentGrid.put(user, new HashMap<Site, Boolean>());

            for (Site site : sites) {
                studyAssignmentGrid.get(user).put(site, false);
            }
        }
    }

    public Map<User, Map<Site, Boolean>> getStudyAssignmentGrid() {
        return studyAssignmentGrid;
    }
}