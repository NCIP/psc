package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
import edu.northwestern.bioinformatics.studycalendar.web.delta.RevisionChanges;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
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

    public DisplayTemplateController() {
        setCrumb(new Crumb());
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int studyId = ServletRequestUtils.getRequiredIntParameter(request, "study");
        Integer selectedStudySegmentId = ServletRequestUtils.getIntParameter(request, "studySegment");
        Integer selectedAmendmentId = ServletRequestUtils.getIntParameter(request, "amendment");
        Map<String, Object> model = new HashMap<String, Object>();

        Study loaded = studyDao.getById(studyId);

        Study study = selectAmendmentAndReviseStudy(loaded, selectedAmendmentId, model);

        StudySegment studySegment = selectStudySegment(study, selectedStudySegmentId);

        getControllerTools().addHierarchyToModel(studySegment.getEpoch(), model);
        model.put("studySegment", new StudySegmentTemplate(studySegment));

        if (study.isAvailableForAssignment()) {
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
            model.put("offStudyAssignments", offStudyAssignments);
            model.put("onStudyAssignments", onStudyAssignments);
        }

        return new ModelAndView("template/display", model);
    }

    private Study selectAmendmentAndReviseStudy(Study study, Integer selectedAmendmentId, Map<String, Object> model) {
        Amendment amendment = null;
        if (selectedAmendmentId == null) {
            amendment = study.getAmendment();
            if (amendment == null) {
                throw new StudyCalendarSystemException("No default amendment for " + study.getName());
            }
        } else if (study.getDevelopmentAmendment() != null && selectedAmendmentId.equals(study.getDevelopmentAmendment().getId())) {
            study = deltaService.revise(study, study.getDevelopmentAmendment());
            amendment = study.getDevelopmentAmendment();
            model.put("developmentRevision", study.getDevelopmentAmendment());
            if (!study.isInInitialDevelopment()) {
                model.put("revisionChanges",
                    new RevisionChanges(daoFinder, study.getDevelopmentAmendment(), study));
            }
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
