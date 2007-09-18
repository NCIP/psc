package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
import edu.northwestern.bioinformatics.studycalendar.web.delta.RevisionChanges;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Rhett Sutphin
 */
@AccessControl(protectionGroups = { StudyCalendarProtectionGroup.STUDY_COORDINATOR, StudyCalendarProtectionGroup.BASE })
public class DisplayTemplateController extends PscAbstractController {
    private StudyDao studyDao;
    private DeltaService deltaService;
    private DaoFinder daoFinder;

    public DisplayTemplateController() {
        setCrumb(new Crumb());
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int studyId = ServletRequestUtils.getRequiredIntParameter(request, "study");
        Integer selectedArmId = ServletRequestUtils.getIntParameter(request, "arm");
        ModelMap model = new ModelMap();

        Study loaded = studyDao.getById(studyId);

        Study study;
        // TODO: make this optional somehow
        if (loaded.getDevelopmentAmendment() != null) {
            study = deltaService.revise(loaded, loaded.getDevelopmentAmendment());
            model.put("developmentRevision", loaded.getDevelopmentAmendment());
            if (!loaded.isInInitialDevelopment()) {
                model.put("revisionChanges",
                    new RevisionChanges(daoFinder, loaded.getDevelopmentAmendment(), loaded));
            }
        } else {
            study = loaded;
        }

        Arm arm = selectArm(study, selectedArmId);

        getControllerTools().addHierarchyToModel(arm.getEpoch(), model);
        model.addObject("arm", new ArmTemplate(arm));

        if (study.isAvailableForAssignment()) {
            List<StudyParticipantAssignment> offStudyAssignments = new ArrayList<StudyParticipantAssignment>();
            List<StudyParticipantAssignment> onStudyAssignments = new ArrayList<StudyParticipantAssignment>();
            List<StudyParticipantAssignment> assignments = studyDao.getAssignmentsForStudy(studyId);

            for(StudyParticipantAssignment currentAssignment: assignments) {
                if (currentAssignment.getEndDateEpoch() == null)
                    onStudyAssignments.add(currentAssignment);
                else
                    offStudyAssignments.add(currentAssignment);
            }
            model.addObject("assignments", assignments);
            model.addObject("offStudyAssignments", offStudyAssignments);
            model.addObject("onStudyAssignments", onStudyAssignments);
        }

        return new ModelAndView("template/display", model);
    }

    private Arm selectArm(Study study, Integer selectedArmId) {
        if (selectedArmId == null) return defaultArm(study);
        for (Epoch epoch : study.getPlannedCalendar().getEpochs()) {
            for (Arm arm : epoch.getArms()) {
                if (arm.getId().equals(selectedArmId)) return arm;
            }
        }
        return defaultArm(study);
    }

    private Arm defaultArm(Study study) {
        return study.getPlannedCalendar().getEpochs().get(0).getArms().get(0);
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

    private static class Crumb extends DefaultCrumb {
        public String getName(BreadcrumbContext context) {
            StringBuilder sb = new StringBuilder(context.getStudy().getName());
            if (context.getArm() != null) {
                sb.append(" (").append(context.getArm().getQualifiedName()).append(')');
            }
            return sb.toString();
        }

        public Map<String, String> getParameters(BreadcrumbContext context) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("study", context.getStudy().getId().toString());
            if (context.getArm() != null) {
                params.put("arm", context.getArm().getId().toString());
            }
            return params;
        }
    }
}
