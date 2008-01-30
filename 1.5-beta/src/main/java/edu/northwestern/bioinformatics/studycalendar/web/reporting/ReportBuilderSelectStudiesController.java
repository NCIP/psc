package edu.northwestern.bioinformatics.studycalendar.web.reporting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;

/**
 * @author Jaron Sampson
 *
 */

@AccessControl(roles = Role.SUBJECT_COORDINATOR)
public class ReportBuilderSelectStudiesController extends AbstractController {
    private StudyDao studyDao;
	

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map model = new HashMap();
		
		int[] studiesIds = ServletRequestUtils.getIntParameters(request, "studies");
		List<Study> studies = new ArrayList<Study>();
		for(int id : studiesIds) {
			studies.add(studyDao.getById(id));
		}
		model.put("studiesSelected", studies);
		Set<Subject> subjects = getSubjectsForStudies(studies);
		model.put("subjects", subjects);
		
        return new ModelAndView("reporting/ajax/subjectsByStudies", model);
        }

	//CONFIG
	////////
	
	@Required
	public void setStudyDao(StudyDao studyDao) {
		this.studyDao = studyDao;
	}
	
	//helper functions
	//////////////////
	private Set<Subject> getSubjectsForStudies(Collection<Study> studies) {
		   Set<Subject> subjects = new HashSet<Subject>();
		   for(Study study : studies) {
		     List<StudySubjectAssignment> assignments = studyDao.getAssignmentsForStudy(study.getId());
		     for(StudySubjectAssignment assignment : assignments) {
		       subjects.add(assignment.getSubject());
		     }
		   }
		   return subjects;
		}
	
}
