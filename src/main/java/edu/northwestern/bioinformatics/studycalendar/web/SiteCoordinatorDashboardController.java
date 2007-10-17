package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;

import javax.servlet.http.HttpServletRequest;

/**
 * @author John Dzak
 */
public class SiteCoordinatorDashboardController extends PscSimpleFormController {
    private StudyDao studyDao;
    private UserDao userDao;

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        SiteCoordinatorDashboardCommand command = new SiteCoordinatorDashboardCommand(userDao, studyDao);
        return command;
    }

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
}
