package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * @author Rhett Sutphin
 * @author John Dzak
 */
public class DisplayScheduleController extends AbstractController {
  protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
      return new ModelAndView(new RedirectView("/pages/subject", true, true, true), request.getParameterMap());  
  }
}