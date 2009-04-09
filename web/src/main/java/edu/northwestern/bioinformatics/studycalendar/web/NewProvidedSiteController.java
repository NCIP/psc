package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;

/**
 * @author Jalpa Patel
 */
@AccessControl(roles = {Role.SYSTEM_ADMINISTRATOR})
public class NewProvidedSiteController extends PscAbstractController {
    private SiteService siteService;
    public NewProvidedSiteController() {
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String name = ServletRequestUtils.getRequiredStringParameter(request, "name");
        String assignedIdentifier = ServletRequestUtils.getRequiredStringParameter(request, "assignedIdentifier");
        Site site = new Site();
        site.setName(name);
        site.setAssignedIdentifier(assignedIdentifier);
        siteService.createOrUpdateSite(site);
        return null;
    }
    @Required
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
}
