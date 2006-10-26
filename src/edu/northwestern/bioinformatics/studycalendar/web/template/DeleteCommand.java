package edu.northwestern.bioinformatics.studycalendar.web.template;

import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class DeleteCommand extends ModalEditCommand {

    ////// MODES

    protected Mode epochMode() {
        return new DeleteEpoch();
    }

    protected Mode armMode() {
        return new DeleteArm();
    }

    private class DeleteEpoch implements Mode {
        public String getRelativeViewName() {
            return "deleteEpoch";
        }

        public Map<String, Object> getModel() {
            return null;
        }

        public void performEdit() {
            getEpoch().getPlannedCalendar().getEpochs().remove(getEpoch());
        }
    }

    private class DeleteArm implements Mode {
        public String getRelativeViewName() {
            return "deleteArm";
        }

        public Map<String, Object> getModel() {
             return null;
        }

        public void performEdit() {
            getArm().getEpoch().getArms().remove(getArm());
        }
    }
}
