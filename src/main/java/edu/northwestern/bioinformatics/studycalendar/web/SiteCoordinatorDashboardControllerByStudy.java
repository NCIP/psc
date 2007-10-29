package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public class SiteCoordinatorDashboardControllerByStudy extends AbstractSiteCoordinatorDashboardController {

    public SiteCoordinatorDashboardControllerByStudy() {
        setFormView("siteCoordinatorDashboardByStudy");
    }

    protected Map referenceData(HttpServletRequest request, Object o, Errors errors) throws Exception {
        SiteCoordinatorDashboardCommandByStudy command = (SiteCoordinatorDashboardCommandByStudy) o;

        Map<String, Object> refdata = super.referenceData(request, o, errors);

        refdata.put("currentStudy", command.getStudy());

        return refdata;
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        User siteCoordinator = getSiteCoordinator();

        List<User>  assignableUsers   = getAssignableUsers(siteCoordinator);
        List<Study> assignableStudies = getAssignableStudies(siteCoordinator);

        Integer studyId            = ServletRequestUtils.getIntParameter(request, "study");
        Study   selectedStudy      = getCurrentStudy(studyId, assignableStudies);

        List<Site> assignableSites = getAssignableSites(siteCoordinator, selectedStudy);

        return createSiteCoordinatorDashboardCommand(selectedStudy, assignableStudies, assignableSites, assignableUsers);
    }

    protected AbstractSiteCoordinatorDashboardCommand createSiteCoordinatorDashboardCommand(Study selectedStudy, List<Study> assignableStudies, List<Site> assignableSites, List<User> assignableUsers) {
        return new SiteCoordinatorDashboardCommandByStudy(getTemplateService(), selectedStudy, assignableStudies, assignableSites, assignableUsers);
    }

    protected ModelAndView onSubmit(Object o) throws Exception {
        SiteCoordinatorDashboardCommandByStudy command = (SiteCoordinatorDashboardCommandByStudy) o;
        command.apply();

        RedirectView rv = new RedirectView("siteCoordinatorScheduleByStudy");

        rv.addStaticAttribute("study", command.getStudy().getId());

        return new ModelAndView(rv);
    }

    protected Study getCurrentStudy(Integer studyId, List<Study> assignableStudies) throws Exception {
        Study study = null;
        if (studyId != null ) {
            study = getStudyDao().getById(studyId);
        } else {
            if(assignableStudies.size() > 0) {
                study = assignableStudies.get(0);
            }
        }
        return study;
    }
}
