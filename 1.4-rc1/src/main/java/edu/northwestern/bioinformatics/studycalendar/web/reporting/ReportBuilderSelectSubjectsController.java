package edu.northwestern.bioinformatics.studycalendar.web.reporting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;

/**
 * @author Jaron Sampson
 *
 */

@AccessControl(roles = Role.SUBJECT_COORDINATOR)
public class ReportBuilderSelectSubjectsController extends AbstractController {
	
	private SubjectDao subjectDao;

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map model = new HashMap();
		
		int[] subjectIds = ServletRequestUtils.getIntParameters(request, "subjects");
		List<Subject> subjects = new ArrayList<Subject>();
		for(int id : subjectIds) {
			subjects.add(subjectDao.getById(id));
		}
		model.put("subjectsSelected", subjects);
		
        return new ModelAndView("reporting/ajax/datesBySubjects", model);
    }
	
	//CONFIG
	////////
	@Required
	public void setSubjectDao(SubjectDao subjectDao) {
		this.subjectDao = subjectDao;
	}
}
