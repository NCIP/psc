package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.Crumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.CrumbSource;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * @author Rhett Sutphin
 */
public abstract class PscSimpleFormController extends SimpleFormController implements CrumbSource {
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
}
