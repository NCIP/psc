
package edu.northwestern.bioinformatics.studycalendar.web.reporting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;

/**
 * @author Jaron Sampson
 *
 */

@AccessControl(protectionGroups = StudyCalendarProtectionGroup.PARTICIPANT_COORDINATOR)
public class ReportBuilderSelectSitesController extends AbstractController {
    private StudyDao studyDao;
    private TemplateService templateService;

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map model = new HashMap();
		
		//TODO: This is not the right service call, this gets all the studies the user has access to, not the set of studies the selected sites are assigned too - jsampson
        List<Study> studies = studyDao.getAll();
//        String userName = ApplicationSecurityManager.getUser(request);
//        List<Study> ownedStudies = templateService.checkOwnership(userName, studies);
        model.put("studies", studies);
        
        return new ModelAndView("reporting/ajax/studiesBySites", model);
	}

	//CONFIG
	////////
	@Required
	public void setStudyDao(StudyDao studyDao) {
		this.studyDao = studyDao;
	}

    @Required
    public void setTemplateService(TemplateService templateService) {
		this.templateService = templateService;
	}

}
