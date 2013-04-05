/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.Crumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.CrumbSource;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedCommand;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public abstract class PscAbstractCommandController<C>
    extends AbstractCommandController
    implements CrumbSource, PscAuthorizedHandler
{
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

    public Collection<ResourceAuthorization> authorizations(
        String httpMethod, Map<String, String[]> queryParameters
    ) throws Exception {
        if (PscAuthorizedCommand.class.isAssignableFrom(getCommandClass())) {
            HttpServletRequest request = fakeRequest(httpMethod, queryParameters);
            PscAuthorizedCommand command = (PscAuthorizedCommand) getCommand(request);
            if (command == null) throw new IllegalStateException("No command available for request " +  request);
            ServletRequestDataBinder binder = bindAndValidate(request, command);
            return command.authorizations(new BindException(binder.getBindingResult()));
        } else {
            throw new StudyCalendarError(
                "This controller either needs to override #authorizations or use a command that implements PscAuthorizedCommand");
        }
    }

    private HttpServletRequest fakeRequest(String httpMethod, Map<String, String[]> queryParameters) {
        MockHttpServletRequest req = new MockHttpServletRequest(httpMethod, null);
        req.setParameters(queryParameters == null ? Collections.emptyMap() : queryParameters);
        return req;
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
