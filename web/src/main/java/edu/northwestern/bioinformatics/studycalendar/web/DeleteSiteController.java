package edu.northwestern.bioinformatics.studycalendar.web;


import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.HashMap;


/**
 * @author Jalpa Patel
 */
@AccessControl(roles = {Role.STUDY_ADMIN, Role.SYSTEM_ADMINISTRATOR})
public class DeleteSiteController extends PscAbstractController
{
    private SiteDao siteDao;
    private SiteService siteService;

    public DeleteSiteController() {

    }
    
     @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        Site site;
        //Boolean isDeletable;
        Integer siteId = ServletRequestUtils.getIntParameter(request, "site");
        site = siteDao.getById(siteId);
       // isDeletable = siteService.checkIfSiteCanBeDeleted(site);
        siteService.removeSite(site);
       // model.put("isDeletable",isDeletable);
        //log.info("====isDeletable" +isDeletable);
        return new ModelAndView("redirectToManageSites",model);

    }

    ////Configuration
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

}
