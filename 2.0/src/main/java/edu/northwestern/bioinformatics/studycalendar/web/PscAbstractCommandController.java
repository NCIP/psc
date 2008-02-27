package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.beans.factory.annotation.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.Crumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.CrumbSource;
import edu.northwestern.bioinformatics.studycalendar.web.schedule.DisplayScheduleController;

/**
 * @author Rhett Sutphin
 */
public abstract class PscAbstractCommandController<C> extends AbstractCommandController implements CrumbSource {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private Crumb crumb;
    private ControllerTools controllerTools;

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

    public ControllerTools getControllerTools() {
        return controllerTools;
    }

    @Required
    public void setControllerTools(ControllerTools controllerTools) {
        this.controllerTools = controllerTools;
    }
}
