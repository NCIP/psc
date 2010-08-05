package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import gov.nih.nci.cabig.ctms.editors.DaoBasedEditor;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_QA_MANAGER;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER;

/**
 * @author Nataliya Shurupova
 */
public class AssociateSiteController extends PscSimpleFormController implements PscAuthorizedHandler {
    private StudyDao studyDao;
    private ApplicationSecurityManager applicationSecurityManager;
    private SiteDao siteDao;
    private Set<Object> unique;
    private Set<Site> managingSites;
    private Boolean isAllSites;
    private Study study;
    private StudyService studyService;

    public AssociateSiteController() {
        setCommandClass(AssociateSiteCommand.class);
        setValidator(new ValidatableValidator());
        setFormView("template/associateSite");
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        String[] studyArray = queryParameters.get("id");
        try {
            String studyString = studyArray[0];
            Integer studyId = Integer.parseInt(studyString);
            Study study = studyDao.getById(studyId);
            return ResourceAuthorization.createTemplateManagementAuthorizations(study, STUDY_QA_MANAGER, STUDY_CALENDAR_TEMPLATE_BUILDER);
        } catch (Exception e) {
            log.error("StudySite parameter is invalid " + e);
            return ResourceAuthorization.createCollection(STUDY_QA_MANAGER, STUDY_CALENDAR_TEMPLATE_BUILDER);
        }
    }

    @SuppressWarnings({ "unchecked" })
    @Override
     protected Object formBackingObject(HttpServletRequest httpServletRequest) throws Exception {
        PscUser user = applicationSecurityManager.getUser();
        study = studyDao.getById(ServletRequestUtils.getRequiredIntParameter(httpServletRequest, "id"));
        isAllSites = false;
        List<Site> sites = null;
        List<Site> listOfSiteObj = new ArrayList<Site>();

        SuiteRoleMembership membershipForStudyQAManager = user.getMembership(PscRole.STUDY_QA_MANAGER);
        SuiteRoleMembership membershipForCalendarTemplateBuilders = user.getMembership(PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER);

        if (membershipForCalendarTemplateBuilders == null && membershipForStudyQAManager == null) {
            throw new Exception("UserRoles don't exist for the specified user");
        }

        if (membershipForStudyQAManager != null) {
            if (membershipForStudyQAManager.isAllSites()) {
                sites = study.getSites();
                isAllSites = true;
            } else {
                listOfSiteObj = (List<Site>) membershipForStudyQAManager.getSites();
            }
        }

        if (membershipForCalendarTemplateBuilders != null) {

            if (membershipForCalendarTemplateBuilders.isAllSites()) {
                if (sites == null) {
                    sites = siteDao.getAll();
                    isAllSites = true;
                }
            } else {
                List<Site> siteObj1 = (List<Site>) membershipForCalendarTemplateBuilders.getSites();
                if (siteObj1 != null) {
                    listOfSiteObj.addAll(siteObj1);
                }
            }
        }

        if (sites != null && listOfSiteObj != null) {
            listOfSiteObj.addAll(sites);
        }

        unique = new LinkedHashSet<Object>(listOfSiteObj);

        managingSites = new HashSet<Site>();
        if (study.isManaged()) {
            managingSites = study.getManagingSites();
        }
        return new AssociateSiteCommand(study, studyService, unique, managingSites, isAllSites);
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        getControllerTools().registerDomainObjectEditor(binder, "sites", siteDao);
        binder.registerCustomEditor(Site.class, new DaoBasedEditor(siteDao));
    }

    @Override
    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest, Object o, Errors errors) throws Exception {
        Map<String, Object> refdata = new HashMap<String, Object>();
        refdata.put("isAllSites", isAllSites);
        refdata.put("study", study);
        refdata.put("isManaged", study.isManaged());
        refdata.put("userSitesToManage", unique);
        refdata.put("managingSites", managingSites);
        return refdata;
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        AssociateSiteCommand assignCommand = (AssociateSiteCommand) oCommand;
        assignCommand.apply();
        return getControllerTools().redirectToCalendarTemplate(ServletRequestUtils.getIntParameter(request, "id"));
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }
}