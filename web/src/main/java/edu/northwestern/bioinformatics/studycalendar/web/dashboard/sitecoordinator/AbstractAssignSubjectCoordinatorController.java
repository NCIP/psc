package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.NamedComparator;
import edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService;
import edu.northwestern.bioinformatics.studycalendar.service.StudySiteService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.Crumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.CrumbSource;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import gov.nih.nci.cabig.ctms.editors.DaoBasedEditor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AccessControl(roles = {Role.SITE_COORDINATOR})
public abstract class AbstractAssignSubjectCoordinatorController extends SimpleFormController implements CrumbSource {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private StudyDao studyDao;
    private UserDao userDao;
    private SiteDao siteDao;
    private TemplateService templateService;
    private UserService userService;
    private Crumb crumb;
    private ApplicationSecurityManager applicationSecurityManager;
    private AuthorizationService authorizationService;
    private StudySiteService studySiteService;

    protected Map referenceData(HttpServletRequest request, Object o, Errors errors) throws Exception {
        Map<String, Object> refdata = new HashMap<String, Object>();
        AbstractAssignSubjectCoordinatorCommand command = (AbstractAssignSubjectCoordinatorCommand) o;
        refdata.put("studies", command.getAssignableStudies());
        refdata.put("sites", command.getAssignableSites());
        List<User> assignableUsers = command.getAssignableUsers();
        List<User> enabledUsers = new ArrayList<User>();
        List<User> disabledUsers = new ArrayList<User>();
        for (User user : assignableUsers) {
            if (user.isEnabled()) {
                enabledUsers.add(user);
            } else {
                disabledUsers.add(user);
            }
        }
        List<User> allUsers = enabledUsers;
        allUsers.addAll(disabledUsers);
        refdata.put("users", allUsers);
        if(!StringUtils.isBlank(request.getParameter("flashMessage"))){
            refdata.put("flashMessage",request.getParameter("flashMessage"));
        }
        return refdata;
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);

        binder.registerCustomEditor(Site.class, new DaoBasedEditor(siteDao));
        binder.registerCustomEditor(User.class, new DaoBasedEditor(userDao));
        binder.registerCustomEditor(Study.class, new DaoBasedEditor(studyDao));
    }

    protected User getSiteCoordinator() {
        return applicationSecurityManager.getFreshUser(true);
    }

    protected List<Study> getAssignableStudies(User siteCoordinator) throws Exception {
        List<Study> studies = studyDao.getAll();
        studySiteService.refreshStudySitesForStudies(studies);

        log.debug("{} studies in system", studies.size());

        Collections.sort(studies, new NamedComparator());

        List<Study> ownedStudies
                = authorizationService.filterStudiesForVisibility(studies, siteCoordinator.getUserRole(Role.SITE_COORDINATOR));
        log.debug("{} studies visible to {}", ownedStudies.size(), siteCoordinator.getName());

        List<Study> assignableStudies = new ArrayList<Study>();
        for (Study ownedStudy : ownedStudies) {
            if (ownedStudy.isReleased()) {
                assignableStudies.add(ownedStudy);
            }
        }
        Collections.sort(assignableStudies, new NamedComparator());
        log.debug("{} released studies visible to {}", assignableStudies.size(), siteCoordinator.getName());
        return assignableStudies;
    }

    protected List<Site> getAssignableSites(User siteCoordinator) {
        List<Site> sites = new ArrayList(siteCoordinator.getUserRole(Role.SITE_COORDINATOR).getSites());
        log.debug("{} sites found for {} as site coord", sites.size(), siteCoordinator.getName());
        Collections.sort(sites, new NamedComparator());
        return sites;
    }

    protected List<Site> getAssignableSites(User siteCoordinator, Study study) {
        if (study == null) {
            return new ArrayList<Site>();
        }
        List<Site> sitesForSiteCoord = getAssignableSites(siteCoordinator);
        List<Site> assignableStudySites = new ArrayList<Site>();
        for (Site site : sitesForSiteCoord) {
            if (StudySite.findStudySite(study, site) != null) {
                assignableStudySites.add(site);
            }
        }
        log.debug("{} sites found for {} and study {}", new Object[]{assignableStudySites.size(), siteCoordinator.getName(), study.getName()});
        Collections.sort(assignableStudySites, new NamedComparator());
        return assignableStudySites;
    }

    protected List<User> getAssignableUsers(User siteCoordinator) {
        return userService.getSiteCoordinatorsAssignableUsers(siteCoordinator);
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

    public Crumb getCrumb() {
        return crumb;
    }

    public void setCrumb(Crumb crumb) {
        this.crumb = crumb;
    }

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }

    @Required
    public void setAuthorizationService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    public void setStudySiteService(StudySiteService studySiteService) {
        this.studySiteService = studySiteService;
    }

}
