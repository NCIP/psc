package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Named;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;

import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class RenameCommand extends EditTemplateCommand {
    private String value;

    ////// BOUND PROPERTIES

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    ////// MODES

    @Override protected Mode studyMode() { return new RenameStudy(); }
    @Override protected Mode epochMode() { return new RenameEpoch(); }
    @Override protected Mode armMode()   { return new RenameArm(); }

    private abstract class RenameMode implements Mode {
        public Map<String, Object> getModel() {
            return null;
        }

        public String getRelativeViewName() {
            return "rename";
        }

        protected void rename(Named target) {
            updateRevision((PlanTreeNode<?>) target,
                PropertyChange.create("name", target.getName(), getValue()));
        }
    }

    // Rename study is unique in that it does not result in a Change
    // TODO: decide if this is reasonable
    private class RenameStudy extends RenameMode {
        public void performEdit() {
            getStudy().setName(getValue());
        }
    }

    private class RenameEpoch extends RenameMode {
        public void performEdit() {
            rename(getEpoch());
            if (!getEpoch().isMultipleArms()) {
                Arm soleArm = getEpoch().getArms().get(0);
                rename(soleArm);
                setArm(soleArm);
            }
        }
    }

    private class RenameArm extends RenameMode {
        public void performEdit() {
            rename(getArm());
            if (getArm().getEpoch() != null && !getArm().getEpoch().isMultipleArms()) {
                rename(getArm().getEpoch());
            }
        }
    }
}
