package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Rhett Sutphin
 */
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.BASE)
public class StudyListController extends AbstractController {
    private StudyDao studyDao;
    private TemplateService templateService; 

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Collection<Study> studies = studyDao.getAll();
        String userName = ApplicationSecurityManager.getUser(request);
        Collection<Study> ownedStudies = templateService.checkOwnership(userName, (List) studies);
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("studies", ownedStudies);
        return new ModelAndView("studyList", model);
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
    
    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
}
