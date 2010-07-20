package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.core.osgi.OsgiLayerTools;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudyProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.restlets.AbstractPscResource;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.StudyConsumer;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.DevelopmentTemplate;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.ReleasedTemplate;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.northwestern.bioinformatics.studycalendar.web.delta.RevisionChanges;
import gov.nih.nci.cabig.ctms.lang.NowFactory;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static edu.northwestern.bioinformatics.studycalendar.domain.Role.SUBJECT_COORDINATOR;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Rhett Sutphin
 */
@AccessControl(roles = {Role.STUDY_ADMIN, Role.SUBJECT_COORDINATOR, Role.STUDY_COORDINATOR, Role.SITE_COORDINATOR})
public class DisplayTemplateController extends PscAbstractController implements PscAuthorizedHandler {
    private StudyDao studyDao;
    private DeltaService deltaService;
    private AmendmentService amendmentService;
    private DaoFinder daoFinder;
    private ApplicationSecurityManager applicationSecurityManager;
    private NowFactory nowFactory;
    private AuthorizationService authorizationService;
    private StudyConsumer studyConsumer;
    private TemplateService templateService;
    private OsgiLayerTools osgiLayerTools;

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(
                DATA_IMPORTER,
                STUDY_QA_MANAGER,
                STUDY_TEAM_ADMINISTRATOR,
                STUDY_SITE_PARTICIPATION_ADMINISTRATOR,
                STUDY_CREATOR,
                STUDY_CALENDAR_TEMPLATE_BUILDER,
                STUDY_SUBJECT_CALENDAR_MANAGER,
                DATA_READER
        );
    }

    public DisplayTemplateController() {
        setCrumb(new Crumb());
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String studyStringIdentifier = ServletRequestUtils.getRequiredStringParameter(request, "study");
        Integer selectedStudySegmentId = ServletRequestUtils.getIntParameter(request, "studySegment");
        Integer selectedAmendmentId = ServletRequestUtils.getIntParameter(request, "amendment");
        Map<String, Object> model = new HashMap<String, Object>();

        model.put("anyProvidersAvailable", anyProvidersAvailable());

        Study loaded = getStudyByTheIdentifier(studyStringIdentifier);
        studyConsumer.refresh(loaded);
        int studyId = loaded.getId();
        
        Study study = selectAmendmentAndReviseStudy(loaded, selectedAmendmentId, model);
        List<Study> studies = new ArrayList<Study>();
        studies.add(study);

        User user = applicationSecurityManager.getUser().getLegacyUser();
        List<DevelopmentTemplate> inDevelopmentTemplates = templateService.getInDevelopmentTemplates(studies, user);
        List<ReleasedTemplate> releasedTemplates = templateService.getReleasedTemplates(studies, user);
        List<ReleasedTemplate> pendingTemplates = templateService.getPendingTemplates(studies, user);
        List<ReleasedTemplate> releasedAndAssignedTemplates = templateService.getReleasedAndAssignedTemplates(studies, user);
        model.put("user", user);
        if (!inDevelopmentTemplates.isEmpty() || !releasedTemplates.isEmpty() || !pendingTemplates.isEmpty() || !releasedAndAssignedTemplates.isEmpty()) {
            StudySegment studySegment = selectStudySegment(study, selectedStudySegmentId);
            getControllerTools().addHierarchyToModel(studySegment.getEpoch(), model);
            model.put("studySegment", new StudySegmentTemplate(studySegment));

            Boolean canEdit = user.hasRole(Role.STUDY_COORDINATOR) && study.isInDevelopment() && !(study.isReleased() && selectedAmendmentId==null);
            model.put("canEdit", canEdit);

            if (study.isReleased()) {
                List<StudySite> visibleStudySites = new ArrayList<StudySite>();
                if (user.hasRole(Role.STUDY_ADMIN) && user.hasRole(Role.SUBJECT_COORDINATOR)) {
                    visibleStudySites = study.getStudySites();
                    List<StudySite> subjectAssignableStudySites = authorizationService.filterStudySitesForVisibility(study.getStudySites(), user.getUserRole(SUBJECT_COORDINATOR));
                    makeOnOrOffAssignments(subjectAssignableStudySites, studyId, model);
                } else if (user.hasRole(Role.STUDY_ADMIN)) {
                    visibleStudySites = study.getStudySites();
                } else if (user.hasRole(Role.SUBJECT_COORDINATOR)) {
                    visibleStudySites = authorizationService.filterStudySitesForVisibility(study.getStudySites(), user.getUserRole(SUBJECT_COORDINATOR));
                    makeOnOrOffAssignments(visibleStudySites, studyId, model);
                }
                model.put("visibleStudySites", visibleStudySites);
            }

            List<Epoch> epochs = study.getPlannedCalendar().getEpochs();
            model.put("epochs", epochs);
            if(study.getAmendment()!=null && study.getDevelopmentAmendment()!=null) {
                model.put("disableAddAmendment", study.getAmendment().getReleasedDate());
            }
            model.put("todayForApi", AbstractPscResource.getApiDateFormat().format(nowFactory.getNow()));

            return new ModelAndView("template/display", model);

        } else {
            response.sendError(Status.CLIENT_ERROR_FORBIDDEN.getCode(),
                "Authenticated account is not authorized for this resource and method");
            return null;
        }
    }

    private void makeOnOrOffAssignments(List<StudySite> studySites, int studyId, Map<String, Object> model ) {
        List<StudySubjectAssignment> assignments = studyDao.getAssignmentsForStudy(studyId);
        List<StudySubjectAssignment> filteredAssignmnetns = authorizationService.filterStudySubjectAssignmentsByStudySite(studySites, assignments);
        List<StudySubjectAssignment> offStudyAssignments = new ArrayList<StudySubjectAssignment>();
        List<StudySubjectAssignment> onStudyAssignments = new ArrayList<StudySubjectAssignment>();
        for(StudySubjectAssignment currentAssignment: filteredAssignmnetns) {
            if (currentAssignment.getEndDateEpoch() == null)
                onStudyAssignments.add(currentAssignment);
            else
                offStudyAssignments.add(currentAssignment);
        }
        model.put("offStudyAssignments", offStudyAssignments);
        model.put("onStudyAssignments", onStudyAssignments);
    }

    public Study getStudyByTheIdentifier(String studyStringIdentifier){
        Study study;
        study = studyDao.getByAssignedIdentifier(studyStringIdentifier);
        if (study == null) {
            try {
                study = studyDao.getById(new Integer(studyStringIdentifier));
            } catch (NumberFormatException e) {
                log.debug("Can't convert id of the study " + study);
                study = null;
            }
        }
        if (study == null) {
            study = studyDao.getByGridId(studyStringIdentifier);
        }
        return study;
    }

    private Study selectAmendmentAndReviseStudy(Study study, Integer selectedAmendmentId, Map<String, Object> model) {
        Amendment amendment = null;
        if (selectedAmendmentId == null) {
            amendment = study.getAmendment();
            if (amendment == null) {
                amendment = study.getDevelopmentAmendment();
                if (amendment == null) {
                    throw new StudyCalendarSystemException("No default amendment for " + study.getName());
                } else {
                    study = reviseStudy(study);
                    model = insertDevelopmentRevisionInsideModel(study, model);
                }
            }
        } else if (study.getDevelopmentAmendment() != null && selectedAmendmentId.equals(study.getDevelopmentAmendment().getId())) {
            amendment = study.getDevelopmentAmendment();
            study = reviseStudy(study);
            model = insertDevelopmentRevisionInsideModel(study, model);
        } else if (study.getAmendment() != null && selectedAmendmentId.equals(study.getAmendment().getId())) {
            amendment = study.getAmendment();
        } else {
            Amendment search = study.getAmendment().getPreviousAmendment();
            while (search != null) {
                if (search.getId().equals(selectedAmendmentId)) {
                    study = amendmentService.getAmendedStudy(study, search);
                    amendment = search;
                    break;
                }
                search = search.getPreviousAmendment();
            }
            if (amendment == null) {
                throw new StudyCalendarSystemException("No amendment with id=" + selectedAmendmentId + " in " + study.getName());
            }
        }
        model.put("amendment", amendment);
        return study;
    }

    private Study reviseStudy(Study study) {
        return deltaService.revise(study, study.getDevelopmentAmendment());
    }

    private Map<String, Object> insertDevelopmentRevisionInsideModel(Study study, Map<String, Object> model) {
        model.put("developmentRevision", study.getDevelopmentAmendment());
        if (!study.isInInitialDevelopment()) {
            model.put("revisionChanges",
            new RevisionChanges(daoFinder, study.getDevelopmentAmendment(), study));
        }
        return model;
    }

    private StudySegment selectStudySegment(Study study, Integer selectedStudySegmentId) {
        if (selectedStudySegmentId == null) return defaultStudySegment(study);
        for (Epoch epoch : study.getPlannedCalendar().getEpochs()) {
            for (StudySegment studySegment : epoch.getStudySegments()) {
                if (studySegment.getId().equals(selectedStudySegmentId)) return studySegment;
            }
        }
        return defaultStudySegment(study);
    }

    private StudySegment defaultStudySegment(Study study) {
        return study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
    }

    private boolean anyProvidersAvailable() {
        boolean result = false;

        List providers = getOsgiLayerTools().getServices(StudyProvider.class);
        if (providers != null && providers.size() > 0) {
            result = true;
        }

        return result;
    }

    ////// CONFIGURATION

    public OsgiLayerTools getOsgiLayerTools() {
        return osgiLayerTools;
    }

    @Required
    public void setOsgiLayerTools(OsgiLayerTools tools) {
        this.osgiLayerTools = tools;
    }

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }

    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }

    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }

    @Required
    public void setNowFactory(NowFactory nowFactory) {
        this.nowFactory = nowFactory;
    }

    @Required
    public void setAuthorizationService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Required
    public void setStudyConsumer(StudyConsumer studyConsumer) {
        this.studyConsumer = studyConsumer;
    }

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    private static class Crumb extends DefaultCrumb {
        @Override
        public String getName(DomainContext context) {
            StringBuilder sb = new StringBuilder(context.getStudy().getName());
            if (context.getStudySegment() != null) {
                sb.append(" (").append(context.getStudySegment().getQualifiedName()).append(')');
            }
            return sb.toString();
        }

        @Override
        public Map<String, String> getParameters(DomainContext context) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("study", context.getStudy().getId().toString());
            if (context.getStudySegment() != null) {
                params.put("studySegment", context.getStudySegment().getId().toString());
            }
            if (context.getAmendment() != null) {
                params.put("amendment", context.getAmendment().getId().toString());
            }
            return params;
        }
    }
}
