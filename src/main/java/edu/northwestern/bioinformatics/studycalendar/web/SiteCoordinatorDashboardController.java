package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.NamedComparator;
import gov.nih.nci.cabig.ctms.editors.DaoBasedEditor;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author John Dzak
 */
@AccessControl(roles = {Role.SITE_COORDINATOR})
public class SiteCoordinatorDashboardController extends PscSimpleFormController {
    private StudyDao studyDao;
    private UserDao userDao;
    private SiteDao siteDao;
    private TemplateService templateService;
    private SiteService siteService;
    private UserService userService;
    private String MODE_ASSIGN_BY_USER = "byUser";


    public SiteCoordinatorDashboardController() {
        setFormView("siteCoordinatorDashboard");
    }


    protected Map referenceData(HttpServletRequest request, Object o, Errors errors) throws Exception {
        Map<String, Object> refdata = new HashMap<String,Object>();

        SiteCoordinatorDashboardCommand command = (SiteCoordinatorDashboardCommand) o;

        refdata.put("studies", command.getAssignableStudies() );
        refdata.put("sites"  , command.getAssignableSites());
        refdata.put("users"  , command.getAssignableUsers());

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
        User siteCoordinator = userDao.getByName(ApplicationSecurityManager.getUser());

        List<Site> assignableSites;
        List<User> assignableUsers    = userService.getParticipantCoordinatorsForSites(getSitesForUser(siteCoordinator));
        List<Study> assignableStudies = getAssignableStudies(siteCoordinator.getName());

        Collections.sort(assignableUsers, new NamedComparator());
        Collections.sort(assignableStudies, new NamedComparator());

        String mode = ServletRequestUtils.getStringParameter(request, "mode");
        AbstractGridCommand command;
        if ( MODE_ASSIGN_BY_USER.equals(mode) ) {
            command = null;
//            Integer userId      = ServletRequestUtils.getIntParameter(request, "user");
//            User selectedUser   = getCurrentUser(userId.toString(), assignableUsers);
//            command = new SiteCoordinatorDashboardCommandByUser(userRoleDao, templateService, siteService, selectedUser, siteCoordinator);
        } else {
            Integer studyId      = ServletRequestUtils.getIntParameter(request, "study");
            Study selectedStudy  = getCurrentStudy(studyId, assignableStudies);
            assignableSites    = getAssignableSites(siteCoordinator, selectedStudy);
            Collections.sort(assignableSites, new NamedComparator());

            command = new SiteCoordinatorDashboardCommand(templateService, selectedStudy, assignableStudies, assignableSites, assignableUsers);
        }
        return command;
    }



    protected List<Study> getAssignableStudies(String siteCoordinatorName) throws Exception {
        List<Study> studies = studyDao.getAll();
        List<Study> ownedStudies = templateService.checkOwnership(siteCoordinatorName, studies);

        List<Study> assignableStudies = new ArrayList<Study>();
         for (Study ownedStudy : ownedStudies) {
            if (ownedStudy.isAvailableForAssignment()) {
                assignableStudies.add(ownedStudy);
            }
         }
        return assignableStudies;
    }

    public List<Site> getSitesForUser(User user) {
        Set<Site> sites = new HashSet<Site>();
        for (UserRole userRole : user.getUserRoles()) {
            if (userRole.getSites().size() > 0)  {
                sites.addAll(userRole.getSites());
            }
        }
        return new ArrayList<Site>(sites);
    }

    public List<Site> getAssignableSites(User siteCoordinator, Study study) {
        List<Site> sitesForSiteCoord = siteService.getSitesForSiteCd(siteCoordinator.getName());
        List<Site> assignableStudySites = new ArrayList<Site>();
        for (Site site : sitesForSiteCoord) {
            if (StudySite.findStudySite(study, site) != null) {
                assignableStudySites.add(site);
            }
        }
        return assignableStudySites;
    }

    protected Study getCurrentStudy(Integer studyId, List<Study> assignableStudies) throws Exception {
        Study study = null;
        if (studyId != null ) {
            study = studyDao.getById(studyId);
        } else {
            if(assignableStudies.size() > 0) {
                study = assignableStudies.get(0);
            }
        }
        return study;
    }

    protected User getCurrentUser(String userId, List<User> assignableUsers) throws Exception {
        User user = null;
        if (userId != null ) {
            user = userDao.getById(Integer.parseInt(userId));
        } else {
            if(assignableUsers.size() > 0) {
                user = assignableUsers.get(0);
            }
        }
        return user;
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

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
