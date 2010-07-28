package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.StudySiteService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.StudyWorkflowStatus;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.TemplateAvailability;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Rhett Sutphin
 */
public class StudyListController extends PscAbstractController implements PscAuthorizedHandler{
    private StudyDao studyDao;
    private TemplateService templateService;
    private ApplicationSecurityManager applicationSecurityManager;
    private StudySiteService studySiteService;

    public StudyListController() {
        setCrumb(new DefaultCrumb("Studies"));
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(
                DATA_IMPORTER,
                STUDY_QA_MANAGER, STUDY_TEAM_ADMINISTRATOR,
                STUDY_SITE_PARTICIPATION_ADMINISTRATOR,
                STUDY_CREATOR,
                STUDY_CALENDAR_TEMPLATE_BUILDER,
                STUDY_SUBJECT_CALENDAR_MANAGER,
                DATA_READER
        );
    }

    @Override   
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        PscUser user = applicationSecurityManager.getUser();

        Map<TemplateAvailability, List<StudyWorkflowStatus>> templates =
            templateService.getVisibleTemplates(user);

        studySiteService.refreshStudySitesForStudies(extractStudies(templates));

        log.debug("{} available templates visible to {}",
            templates.get(TemplateAvailability.AVAILABLE).size(), user.getUsername());
        log.debug("{} pending templates visible to {}",
            templates.get(TemplateAvailability.PENDING).size(), user.getUsername());
        log.debug("{} studies open for editing by {}",
            templates.get(TemplateAvailability.IN_DEVELOPMENT).size(), user.getUsername());
        log.trace("All visible templates: {}", templates);

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("pendingTemplates", templates.get(TemplateAvailability.PENDING));
        model.put("availableTemplates", templates.get(TemplateAvailability.AVAILABLE));
        model.put("inDevelopmentTemplates", templates.get(TemplateAvailability.IN_DEVELOPMENT));

        return new ModelAndView("studyList", model);
    }

    private List<Study> extractStudies(Map<TemplateAvailability, List<StudyWorkflowStatus>> templates) {
        Set<Study> studies = new LinkedHashSet<Study>();
        for (List<StudyWorkflowStatus> list : templates.values()) {
            for (StudyWorkflowStatus sws : list) {
                studies.add(sws.getStudy());
            }
        }
        return new ArrayList<Study>(studies);
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
    
    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }

    @Required
    public void setStudySiteService(StudySiteService studySiteService) {
        this.studySiteService = studySiteService;
    }
}
