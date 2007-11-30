package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.Crumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.CrumbSource;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.beans.factory.annotation.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rhett Sutphin
 */
public abstract class PscSimpleFormController extends SimpleFormController implements CrumbSource {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private ControllerTools controllerTools;
    private Crumb crumb;

    protected PscSimpleFormController() {
        setBindOnNewForm(true);
    }

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
