package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.web.ReturnSingleObjectController;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
// TODO: this is not amendment-aware
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.BASE)
public class SelectArmController extends ReturnSingleObjectController<Arm> {
    private ControllerTools controllerTools;

    public SelectArmController() {
        setParameterName("arm");
        setViewName("template/ajax/selectArm");
    }

    @Override
    protected void amplifyModel(Arm arm, Map<String, Object> model) {
        controllerTools.addHierarchyToModel(arm, model);
    }

    @Override
    protected Object wrapObject(Arm loaded) {
        return new ArmTemplate(loaded);
    }

    @Required
    public void setControllerTools(ControllerTools controllerTools) {
        this.controllerTools = controllerTools;
    }
}
