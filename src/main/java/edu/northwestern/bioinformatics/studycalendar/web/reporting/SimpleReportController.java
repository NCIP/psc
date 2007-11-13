package edu.northwestern.bioinformatics.studycalendar.web.reporting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ReportRowDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ReportRow;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;

@AccessControl(roles = Role.PARTICIPANT_COORDINATOR)
public class SimpleReportController extends AbstractController {
	ReportRowDao reportRowDao;

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        List<ReportRow> reportRows = new ArrayList<ReportRow>();
        ArrayList<Site> sites = new ArrayList<Site>();
        reportRows = reportRowDao.getFilteredReport((List) sites, (List)  new ArrayList<Site>(),(List)  new ArrayList<Site>(), "", "");
        model.put("scheduledActivities", reportRows);
        return new ModelAndView("reporting/simpleReport", model);
	}
	
    ////// CONFIGURATION    
    @Required
	public void setReportRowDao(ReportRowDao reportRowDao) {
		this.reportRowDao = reportRowDao;
	}
    
}
