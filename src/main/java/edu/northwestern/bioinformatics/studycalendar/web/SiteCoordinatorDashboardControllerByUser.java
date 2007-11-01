package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@AccessControl(roles = {Role.SITE_COORDINATOR})
public class SiteCoordinatorDashboardControllerByUser extends AbstractSiteCoordinatorDashboardController {

    public SiteCoordinatorDashboardControllerByUser() {
        setFormView("siteCoordinatorDashboard");
    }

    protected Map referenceData(HttpServletRequest request, Object o, Errors errors) throws Exception {
        SiteCoordinatorDashboardCommandByUser command = (SiteCoordinatorDashboardCommandByUser) o;

        Map<String, Object> refdata = super.referenceData(request, o, errors);

        refdata.put("selected", command.getSelected());
        refdata.put("submitUrl", "/pages/dashboard/siteCoordinatorScheduleByUser");

        return refdata;
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        User siteCoordinator = getSiteCoordinator();

        List<User> assignableUsers    = getAssignableUsers(siteCoordinator);
        List<Study> assignableStudies = getAssignableStudies(siteCoordinator);

        Integer userId      = ServletRequestUtils.getIntParameter(request, "selected");
        User selectedUser   = getCurrentUser(userId, assignableUsers);

        List<Site> assignableSites = getAssignableSites(siteCoordinator);

        return createSiteCoordinatorDashboardCommand(selectedUser, assignableStudies, assignableSites, assignableUsers);
    }

    public AbstractSiteCoordinatorDashboardCommand createSiteCoordinatorDashboardCommand(User selectedUser, List<Study> assignableStudies, List<Site> assignableSites, List<User> assignableUsers) {
        return new SiteCoordinatorDashboardCommandByUser(getTemplateService(), selectedUser, assignableStudies, assignableSites, assignableUsers);
    }

    protected ModelAndView onSubmit(Object o) throws Exception {
        SiteCoordinatorDashboardCommandByUser command = (SiteCoordinatorDashboardCommandByUser) o;
        command.apply();

        RedirectView rv = new RedirectView("siteCoordinatorScheduleByUser");

        rv.addStaticAttribute("selected", command.getSelected().getId());

        return new ModelAndView(rv);
    }

    protected User getCurrentUser(Integer userId, List<User> assignableUsers) throws Exception {
        User user = null;
        if (userId != null ) {
            user = getUserDao().getById(userId);
        } else {
            if(assignableUsers.size() > 0) {
                user = assignableUsers.get(0);
            }
        }
        return user;
    }
}
