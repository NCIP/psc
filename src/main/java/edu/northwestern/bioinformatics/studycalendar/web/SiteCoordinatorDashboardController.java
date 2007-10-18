package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.RoleEditor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

import org.springframework.web.bind.ServletRequestDataBinder;
import gov.nih.nci.cabig.ctms.editors.DaoBasedEditor;

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

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);

        binder.registerCustomEditor(Site.class, new DaoBasedEditor(siteDao));
        binder.registerCustomEditor(User.class, new DaoBasedEditor(userDao));
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
