package edu.northwestern.bioinformatics.studycalendar.web.reporting;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;

/**
 * @author Jaron Sampson
 *
 */

@AccessControl(roles = Role.SUBJECT_COORDINATOR)
public class ReportBuilderSelectDateRangeController extends AbstractController {

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map model = new HashMap();

		String fromFilter = ServletRequestUtils.getRequiredStringParameter(request, "startDateInput");
		String toFilter = ServletRequestUtils.getRequiredStringParameter(request, "endDateInput");
		
		model.put("fromSelected", fromFilter);
		model.put("toSelected", toFilter);
		
        return new ModelAndView("reporting/ajax/dateRange", model);	
	}

}
