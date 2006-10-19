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
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;

/**
 * @author Rhett Sutphin
 */
public class DisplayTemplateController implements Controller {
    private StudyDao studyDao;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int studyId = ServletRequestUtils.getRequiredIntParameter(request, "study");
        Study study = studyDao.getById(studyId);
        PlannedCalendar calendar = study.getPlannedCalendar();
        Arm arm = calendar.getEpochs().get(0).getArms().get(0);

        ModelMap model = new ModelMap();
        ControllerTools.addHierarchyToModel(arm.getEpoch(), model);
        model.addObject("arm", new ArmTemplate(arm));

        return new ModelAndView("template/display", model);
    }

    ////// CONFIGURATION

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
}
