package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserRoleDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import gov.nih.nci.cabig.ctms.editors.DaoBasedEditor;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.validation.Errors;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * @author John Dzak
 */
@AccessControl(roles = {Role.PARTICIPANT_COORDINATOR})
public class SiteCoordinatorDashboardController extends PscSimpleFormController {
    private StudyDao studyDao;
    private UserDao userDao;
    private SiteDao siteDao;
    private UserRoleDao userRoleDao;
    private TemplateService templateService;


    public SiteCoordinatorDashboardController() {
        setFormView("siteCoordinatorDashboard");
    }


    protected Map referenceData(HttpServletRequest request, Object o, Errors errors) throws Exception {
        Map<String, Object> refdata = new HashMap<String,Object>();

        SiteCoordinatorDashboardCommand command = (SiteCoordinatorDashboardCommand) o;

        refdata.put("studies",  getAssignableStudies(ApplicationSecurityManager.getUser()));
        refdata.put("sites", siteDao.getAll());
        refdata.put("currentStudy", command.getStudy());
        return refdata;
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);

        binder.registerCustomEditor(Site.class, new DaoBasedEditor(siteDao));
        binder.registerCustomEditor(User.class, new DaoBasedEditor(userDao));
        binder.registerCustomEditor(Study.class, new DaoBasedEditor(studyDao));
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        Integer studyId = ServletRequestUtils.getIntParameter(request, "study");
        Study selectedStudy = getCurrentStudy(studyId, ApplicationSecurityManager.getUser());

        SiteCoordinatorDashboardCommand command = new SiteCoordinatorDashboardCommand(siteDao, userRoleDao, templateService, selectedStudy);
        return command;
    }

    protected Study getCurrentStudy(Integer studyId, String userName) throws Exception {
        Study study = null;
        if (studyId != null ) {
            study = studyDao.getById(studyId);
        } else {
            List<Study> assignableStudies = getAssignableStudies(userName);
            if(assignableStudies.size() > 0) {
                study = assignableStudies.get(0);
            }
        }
        return study;
    }

    protected List<Study> getAssignableStudies(String userName) throws Exception {
        List<Study> studies = studyDao.getAll();
        List<Study> ownedStudies = templateService.checkOwnership(userName, studies);

        List<Study> assignableStudies = new ArrayList<Study>();
         for (Study ownedStudy : ownedStudies) {
            if (ownedStudy.isAvailableForAssignment()) {
                assignableStudies.add(ownedStudy);
            }
         }
        return assignableStudies;
    }

    protected ModelAndView onSubmit(Object o) throws Exception {
        SiteCoordinatorDashboardCommand command = (SiteCoordinatorDashboardCommand) o;
        command.apply();

        RedirectView rv = new RedirectView("siteCoordinatorSchedule");
        rv.addStaticAttribute("study", command.getStudy().getId());
        return new ModelAndView(rv);
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

    public void setUserRoleDao(UserRoleDao userRoleDao) {
        this.userRoleDao = userRoleDao;
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
}
