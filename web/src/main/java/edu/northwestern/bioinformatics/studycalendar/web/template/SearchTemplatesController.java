package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.TemplateWorkflowStatus;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserTemplateRelationship;
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

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER;

/**
 * @author Saurabh Agrawal
 * @author Rhett Sutphin
 */
// TODO: use the API instead of this.
public class SearchTemplatesController extends PscAbstractController implements PscAuthorizedHandler {
    private TemplateService templateService;
    private ApplicationSecurityManager applicationSecurityManager;
    private static final String SEARCH_TEXT_PARAMETER_NAME = "searchText";

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        // TODO: this is insufficient
        return ResourceAuthorization.createCollection(STUDY_CALENDAR_TEMPLATE_BUILDER);
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
        Map<TemplateWorkflowStatus, List<UserTemplateRelationship>> templates = templateService.searchVisibleTemplates(
            applicationSecurityManager.getUser(), getEffectiveSearchText(request));

        ModelAndView mv = new ModelAndView("template/ajax/templates");
        mv.addObject("inDevelopmentTemplates", templates.get(TemplateWorkflowStatus.IN_DEVELOPMENT));
        mv.addObject("releasedTemplates", allReleased(templates));
        return mv;
    }

    private List<UserTemplateRelationship> allReleased(Map<TemplateWorkflowStatus, List<UserTemplateRelationship>> source) {
        Set<UserTemplateRelationship> unique = new HashSet<UserTemplateRelationship>();
        unique.addAll(source.get(TemplateWorkflowStatus.PENDING));
        unique.addAll(source.get(TemplateWorkflowStatus.AVAILABLE));
        List<UserTemplateRelationship> result = new ArrayList<UserTemplateRelationship>(unique);
        Collections.sort(result, UserTemplateRelationship.byReleaseDisplayName());
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
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }
}
