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
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;

/**
 * @author Jaron Sampson
 *
 */

@AccessControl(protectionGroups = StudyCalendarProtectionGroup.PARTICIPANT_COORDINATOR)
public class ReportBuilderSelectStudiesController extends AbstractController {
    private StudyDao studyDao;
	

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map model = new HashMap();
		
		int[] studiesIds = ServletRequestUtils.getIntParameters(request, "studies");
		List<Study> studies = new ArrayList<Study>();
		for(int id : studiesIds) {
			studies.add(studyDao.getById(id));
		}
		Set<Participant> participants = getParticipantsForStudies(studies);
		model.put("particpants", participants);
		
        return new ModelAndView("reporting/ajax/participantsByStudies", model);	
        }

	//CONFIG
	////////
	
	@Required
	public void setStudyDao(StudyDao studyDao) {
		this.studyDao = studyDao;
	}
	
	//helper functions
	//////////////////
	private Set<Participant> getParticipantsForStudies(Collection<Study> studies) {
		   Set<Participant> participants = new HashSet<Participant>();
		   for(Study study : studies) {
		     List<StudyParticipantAssignment> assignments = studyDao.getAssignmentsForStudy(study.getId());
		     for(StudyParticipantAssignment assignment : assignments) {
		       participants.add(assignment.getParticipant());
		     }
		   }
		   return participants;
		}
	
}
