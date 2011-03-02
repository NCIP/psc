package edu.northwestern.bioinformatics.studycalendar.web.delta;

import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserTemplateRelationship;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class ViewAmendmentsController extends PscAbstractController implements PscAuthorizedHandler {
    private DaoFinder daoFinder;
    private StudyDao studyDao;
    private Configuration configuration;

    public ViewAmendmentsController() {
        setCrumb(new Crumb());
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        Study study = null;
        try {
            String studyId = queryParameters.get("study")[0];
            study = studyDao.getById(new Integer(studyId));
        } catch (Exception re) {
            log.debug("Error while extracting study from request; continuing with role-only authorization", re);
        }
        return ResourceAuthorization.createAllStudyAuthorizations(study);
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        PscUser user = getControllerTools().getCurrentUser(request);

        Study study = studyDao.getById(ServletRequestUtils.getRequiredIntParameter(request, "study"));
        model.put("study", study);
        log.trace("Displaying amendments of {} for {}", study, user);

        if (exposeDevelopmentAmendment(request, study)) {
            model.put("dev", new AmendmentView(user, study, study.getDevelopmentAmendment(), daoFinder, configuration));
        }

        List<Amendment> amendments = study.getAmendmentsList();
        List<AmendmentView> views = new ArrayList<AmendmentView>(amendments.size());
        for (Amendment amendment : amendments) {
            views.add(new AmendmentView(user, study, amendment, daoFinder, configuration));
        }
        model.put("amendments", views);

        Amendment selectedAmendment = selectAmendment(request, response, study);
        if (selectedAmendment == null) {
            // selectAmendment has already registered an HTTP error code
            return null;
        } else {
            model.put("amendment", selectedAmendment);
        }

        return new ModelAndView("delta/viewAmendments", model);
    }

    private Amendment selectAmendment(
        HttpServletRequest request, HttpServletResponse response, Study study
    ) throws ServletRequestBindingException, IOException {
        Integer selectedAmendmentId = ServletRequestUtils.getIntParameter(request, "amendment");
        Amendment selectedAmendment = null;
        if (selectedAmendmentId == null) {
            if (exposeDevelopmentAmendment(request, study)) {
                selectedAmendment = study.getDevelopmentAmendment();
            } else {
                selectedAmendment = study.getAmendment();
            }
        } else {
            if (study.getDevelopmentAmendment() != null && selectedAmendmentId.equals(study.getDevelopmentAmendment().getId())) {
                if (exposeDevelopmentAmendment(request, study)) {
                    selectedAmendment = study.getDevelopmentAmendment();
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
            } else {
                for (Amendment amendment : study.getAmendmentsList()) {
                    if (amendment.getId().equals(selectedAmendmentId)) {
                        selectedAmendment = amendment;
                        break;
                    }
                }
                if (selectedAmendment == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        String.format("The amendment with id %d (if any) is not part of the study %s",
                            selectedAmendmentId, study.getName()));
                }
            }
        }
        return selectedAmendment;
    }

    private boolean exposeDevelopmentAmendment(HttpServletRequest request, Study study) {
        PscUser user = getControllerTools().getCurrentUser(request);
        return new UserTemplateRelationship(user, study, configuration).getCanSeeDevelopmentVersion();
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }

    @Required
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public class Crumb extends DefaultCrumb {
        public Crumb() {
            setName("All amendments");
        }

        @Override
        public Map<String, String> getParameters(DomainContext context) {
            return createParameters(
                "study", context.getStudy().getId().toString(),
                "amendment", context.getAmendment().getId().toString()
            );
        }
    }
}
