package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;

/**
 * This is very rough.
 *
 * @author Rhett Sutphin
 */
public class DisplayCalendarTemplateController extends AbstractController {
    private static final String SELECTED_CALENDAR_VIEW_COOKIE = "selectedCalendarView";

    private StudyDao studyDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int id = ServletRequestUtils.getIntParameter(request, "id");
        Study study = studyDao.getById(id);
        CalendarTemplate template = new CalendarTemplate(study.getPlannedCalendar());
        ListCalendarTemplate listTemplate = new ListCalendarTemplate(study.getPlannedCalendar());
        ModelAndView mv = new ModelAndView(selectViewName(request));
        mv.addObject("calendar", template);
        mv.addObject("listTemplate", listTemplate);
        mv.addObject("study", study);
        return mv;
    }

    private String selectViewName(HttpServletRequest request) {
        return getCalendarViewName(request) + "CalendarTemplate";
    }

    private String getCalendarViewName(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, SELECTED_CALENDAR_VIEW_COOKIE);
        return cookie == null ? "grid" : cookie.getValue();
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
}
