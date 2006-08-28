package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;

/**
 * This is very rough.
 *
 * @author Rhett Sutphin
 */
public class DisplayCalendarTemplateController extends AbstractController {
    private StudyDao studyDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int id = ServletRequestUtils.getIntParameter(request, "id");
        Study study = studyDao.getById(id);
        CalendarTemplate template = new CalendarTemplate(study.getPlannedSchedule());
        ModelAndView mv = new ModelAndView("calendarTemplate");
        mv.addObject("calendar", template);
        mv.addObject("study", study);
        return mv;
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
}
