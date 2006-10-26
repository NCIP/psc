package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Named;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;

import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class RenameCommand extends ModalEditCommand {
    private String value;

    ////// BOUND PROPERTIES

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    ////// MODES

    protected Mode studyMode() {
        return new RenameStudy();
    }

    protected Mode epochMode() {
        return new RenameEpoch();
    }

    protected Mode armMode() {
        return new RenameArm();
    }

    private abstract class RenameMode implements Mode {
        public Map<String, Object> getModel() {
            return null;
        }

        public String getRelativeViewName() {
            return "rename";
        }

        protected final void rename(Named named) {
            named.setName(getValue());
        }
    }

    private class RenameStudy extends RenameMode {
        public void performEdit() {
            rename(getStudy());
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
            if (!getArm().getEpoch().isMultipleArms()) {
                rename(getArm().getEpoch());
                setEpoch(getArm().getEpoch());
            }
        }
    }
}
