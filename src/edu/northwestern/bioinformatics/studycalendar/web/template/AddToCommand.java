package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;

import java.util.Map;
import java.util.List;

import org.springframework.ui.ModelMap;

/**
 * @author Rhett Sutphin
 */
public class AddToCommand extends ModalEditCommand {
    protected Mode studyMode() {
        return new AddEpoch();
    }

    protected Mode epochMode() {
        return new AddArm();
    }

    private class AddArm implements Mode {
        public String getRelativeViewName() {
            return "addArm";
        }

        public void performEdit() {
            Arm arm = new Arm();
            arm.setName("New arm");
            getEpoch().addArm(arm);
        }

        public Map<String, Object> getModel() {
            List<Arm> arms = getEpoch().getArms();
            return new ModelMap("arm", arms.get(arms.size() - 1));
        }
    }

    private class AddEpoch implements Mode {
        public String getRelativeViewName() {
            return "addEpoch";
        }

        public Map<String, Object> getModel() {
            List<Epoch> epochs = getStudy().getPlannedCalendar().getEpochs();
            return new ModelMap("epoch", epochs.get(epochs.size() - 1));
        }

        public void performEdit() {
            getStudy().getPlannedCalendar().addEpoch(Epoch.create("New epoch"));
        }
    }
}
