package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.PERSON_AND_ORGANIZATION_INFORMATION_MANAGER;

/**
 * @author Jaron Sampson
 */
@AccessControl(roles = Role.SYSTEM_ADMINISTRATOR)
public class ManageSitesController extends PscAbstractController implements PscAuthorizedHandler {
    private SiteService siteService;

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(PERSON_AND_ORGANIZATION_INFORMATION_MANAGER);
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Collection<Site> sites = siteService.getAll();
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("sites", sites);
        Map<Integer, Boolean> enableDeletes = new HashMap<Integer, Boolean>();
        for (Site site : sites) {
            if (site.hasAssignments()) {
                enableDeletes.put(site.getId(), false );
            } else {
                enableDeletes.put(site.getId(), true);
            }
        }

        model.put("enableDeletes", enableDeletes);
        return new ModelAndView("manageSites", model);
    }

    ////// CONFIGURATION

    @Required
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
}
