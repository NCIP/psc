package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.dao.ArmDao;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Rhett Sutphin
 */
public class SelectArmController implements Controller {
    private TemplateService templateService;
    private DeltaService deltaService;
    private ControllerTools controllerTools;
    private ArmDao armDao;

    @SuppressWarnings({ "unchecked" })
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int id = ServletRequestUtils.getRequiredIntParameter(request, "arm");
        Arm arm = armDao.getById(id);
        Map<String, Object> model = new HashMap<String, Object>();
        Study study = templateService.findStudy(arm);
        if (study.getDevelopmentAmendment() != null) {
            arm = deltaService.revise(arm);
            model.put("developmentRevision", study.getDevelopmentAmendment());
        }
        controllerTools.addHierarchyToModel(arm, model);
        model.put("arm", new ArmTemplate(arm));
        return new ModelAndView("template/ajax/selectArm", model);
    }

    @Required
    public void setControllerTools(ControllerTools controllerTools) {
        this.controllerTools = controllerTools;
    }

    @Required
    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Required
    public void setArmDao(ArmDao armDao) {
        this.armDao = armDao;
    }
}
