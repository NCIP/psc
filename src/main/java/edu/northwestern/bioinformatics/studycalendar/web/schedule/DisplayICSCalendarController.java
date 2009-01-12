package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Saurabh Agrawal
 */
public class DisplayICSCalendarController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(DisplayICSCalendarController.class.getName());

    private StudySubjectAssignmentDao studySubjectAssignmentDao;

    public DisplayICSCalendarController() {
        // supports only get
        // https://trac.bioinformatics.northwestern.edu/studycalendar/ticket/202
        setSupportedMethods(new String[]{"GET"});
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        String gridId = findGridIdInRequest(request);

        if (gridId != null) {
            StudySubjectAssignment studySubjectAssignment = studySubjectAssignmentDao.getByGridId(gridId);
            Calendar icsCalendar = ICalTools.generateICSCalendar(studySubjectAssignment);

            String fileName = ICalTools.generateICSfileName(studySubjectAssignment);

            // response.setContentType("application/ics");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            response.setContentType("text/calendar");

            final CalendarOutputter output = new CalendarOutputter();
            output.setValidating(false);
            output.output(icsCalendar, response.getWriter());
            response.getWriter().close();

        }

        // return new ModelAndView("template/ajax/listOfSubjectsAndEvents", model);
        return null;
    }

    /**
     * Finds grid id in request. Returns null if no grid id is found
     *
     * @param request the request
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
    public void setStudySubjectAssignmentDao(final StudySubjectAssignmentDao studySubjectAssignmentDao) {
        this.studySubjectAssignmentDao = studySubjectAssignmentDao;
    }

}