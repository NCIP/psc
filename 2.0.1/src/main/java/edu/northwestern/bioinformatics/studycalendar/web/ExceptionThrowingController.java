package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * For testing exception handling
 *
 * @author Rhett Sutphin
 */
public class ExceptionThrowingController implements Controller {
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        throw new IllegalStateException("This is a test for failure handling");
    }
}
