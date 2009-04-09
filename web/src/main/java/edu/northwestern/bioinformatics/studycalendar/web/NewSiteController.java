package edu.northwestern.bioinformatics.studycalendar.web;

import java.util.HashMap;
import java.util.Map;
import static java.lang.Integer.parseInt;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;


@AccessControl(roles = {Role.STUDY_ADMIN, Role.SYSTEM_ADMINISTRATOR})
public class NewSiteController extends PscSimpleFormController {
    private SiteService siteService;
    private SiteDao siteDao;
    protected final Logger log = LoggerFactory.getLogger(getClass());

    public NewSiteController() {
        setCommandClass(NewSiteCommand.class);
        setFormView("createSite");
        setBindOnNewForm(true);
    }
    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        Site site;
        Integer siteId = ServletRequestUtils.getIntParameter(request, "site");           
        if (siteId == null) {
            site = new Site();
        }
        else {
           site = siteDao.getById(siteId);
        }

        return new NewSiteCommand(site, siteService);
    }

    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest, Object oCommand, Errors errors) throws Exception {
           NewSiteCommand command = (NewSiteCommand) oCommand;
           Map<String, Object> refdata = new HashMap<String, Object>();
           refdata.put("sites",siteDao.getAll());
           refdata.put("action", "Create / Edit");
           refdata.put("name",command.getSite().getName());
           refdata.put("assignIdentifier",command.getSite().getAssignedIdentifier());
           return refdata;
       }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
         NewSiteCommand command = (NewSiteCommand) oCommand;
         try {
            Site site = command.createSite();
         } catch (StudyCalendarValidationException scve) {

           scve.rejectInto(errors);
         }
         return new ModelAndView("redirectToManageSites");

    }

    ////// CONFIGURATION

    @Required
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }
}


