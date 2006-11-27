package edu.northwestern.bioinformatics.studycalendar.web.reporting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;

/**
 * @author Jaron Sampson
 *
 */

@AccessControl(protectionGroups = StudyCalendarProtectionGroup.PARTICIPANT_COORDINATOR)
public class ReportBuilderSelectParticipantsController extends AbstractController {
	
	private ParticipantDao participantDao;

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map model = new HashMap();
		
		int[] participantIds = ServletRequestUtils.getIntParameters(request, "participants");
		List<Participant> participants = new ArrayList<Participant>();
		for(int id : participantIds) {
			participants.add(participantDao.getById(id));
		}
		model.put("participantsSelected", participants);
		
        return new ModelAndView("reporting/ajax/datesByParticipants", model);	
    }
	
	//CONFIG
	////////
	@Required
	public void setParticipantDao(ParticipantDao participantDao) {
		this.participantDao = participantDao;
	}
}
