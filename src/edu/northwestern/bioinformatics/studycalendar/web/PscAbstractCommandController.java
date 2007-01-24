package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.Crumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.CrumbSource;

/**
 * @author Rhett Sutphin
 */
public abstract class PscAbstractCommandController<C> extends AbstractCommandController implements CrumbSource {
    private Crumb crumb;

    @Override
    @SuppressWarnings("unchecked")
    protected final ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        return handle((C) command, errors, request, response);
    }

    // The order of the parameters is different here b/c otherwise it might conflict with the superclass
    // handle() when the generic type is erased.
    protected abstract ModelAndView handle(C command, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception;

    ////// IMPLEMENTATION OF CrumbSource

    public Crumb getCrumb() {
        return crumb;
    }

    ////// CONFIGURATION

    public void setCrumb(Crumb crumb) {
        this.crumb = crumb;
    }
}
