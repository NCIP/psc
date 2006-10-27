package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.web.ReturnSingleObjectController;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;

/**
 * @author Rhett Sutphin
 */
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.CREATE_STUDY)
public class SelectArmController extends ReturnSingleObjectController<Arm> {
    public SelectArmController() {
        setParameterName("arm");
        setViewName("template/ajax/selectArm");
    }

    @Override
    protected Object wrapObject(Arm loaded) {
        return new ArmTemplate(loaded);
    }
}
