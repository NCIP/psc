package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;

import java.util.Map;
import java.util.List;

import org.springframework.ui.ModelMap;

/**
 * @author Rhett Sutphin
 */
public class AddToCommand extends EditTemplateCommand {
    @Override
    protected Mode studyMode() {
        return new AddEpoch();
    }

    @Override
    protected Mode epochMode() {
        return new AddArm();
    }

    private class AddArm implements Mode {
        public String getRelativeViewName() {
            return "addArm";
        }

        public void performEdit() {
            Arm arm = new Arm();
            arm.setName("[Unnamed arm]");
            updateRevision(getEpoch(), Add.create(arm, getEpoch().getArms().size()));
        }

        @SuppressWarnings({ "unchecked" })
        public Map<String, Object> getModel() {
            List<Arm> arms = getRevisedEpoch().getArms();
            return new ModelMap("arm", arms.get(arms.size() - 1));
        }
    }

    private class AddEpoch implements Mode {
        public String getRelativeViewName() {
            return "addEpoch";
        }

        @SuppressWarnings({ "unchecked" })
        public Map<String, Object> getModel() {
            List<Epoch> epochs = getRevisedStudy().getPlannedCalendar().getEpochs();
            return new ModelMap("epoch", epochs.get(epochs.size() - 1));
        }

        public void performEdit() {
            Epoch epoch = Epoch.create("[Unnamed epoch]");
            PlannedCalendar cal = getStudy().getPlannedCalendar();

            updateRevision(getStudy().getPlannedCalendar(),
                Add.create(epoch, getRevisedStudy().getPlannedCalendar().getEpochs().size()));
        }
    }
}
