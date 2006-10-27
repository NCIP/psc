package edu.northwestern.bioinformatics.studycalendar.web.template;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;

/**
 * @author Rhett Sutphin
 */
@AccessControl(protectionGroups = { StudyCalendarProtectionGroup.STUDY_COORDINATOR, StudyCalendarProtectionGroup.BASE })
public class DisplayTemplateController implements Controller {
    private StudyDao studyDao;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int studyId = ServletRequestUtils.getRequiredIntParameter(request, "study");
        Integer selectedArmId = ServletRequestUtils.getIntParameter(request, "arm");

        Study study = studyDao.getById(studyId);
        Arm arm = selectArm(study, selectedArmId);

        ModelMap model = new ModelMap();
        ControllerTools.addHierarchyToModel(arm.getEpoch(), model);
        model.addObject("arm", new ArmTemplate(arm));

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
}
