package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.PERSON_AND_ORGANIZATION_INFORMATION_MANAGER;

/**
 * @author Jaron Sampson
 */
@AccessControl(roles = Role.SYSTEM_ADMINISTRATOR)
public class ManageSitesController extends PscAbstractCommandController<ManageSitesCommand> implements PscAuthorizedHandler {
    private SiteService siteService;
    private ApplicationSecurityManager applicationSecurityManager;


    public Collection<ResourceAuthorization> authorizations(
        String httpMethod, Map<String, String[]> queryParameters
    ) {
        return ResourceAuthorization.createCollection(PERSON_AND_ORGANIZATION_INFORMATION_MANAGER);
    }

     @Override
    protected ManageSitesCommand getCommand(HttpServletRequest request) throws Exception {
        return ManageSitesCommand.create(siteService, applicationSecurityManager.getUser());
    }

    @Override
    protected ModelAndView handle(ManageSitesCommand command, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if ("POST".equals(request.getMethod())) {
            throw new StudyCalendarSystemException("POST method not implemented for this controller");
        } else {
            return new ModelAndView("manageSites", errors.getModel());
        }
    }

    ////// CONFIGURATION

    @Required
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }
}
