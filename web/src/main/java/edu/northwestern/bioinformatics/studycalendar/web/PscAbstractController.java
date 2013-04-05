/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.Crumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.CrumbSource;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.beans.factory.annotation.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rhett Sutphin
 */
public abstract class PscAbstractController extends AbstractController implements CrumbSource {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private Crumb crumb;
    private ControllerTools controllerTools;

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
