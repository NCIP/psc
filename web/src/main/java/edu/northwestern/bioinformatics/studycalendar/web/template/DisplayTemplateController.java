package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.restlets.AbstractPscResource;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import static edu.northwestern.bioinformatics.studycalendar.domain.Role.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
import edu.northwestern.bioinformatics.studycalendar.web.delta.RevisionChanges;
import gov.nih.nci.cabig.ctms.lang.NowFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class DisplayTemplateController extends PscAbstractController {
    private StudyDao studyDao;
    private DeltaService deltaService;
    private AmendmentService amendmentService;
    private DaoFinder daoFinder;
    private UserDao userDao;
    private TemplateService templateService;
    private ApplicationSecurityManager applicationSecurityManager;
    private NowFactory nowFactory;

    public DisplayTemplateController() {
        setCrumb(new Crumb());
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String studyStringIdentifier = ServletRequestUtils.getRequiredStringParameter(request, "study");
        Integer selectedStudySegmentId = ServletRequestUtils.getIntParameter(request, "studySegment");
        Integer selectedAmendmentId = ServletRequestUtils.getIntParameter(request, "amendment");
        Map<String, Object> model = new HashMap<String, Object>();

        Study loaded = getStudyByTheIdentifier(studyStringIdentifier);
        int studyId = loaded.getId();
        
        Study study = selectAmendmentAndReviseStudy(loaded, selectedAmendmentId, model);

        StudySegment studySegment = selectStudySegment(study, selectedStudySegmentId);

        getControllerTools().addHierarchyToModel(studySegment.getEpoch(), model);
        model.put("studySegment", new StudySegmentTemplate(studySegment));

        Boolean canNotViewPopulations = study.isReleased() && selectedAmendmentId==null ;
        model.put("canNotViewPopulations", canNotViewPopulations);

        if (study.isReleased()) {
            String userName = applicationSecurityManager.getUserName();
            User user = userDao.getByName(userName);
            List<Study> subjectAssignableStudies = templateService.filterForVisibility(Collections.singletonList(study), user.getUserRole(SUBJECT_COORDINATOR));
            Boolean canAssignSubjects = !subjectAssignableStudies.isEmpty();

            List<StudySubjectAssignment> offStudyAssignments = new ArrayList<StudySubjectAssignment>();
            List<StudySubjectAssignment> onStudyAssignments = new ArrayList<StudySubjectAssignment>();
            List<StudySubjectAssignment> assignments = studyDao.getAssignmentsForStudy(studyId);

            for(StudySubjectAssignment currentAssignment: assignments) {
                if (currentAssignment.getEndDateEpoch() == null)
                    onStudyAssignments.add(currentAssignment);                                                                                                                                                                             
                else
                    offStudyAssignments.add(currentAssignment);
            }
            model.put("assignments", assignments);
            model.put("canAssignSubjects", canAssignSubjects);
            model.put("offStudyAssignments", offStudyAssignments);
            model.put("onStudyAssignments", onStudyAssignments);
        }

        List<Epoch> epochs = study.getPlannedCalendar().getEpochs();
        model.put("epochs", epochs);
        if(study.getAmendment()!=null && study.getDevelopmentAmendment()!=null) {
            model.put("disableAddAmendment", study.getAmendment().getReleasedDate());
        }

        model.put("todayForApi", AbstractPscResource.getApiDateFormat().format(nowFactory.getNow()));

        return new ModelAndView("template/display", model);
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

    ////// CONFIGURATION

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

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }

    @Required
    public void setNowFactory(NowFactory nowFactory) {
        this.nowFactory = nowFactory;
    }

    private static class Crumb extends DefaultCrumb {
        @Override
        public String getName(BreadcrumbContext context) {
            StringBuilder sb = new StringBuilder(context.getStudy().getName());
            if (context.getStudySegment() != null) {
                sb.append(" (").append(context.getStudySegment().getQualifiedName()).append(')');
            }
            return sb.toString();
        }

        @Override
        public Map<String, String> getParameters(BreadcrumbContext context) {
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
