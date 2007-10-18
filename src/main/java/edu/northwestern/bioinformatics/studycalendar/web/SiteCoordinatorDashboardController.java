package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

/**
 * @author John Dzak
 */
public class SiteCoordinatorDashboardController extends PscSimpleFormController {
    private StudyDao studyDao;
    private UserDao userDao;
    private SiteDao siteDao;


    public SiteCoordinatorDashboardController() {
        setFormView("siteCoordinatorDashboard");
    }


    protected Map referenceData(HttpServletRequest request) throws Exception {
        Map<String, Object> refdata = new HashMap<String,Object>();
        refdata.put("sites", siteDao.getAll());
        return refdata;
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        SiteCoordinatorDashboardCommand command = new SiteCoordinatorDashboardCommand(userDao, studyDao, siteDao);
        return command;
    }

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }
}
