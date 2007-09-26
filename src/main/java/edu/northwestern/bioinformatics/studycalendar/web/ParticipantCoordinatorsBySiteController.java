package edu.northwestern.bioinformatics.studycalendar.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import gov.nih.nci.security.authorization.domainobjects.User;

/**
 * @author Jaron Sampson
 * @author Rhett Sutphin
 *
 */

@AccessControl(roles = Role.SITE_COORDINATOR)
public class ParticipantCoordinatorsBySiteController extends AbstractController {
    private SiteDao siteDao;
    private StudyDao studyDao;
    private TemplateService templateService;
//    static Log log = LogFactory.getLog(ParticipantCoordinatorsBySiteController.class);
    static Logger log = LoggerFactory.getLogger(ParticipantCoordinatorsBySiteController.class);

    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Site site = siteDao.getById(ServletRequestUtils.getRequiredIntParameter(request, "site"));
        Study study = studyDao.getById(ServletRequestUtils.getRequiredIntParameter(request, "study"));
        Map<String, List<User>> userLists = templateService.getParticipantCoordinators(study, site);
        log.debug("+++" + userLists);
        Map model = new HashMap();
        model.put("assigned", userLists.get(StudyCalendarAuthorizationManager.ASSIGNED_USERS));
        model.put("available", userLists.get(StudyCalendarAuthorizationManager.AVAILABLE_USERS));
        model.put("site", site);
        return new ModelAndView("admin/ajax/participantCoordinatorsBySite", model);
    }

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
    
    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
}
