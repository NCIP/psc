package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * @author Rhett Sutphin
 */
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.BASE)
public class StudyListController extends PscAbstractController {
    private StudyDao studyDao;
    private TemplateService templateService;
    private SiteService siteService;

    public StudyListController() {
        setCrumb(new DefaultCrumb("Home"));
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<Study> studies = studyDao.getAll();
        String userName = ApplicationSecurityManager.getUser();
        List<Study> ownedStudies = templateService.checkOwnership(userName, studies);
        List<Site> ownedSites = siteService.getSitesForSiteCd(userName);

        List<Study> assignableStudies = new ArrayList<Study>();
        List<Study> inDevelopmentStudies = new ArrayList<Study>();

        for (Study ownedStudy : ownedStudies) {
            // could be in both lists
            if (ownedStudy.isAvailableForAssignment()) {
                assignableStudies.add(ownedStudy);
            }
            if (ownedStudy.isInDevelopment()) {
                inDevelopmentStudies.add(ownedStudy);
            }
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("assignableStudies", assignableStudies);
        model.put("inDevelopmentStudies", inDevelopmentStudies);
        model.put("sites", ownedSites);
        
        return new ModelAndView("studyList", model);
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
    
    @Required
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
    
    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
}
