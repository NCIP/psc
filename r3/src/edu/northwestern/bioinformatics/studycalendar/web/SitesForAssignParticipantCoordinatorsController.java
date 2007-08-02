package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Yufang Wang
 */
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.SITE_COORDINATOR)
public class SitesForAssignParticipantCoordinatorsController extends AbstractController {
    private SiteDao siteDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Collection<Site> sites = siteDao.getAll();
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("sites", sites);
        return new ModelAndView("sitesForAssignParticipantCoordinators", model);
    }

    ////// CONFIGURATION

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }
}
