package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.NamedComparator;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.mvc.SimpleFormController;
import gov.nih.nci.cabig.ctms.editors.DaoBasedEditor;

@AccessControl(roles = {Role.SITE_COORDINATOR})
public abstract class AbstractSiteCoordinatorDashboardController extends SimpleFormController {
    private StudyDao studyDao;
    private UserDao userDao;
    private SiteDao siteDao;
    private TemplateService templateService;
    private SiteService siteService;
    private UserService userService;

    protected Map referenceData(HttpServletRequest request, Object o, Errors errors) throws Exception {
        Map<String, Object> refdata = new HashMap<String,Object>();
        AbstractSiteCoordinatorDashboardCommand command = (AbstractSiteCoordinatorDashboardCommand) o;
        refdata.put("studies", command.getAssignableStudies() );
        refdata.put("sites"  , command.getAssignableSites());
        refdata.put("users"  , command.getAssignableUsers());

        return refdata;
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);

        binder.registerCustomEditor(Site.class, new DaoBasedEditor(siteDao));
        binder.registerCustomEditor(User.class, new DaoBasedEditor(userDao));
        binder.registerCustomEditor(Study.class, new DaoBasedEditor(studyDao));
    }

    protected User getSiteCoordinator() {
        return userDao.getByName(ApplicationSecurityManager.getUser());
    }

    protected List<Study> getAssignableStudies(User siteCoordinator) throws Exception {
        List<Study> studies      = studyDao.getAll();
        List<Study> ownedStudies = templateService.checkOwnership(siteCoordinator.getName(), studies);

        List<Study> assignableStudies = new ArrayList<Study>();
        for (Study ownedStudy : ownedStudies) {
            if (ownedStudy.isAvailableForAssignment()) {
                assignableStudies.add(ownedStudy);
            }
        }
        Collections.sort(assignableStudies, new NamedComparator());
        return assignableStudies;
    }

    protected List<Site> getAssignableSites(User siteCoordinator) {
        List<Site> sites = siteService.getSitesForSiteCd(siteCoordinator.getName());
        Collections.sort(sites, new NamedComparator());
        return sites;
    }

    protected List<Site> getAssignableSites(User siteCoordinator, Study study) {
        List<Site> sitesForSiteCoord = getAssignableSites(siteCoordinator);
        List<Site> assignableStudySites = new ArrayList<Site>();
        for (Site site : sitesForSiteCoord) {
            if (StudySite.findStudySite(study, site) != null) {
                assignableStudySites.add(site);
            }
        }
        Collections.sort(assignableStudySites, new NamedComparator());
        return assignableStudySites;
    }

    protected List<User> getAssignableUsers(User siteCoordinator) {
        List<Site> sites = new ArrayList<Site>();
        for (UserRole userRole : siteCoordinator.getUserRoles()) {
            if (userRole.getSites().size() > 0)  {
                sites.addAll(userRole.getSites());
            }
        }
        List<User> assignableUsers = userService.getParticipantCoordinatorsForSites(sites);
        Collections.sort(assignableUsers, new NamedComparator());
        return assignableUsers;
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

    public StudyDao getStudyDao() {
        return studyDao;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public TemplateService getTemplateService() {
        return templateService;
    }
}
