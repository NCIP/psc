package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * @author John Dzak
 */
public class StudiesReportController extends PscSimpleFormController {
    public StudiesReportController() {
        setFormView("studiesReport");
    }


    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return super.formBackingObject(request);
    }


    protected ModelAndView onSubmit(Object command, BindException errors) throws Exception {
        return super.onSubmit(command, errors);
    }
}
