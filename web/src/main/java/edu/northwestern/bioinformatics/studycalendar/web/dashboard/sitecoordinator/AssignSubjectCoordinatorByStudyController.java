package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AccessControl(roles = {Role.SITE_COORDINATOR})
public class AssignSubjectCoordinatorByStudyController extends AbstractAssignSubjectCoordinatorController {

    public AssignSubjectCoordinatorByStudyController() {
        setFormView("dashboard/sitecoordinator/siteCoordinatorDashboard");
        setValidator(new ValidatableValidator());

    }

    protected Map referenceData(HttpServletRequest request, Object o, Errors errors) throws Exception {
        AssignSubjectCoordinatorByStudyCommand command = (AssignSubjectCoordinatorByStudyCommand) o;

        Map<String, Object> refdata = super.referenceData(request, o, errors);

        refdata.put("selected", command.getSelected());
        refdata.put("submitUrl", "/pages/dashboard/siteCoordinator/assignSubjectCoordinatorByStudy");

        return refdata;
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        User siteCoordinator = getSiteCoordinator();

        List<User> assignableUsers = getAssignableUsers(siteCoordinator);
        List<Study> assignableStudies = getAssignableStudies(siteCoordinator);

        if (request.getParameter("selected") != null && !request.getParameter("selected").trim().equalsIgnoreCase("")) {
            Integer studyId = ServletRequestUtils.getIntParameter(request, "selected");
            Study selectedStudy = getCurrentStudy(studyId, assignableStudies);

            List<Site> assignableSites = getAssignableSites(siteCoordinator, selectedStudy);

            return createSiteCoordinatorDashboardCommand(selectedStudy, assignableStudies, assignableSites, assignableUsers);
        }

        return createSiteCoordinatorDashboardCommand(null, assignableStudies, new ArrayList<Site>(), assignableUsers);

    }

    protected AbstractAssignSubjectCoordinatorCommand createSiteCoordinatorDashboardCommand(Study selectedStudy, List<Study> assignableStudies, List<Site> assignableSites, List<User> assignableUsers) {
        return new AssignSubjectCoordinatorByStudyCommand(getTemplateService(), selectedStudy, assignableStudies, assignableSites, assignableUsers);
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        AssignSubjectCoordinatorByStudyCommand command = (AssignSubjectCoordinatorByStudyCommand) oCommand;
        try {
            command.apply();
        } catch (StudyCalendarValidationException scve) {
            scve.rejectInto(errors);
        }

        if (errors.hasErrors()) {
            return showForm(request, response, errors);
        } else {
            RedirectView rv = new RedirectView("assignSubjectCoordinatorByStudy");
            rv.addStaticAttribute("selected", command.getSelected().getId());
            String successMessage = String.format("Information saved successfully.");
            ModelAndView modelAndView = new ModelAndView(rv);
            modelAndView.getModel().put("flashMessage", successMessage);
            return modelAndView;
        }
    }

    protected Study getCurrentStudy(Integer studyId, List<Study> assignableStudies) throws Exception {
        Study study = null;
        if (studyId != null) {
            study = getStudyDao().getById(studyId);
        }
//        else {
//            if(assignableStudies.size() > 0) {
//                study = assignableStudies.get(0);
//            }
//        }
        return study;
    }
}
