package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.beans.factory.annotation.Required;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.SYSTEM_ADMINISTRATOR;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_SUBJECT_CALENDAR_MANAGER;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_TEAM_ADMINISTRATOR;


public class RedirectToDashboardController extends PscAbstractController implements PscAuthorizedHandler {

    //todo - need to verify
    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(SYSTEM_ADMINISTRATOR, STUDY_SUBJECT_CALENDAR_MANAGER, STUDY_TEAM_ADMINISTRATOR);
    }
    
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
       SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        GrantedAuthority[] authority = authentication.getAuthorities();
        List<String> authorityList = new ArrayList<String>();
        for (GrantedAuthority anAuthority : authority) {
            authorityList.add(anAuthority.getAuthority());
        }
        if (authorityList.contains(Role.SUBJECT_COORDINATOR.toString())){
           return new ModelAndView(new RedirectView("dashboard/subjectCoordinatorSchedule"));
        } else if (authorityList.contains(Role.SITE_COORDINATOR.toString())){
           return new ModelAndView(new RedirectView("dashboard/siteCoordinator"));
        } else if (authorityList.contains(Role.SYSTEM_ADMINISTRATOR.toString())){
           return new ModelAndView(new RedirectView("dashboard/systemAdmin"));
        } else if (authorityList.contains(Role.STUDY_ADMIN.toString())){
           return new ModelAndView(new RedirectView("dashboard/studyAdmin"));
        } else if (authorityList.contains(Role.STUDY_COORDINATOR.toString())){
           return new ModelAndView(new RedirectView("dashboard/studyCoordinator"));
        }
        return null;
    }
}
