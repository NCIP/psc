package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyParticipantAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;

/**
 * @author Saurabh Agrawal
 */
public class DisplayICSCalendarController extends AbstractController {

	private static final Logger log = LoggerFactory.getLogger(DisplayICSCalendarController.class.getName());

	private StudyParticipantAssignmentDao studyParticipantAssignmentDao;

	public DisplayICSCalendarController() {
		// supports only get
		// https://trac.bioinformatics.northwestern.edu/studycalendar/ticket/202
		setSupportedMethods(new String[] { "GET" });
	}

	@Override
	protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
			throws Exception {

		String gridId = findGridIdInRequest(request);

		if (gridId != null) {
			StudyParticipantAssignment studyParticipantAssignment = studyParticipantAssignmentDao.getByGridId(gridId);
			Calendar icsCalendar = ICalTools.generateICalendar(studyParticipantAssignment);

			String fileName = generateICSfileName(studyParticipantAssignment);

			// response.setContentType("application/ics");
			response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

			response.setContentType("text/calendar");

			final CalendarOutputter output = new CalendarOutputter();
			output.setValidating(false);
			output.output(icsCalendar, response.getWriter());
			response.getWriter().close();

		}

		// return new ModelAndView("template/ajax/listOfParticipantsAndEvents", model);
		return null;
	}

	/**
	 * Generate ICS file name as lastname_firstname_studyprotocolauthorityid
	 * 
	 * @param studyParticipantAssignment the study participant assignment
	 * 
	 * @return the string
	 */
	private String generateICSfileName(final StudyParticipantAssignment studyParticipantAssignment) {
		StringBuffer fileName = new StringBuffer(studyParticipantAssignment.getGridId());
		Participant participant = studyParticipantAssignment.getParticipant();
		if (participant != null) {
			fileName = new StringBuffer("");
			if (participant.getLastName() != null) {
				fileName.append(participant.getLastName() + "-");
			}
			if (participant.getFirstName() != null) {
				fileName.append(participant.getFirstName() + "-");
			}
		}
		if (studyParticipantAssignment.getStudySite() != null
				&& studyParticipantAssignment.getStudySite().getStudy() != null
				&& studyParticipantAssignment.getStudySite().getStudy().getProtocolAuthorityId() != null) {
			fileName.append(studyParticipantAssignment.getStudySite().getStudy().getProtocolAuthorityId());
		}
		return fileName.append(".ics").toString();
	}

	/**
	 * Finds grid id in request. Returns null if no grid id is found
	 * 
	 * @param request the request
	 * 
	 * @return the grid id or null if no grid id is found
	 */
	private String findGridIdInRequest(final HttpServletRequest request) {
		// the grid Id should be in following form "/cal/schedule/display/[assignment-grid-id].ics"
		String gridId = null;
		String pathInfo = request.getPathInfo();
		if (pathInfo.indexOf(".ics") <= 0) {
			return null;
		}
		int beginIndex = pathInfo.indexOf("display/");

		int endIndex = pathInfo.indexOf(".ics");
		gridId = pathInfo.substring(beginIndex + 8, endIndex);

		return gridId;
	}

	// //// CONFIGURATION
	@Required
	public void setStudyParticipantAssignmentDao(final StudyParticipantAssignmentDao studyParticipantAssignmentDao) {
		this.studyParticipantAssignmentDao = studyParticipantAssignmentDao;
	}

}