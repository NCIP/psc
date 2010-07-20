package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_TEAM_ADMINISTRATOR;

@AccessControl(roles = {Role.SITE_COORDINATOR})
public class AssignSubjectCoordinatorByUserController extends AbstractAssignSubjectCoordinatorController implements PscAuthorizedHandler {

    public AssignSubjectCoordinatorByUserController() {
        setFormView("dashboard/sitecoordinator/siteCoordinatorDashboard");
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(STUDY_TEAM_ADMINISTRATOR);
    }     

    protected Map referenceData(HttpServletRequest request, Object o, Errors errors) throws Exception {
        AssignSubjectCoordinatorByUserCommand command = (AssignSubjectCoordinatorByUserCommand) o;

        Map<String, Object> refdata = super.referenceData(request, o, errors);

        refdata.put("selected", command.getSelected());
        refdata.put("submitUrl", "/pages/dashboard/siteCoordinator/assignSubjectCoordinatorByUser");

        return refdata;
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        User siteCoordinator = getSiteCoordinator();

        List<User> assignableUsers = getAssignableUsers(siteCoordinator);
        List<Study> assignableStudies = getAssignableStudies(siteCoordinator);

        Integer userId = ServletRequestUtils.getIntParameter(request, "selected");
        User selectedUser = getCurrentUser(userId, assignableUsers);

        List<Site> assignableSites = getAssignableSites(siteCoordinator);

        return createSiteCoordinatorDashboardCommand(selectedUser, assignableStudies, assignableSites, assignableUsers);
    }

    public AbstractAssignSubjectCoordinatorCommand createSiteCoordinatorDashboardCommand(User selectedUser, List<Study> assignableStudies, List<Site> assignableSites, List<User> assignableUsers) {
        return new AssignSubjectCoordinatorByUserCommand(getTemplateService(), selectedUser, assignableStudies, assignableSites, assignableUsers, getInstalledAuthenticationSystem());
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        AssignSubjectCoordinatorByUserCommand command = (AssignSubjectCoordinatorByUserCommand) oCommand;
        try {
            command.apply();

        } catch (StudyCalendarValidationException scve) {
            scve.rejectInto(errors);
        }

        if (errors.hasErrors()) {
            return showForm(request, response, errors);
        } else {
            RedirectView rv = new RedirectView("assignSubjectCoordinatorByUser");
            rv.addStaticAttribute("selected", command.getSelected().getId());

            String successMessage = String.format("Information saved successfully.");
            ModelAndView modelAndView = new ModelAndView(rv);
            modelAndView.getModel().put("flashMessage", successMessage);
            return modelAndView;

        }
    }

    protected User getCurrentUser(Integer userId, List<User> assignableUsers) throws Exception {
        User user = null;
        if (userId != null) {
            user = getUserDao().getById(userId);
        } else {
            if (assignableUsers.size() > 0) {
                user = assignableUsers.get(0);
            }
        }
        return user;
    }
}
