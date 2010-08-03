package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.StudyWorkflowStatus;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.TemplateAvailability;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Saurabh Agrawal
 * @author Rhett Sutphin
 */
// TODO: use the API instead of this.
public class SearchTemplatesController extends PscAbstractController implements PscAuthorizedHandler {
    private StudyService studyService;
    private ApplicationSecurityManager applicationSecurityManager;
    private static final String SEARCH_TEXT_PARAMETER_NAME = "searchText";

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(PscRole.valuesWithStudyAccess());
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!request.getMethod().equals("GET")) {
            getControllerTools().sendGetOnlyError(response);
            return null;
        }
        if (request.getParameter(SEARCH_TEXT_PARAMETER_NAME) == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "searchText parameter is required");
            return null;
        }
        Map<TemplateAvailability, List<StudyWorkflowStatus>> templates = studyService.searchVisibleStudies(
            applicationSecurityManager.getUser(), getEffectiveSearchText(request));

        ModelAndView mv = new ModelAndView("template/ajax/templates");
        mv.addObject("inDevelopmentTemplates", templates.get(TemplateAvailability.IN_DEVELOPMENT));
        mv.addObject("releasedTemplates", allReleased(templates));
        return mv;
    }

    private List<StudyWorkflowStatus> allReleased(Map<TemplateAvailability, List<StudyWorkflowStatus>> source) {
        Set<StudyWorkflowStatus> unique = new HashSet<StudyWorkflowStatus>();
        unique.addAll(source.get(TemplateAvailability.PENDING));
        unique.addAll(source.get(TemplateAvailability.AVAILABLE));
        List<StudyWorkflowStatus> result = new ArrayList<StudyWorkflowStatus>(unique);
        Collections.sort(result, StudyWorkflowStatus.byReleaseDisplayName());
        return result;
    }

    private String getEffectiveSearchText(HttpServletRequest request) {
        String text = request.getParameter(SEARCH_TEXT_PARAMETER_NAME);
        if (StringUtils.isBlank(text)) {
            return null;
        } else {
            return text;
        }
    }

    ////// CONFIGURATION

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }
}
