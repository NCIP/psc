package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Jaron Sampson
 */
@AccessControl(roles = Role.SYSTEM_ADMINISTRATOR)
public class ManageSitesController extends PscAbstractController {
    private SiteDao siteDao;
    private SiteService siteService = new SiteService();

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Collection<Site> sites = siteDao.getAll();
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("sites", sites);
        Map<Integer, Boolean> enableDeletes = new HashMap<Integer, Boolean>();
        for (Site site : sites) {
            if (siteService.checkIfSiteCanBeDeleted(site)) {
                enableDeletes.put(site.getId(), true);
            } else {
                enableDeletes.put(site.getId(), false);
            }
        }

        model.put("enableDeletes", enableDeletes);
        return new ModelAndView("manageSites", model);
    }

    ////// CONFIGURATION

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }
}
