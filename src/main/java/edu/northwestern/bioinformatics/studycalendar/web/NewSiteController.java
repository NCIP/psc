package edu.northwestern.bioinformatics.studycalendar.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;


@AccessControl(roles = {Role.STUDY_ADMIN, Role.SYSTEM_ADMINISTRATOR})
public class NewSiteController extends PscSimpleFormController {
    private SiteService siteService;
       
    public NewSiteController() {
        setCommandClass(NewSiteCommand.class);
        setFormView("createSite");
        setBindOnNewForm(true);
    }


    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, Object> refdata = new HashMap<String, Object>();
        refdata.put("action", "New");
        return refdata;
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        NewSiteCommand command = (NewSiteCommand) oCommand;
        Site site = command.createSite();
        return new ModelAndView("redirectToManageSites");
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
    	NewSiteCommand command = new NewSiteCommand();
        command.setSiteService(siteService);
        return command;
    }

    ////// CONFIGURATION

   
    @Required
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
    
}
