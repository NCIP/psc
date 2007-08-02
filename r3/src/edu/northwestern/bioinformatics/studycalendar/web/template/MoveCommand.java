package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Named;
import edu.northwestern.bioinformatics.studycalendar.domain.DomainObject;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;

import java.util.Map;
import java.util.List;

import org.springframework.ui.ModelMap;

/**
 * @author Rhett Sutphin
 */
public class MoveCommand extends ModalEditCommand {
    private Integer offset;

    ////// BOUND PROPERTIES

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    ////// MODES

    protected Mode epochMode() {
        return new MoveEpoch();
    }

    protected Mode armMode() {
        return new MoveArm();
    }

    private abstract class MoveMode<T extends DomainObject> implements Mode {
        boolean changed = false;

        public void performEdit() {
            int currentIndex = toMutate().indexOf(toMove());
            int targetIndex = targetIndex(currentIndex);
            if (targetIndex == currentIndex) {
                changed = false;
                return;
            }

            toMutate().remove(toMove());
            toMutate().add(targetIndex, toMove());
            changed = true;
        }

        private int targetIndex(int currentIndex) {
            int targetIndex = currentIndex + getOffset();
            targetIndex = Math.max(0, targetIndex);
            targetIndex = Math.min(toMutate().size() - 1, targetIndex);
            return targetIndex;
        }

        public Map<String, Object> getModel() {
            ModelMap model = new ModelMap("changed", changed);
            if (changed) {
                int newIndex = toMutate().indexOf(toMove());
                if (newIndex == toMutate().size() - 1) {
                    model.addObject("position", "after")
                        .addObject("relativeTo", toMutate().get(newIndex - 1));
                } else {
                    model.addObject("position", "before")
                        .addObject("relativeTo", toMutate().get(newIndex + 1));
                }
            }
            return model;
        }

        protected abstract List<T> toMutate();
        protected abstract T toMove();
    }

    private class MoveArm extends MoveMode<Arm> {
        protected List<Arm> toMutate() {
            return getArm().getEpoch().getArms();
        }

        protected Arm toMove() {
            return getArm();
        }

        public String getRelativeViewName() {
            return "moveArm";
        }
    }

    private class MoveEpoch extends MoveMode<Epoch> {
        protected List<Epoch> toMutate() {
            return getEpoch().getPlannedCalendar().getEpochs();
        }

        protected Epoch toMove() {
            return getEpoch();
        }

        public String getRelativeViewName() {
            return "moveEpoch";
        }
    }
}
