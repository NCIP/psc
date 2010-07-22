package edu.northwestern.bioinformatics.studycalendar.web;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.bind.ServletRequestUtils;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.PERSON_AND_ORGANIZATION_INFORMATION_MANAGER;

@AccessControl(roles = {Role.STUDY_ADMIN, Role.SYSTEM_ADMINISTRATOR})
public class NewSiteController extends PscSimpleFormController implements PscAuthorizedHandler {
    private SiteService siteService;

    public NewSiteController() {
        setCommandClass(NewSiteCommand.class);
        setValidator(new ValidatableValidator());
        setFormView("createSite");
        setSuccessView("sites");
        setBindOnNewForm(true);
        setCrumb(new Crumb());
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        String[] siteArray = queryParameters.get("id");
        try {
            String siteString = siteArray[0];
            Integer siteId = Integer.parseInt(siteString);
            Site site = siteService.getById(siteId);
            return ResourceAuthorization.createCollection(site, PERSON_AND_ORGANIZATION_INFORMATION_MANAGER);
        } catch (Exception e) {
            log.error("StudySite parameter is invalid " + e);
            return ResourceAuthorization.createCollection(PERSON_AND_ORGANIZATION_INFORMATION_MANAGER);
        }
    }    

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        Site site;
        Integer siteId = ServletRequestUtils.getIntParameter(request, "id");
        if (siteId == null) {
            site = new Site();
        } else {
            site = siteService.getById(siteId);
        }
        return new NewSiteCommand(site, siteService);
    }

    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest, Object oCommand, Errors errors) throws Exception {
           NewSiteCommand command = (NewSiteCommand) oCommand;
           Map<String, Object> refdata = new HashMap<String, Object>();
           String actionText = ServletRequestUtils.getIntParameter(httpServletRequest, "id") == null ? "Create" : "Edit";
           refdata.put("action", actionText);
           refdata.put("site", command.getSite());
           refdata.put("sites", siteService.getAll());
           return refdata;
       }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
         NewSiteCommand command = (NewSiteCommand) oCommand;
         command.createSite();
         return new ModelAndView(new RedirectView(getSuccessView()));
    }

    ////// CONFIGURATION

    @Required
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private static class Crumb extends DefaultCrumb {
        @Override
        public String getName(DomainContext context) {
            StringBuilder sb = new StringBuilder();
            if (context.getSite() == null || context.getSite().getId() == null) {
                sb.append( "Create Site");
            } else {
                sb.append(" Edit Site ");
            }
            return sb.toString();
        }

        @Override
        public Map<String, String> getParameters(DomainContext context) {
            Map<String, String> params = new HashMap<String, String>();
            if (context.getSite() != null && context.getSite().getId() != null) {
                params.put("id", context.getSite().getId().toString());
            }
            return params;
        }
    }
}

