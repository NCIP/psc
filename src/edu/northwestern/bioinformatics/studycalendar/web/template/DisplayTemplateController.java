package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Rhett Sutphin
 */
@AccessControl(protectionGroups = { StudyCalendarProtectionGroup.STUDY_COORDINATOR, StudyCalendarProtectionGroup.BASE })
public class DisplayTemplateController extends PscAbstractController {
    private StudyDao studyDao;

    public DisplayTemplateController() {
        setCrumb(new Crumb());
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int studyId = ServletRequestUtils.getRequiredIntParameter(request, "study");
        Integer selectedArmId = ServletRequestUtils.getIntParameter(request, "arm");

        Study study = studyDao.getById(studyId);
        Arm arm = selectArm(study, selectedArmId);

        ModelMap model = new ModelMap();
        ControllerTools.addHierarchyToModel(arm.getEpoch(), model);
        model.addObject("arm", new ArmTemplate(arm));

        if (study.getPlannedCalendar().isComplete()) {
            model.addObject("assignments", studyDao.getAssignmentsForStudy(studyId));
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

    private static class Crumb extends DefaultCrumb {
        public String getName(BreadcrumbContext context) {
            StringBuilder sb = new StringBuilder(context.getStudy().getName());
            if (context.getArm() != null) {
                sb.append(" | ").append(context.getArm().getQualifiedName());
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
