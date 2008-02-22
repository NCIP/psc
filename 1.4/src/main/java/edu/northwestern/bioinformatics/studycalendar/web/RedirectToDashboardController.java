package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
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
import java.util.List;
import java.util.ArrayList;


public class RedirectToDashboardController extends PscAbstractController {
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
        } else if (authorityList.contains(Role.RESEARCH_ASSOCIATE.toString())){
           return new ModelAndView(new RedirectView("dashboard/researchAssociate"));
        } else if (authorityList.contains(Role.STUDY_COORDINATOR.toString())){
           return new ModelAndView(new RedirectView("dashboard/studyCoordinator"));
        }
        return null;
    }
}
