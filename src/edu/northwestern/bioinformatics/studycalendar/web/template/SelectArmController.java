package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.web.ReturnSingleObjectController;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;

/**
 * @author Rhett Sutphin
 */
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
