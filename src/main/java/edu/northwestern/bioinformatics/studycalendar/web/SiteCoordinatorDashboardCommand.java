package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.User;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class SiteCoordinatorDashboardCommand {
    private UserDao userDao;
    private StudyDao studyDao;
    private Map<User,Map<Study, Boolean>> studyAssignmentGrid;

    public SiteCoordinatorDashboardCommand(UserDao userDao, StudyDao studyDao) {
        this.userDao = userDao;
        this.studyDao = studyDao;

        buildStudyAssignmentGrid();
    }

    public void buildStudyAssignmentGrid() {
        studyAssignmentGrid = new HashMap<User,Map<Study, Boolean>>();
        List<User> users = userDao.getAll();
        List<Study> studies = studyDao.getAll();

        for (User user : users) {
            if (!studyAssignmentGrid.containsKey(user)) studyAssignmentGrid.put(user, new HashMap<Study, Boolean>());

            for (Study study : studies) {
                studyAssignmentGrid.get(user).put(study, false);
            }
        }
    }

    public Map<User, Map<Study, Boolean>> getStudyAssignmentGrid() {
        return studyAssignmentGrid;
    }
}