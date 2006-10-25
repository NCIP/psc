package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;

import java.util.Map;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class AddArmCommand extends AddToCommand {
    private Epoch epoch;

    public AddArmCommand(StudyDao studyDao) { super(studyDao); }

    public Map<String, Object> getModel() {
        Map<String, Object> model = super.getModel();
        List<Arm> arms = getEpoch().getArms();
        model.put("arm", arms.get(arms.size() - 1));
        return model;
    }

    protected String whatAdded() {
        return "arm";
    }

    protected void createAndAddNewChild() {
        Arm arm = new Arm();
        arm.setName("New arm");
        epoch.addArm(arm);
    }

    protected Study toSave() {
        return getEpoch().getPlannedCalendar().getStudy();
    }

    ////// BOUND PROPERTIES

    public Epoch getEpoch() {
        return epoch;
    }

    public void setEpoch(Epoch epoch) {
        this.epoch = epoch;
    }
}
