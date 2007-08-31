package edu.northwestern.bioinformatics.studycalendar.web.reporting;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;


/**
 * @author Jaron Sampson
 *
 */

@AccessControl(protectionGroups = StudyCalendarProtectionGroup.PARTICIPANT_COORDINATOR)
public class ReportBuilderSelectSitesController extends AbstractController {
    private SiteDao siteDao;
    private TemplateService templateService;
    
	private static final Logger log = LoggerFactory.getLogger(ReportBuilderSelectSitesController.class.getName());

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map model = new HashMap();
		
		int[] siteIds = ServletRequestUtils.getIntParameters(request, "sites");
		List<Site> sites = new ArrayList<Site>();
		for(int id : siteIds) {
			sites.add(siteDao.getById(id));
		}
        model.put("sitesSelected", sites);
        Set<Study> studiesSet = getStudiesForSites(sites);
        List<Study> studies = new ArrayList<Study>();
        for(Study study : studiesSet) {
        	studies.add(study);
        }
        model.put("studies", templateService.checkOwnership(ApplicationSecurityManager.getUser(), (List) studies));
        
        return new ModelAndView("reporting/ajax/studiesBySites", model);
	}

	//CONFIG
	////////

    @Required
	public void setSiteDao(SiteDao siteDao) {
		this.siteDao = siteDao;
	}
	
	//helper functions
	//////////////////
    protected Set<Study> getStudiesForSites(Collection<Site> sites) {
    	
		Set<Study> studies = new HashSet<Study>();
		for(Site site : sites) {
			for(StudySite studySite : site.getStudySites()) {
				studies.add(studySite.getStudy());
			}
		}
		return studies;
	}

    @Required
    public void setTemplateService(TemplateService templateService) {
		this.templateService = templateService;
	}



}
