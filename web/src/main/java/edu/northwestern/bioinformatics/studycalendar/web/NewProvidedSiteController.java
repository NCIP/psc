/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;

import java.util.Collection;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.PERSON_AND_ORGANIZATION_INFORMATION_MANAGER;

/**
 * @author Jalpa Patel
 */
public class NewProvidedSiteController extends PscAbstractController implements PscAuthorizedHandler {
    private SiteService siteService;
    public NewProvidedSiteController() {}

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(PERSON_AND_ORGANIZATION_INFORMATION_MANAGER);
    }


    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String name = ServletRequestUtils.getRequiredStringParameter(request, "name");
        String assignedIdentifier = ServletRequestUtils.getRequiredStringParameter(request, "assignedIdentifier");
        String provider = ServletRequestUtils.getRequiredStringParameter(request, "provider");
        Site site = new Site();
        site.setName(name);
        site.setAssignedIdentifier(assignedIdentifier);
        site.setProvider(provider);
        siteService.createOrUpdateSite(site);
        return null;
    }

    @Required
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
}
