package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Reorder;
import org.springframework.ui.ModelMap;

import java.util.List;
import java.util.Map;

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

    @Override
    protected Mode epochMode() {
        return new MoveEpoch();
    }

    @Override
    protected Mode armMode() {
        return new MoveArm();
    }

    private abstract class MoveMode<T extends PlanTreeNode<?>> implements Mode {
        boolean changed = false;

        public void performEdit() {
            int currentIndex = toMutateRevised().indexOf(toMoveRevised());
            int targetIndex = targetIndex(currentIndex);
            if (targetIndex == currentIndex) {
                changed = false;
                return;
            }

            updateRevision(toMoveParent(),
                Reorder.create(toMove(), currentIndex, targetIndex));
            changed = true;
        }

        private int targetIndex(int currentIndex) {
            int targetIndex = currentIndex + getOffset();
            targetIndex = Math.max(0, targetIndex);
            targetIndex = Math.min(toMutateRevised().size() - 1, targetIndex);
            return targetIndex;
        }

        @SuppressWarnings({ "unchecked" })
        public Map<String, Object> getModel() {
            ModelMap model = new ModelMap("changed", changed);
            if (changed) {
                int newIndex = toMutateRevised().indexOf(toMoveRevised());
                if (newIndex == toMutateRevised().size() - 1) {
                    model.addObject("position", "after")
                        .addObject("relativeTo", toMutateRevised().get(newIndex - 1));
                } else {
                    model.addObject("position", "before")
                        .addObject("relativeTo", toMutateRevised().get(newIndex + 1));
                }
            }
            return model;
        }

        protected abstract List<T> toMutate();
        protected abstract T toMove();

        protected abstract List<T> toMutateRevised();
        protected abstract T toMoveRevised();

        protected abstract PlanTreeNode<?> toMoveParent();
    }

    private class MoveArm extends MoveMode<Arm> {
        @Override
        protected List<Arm> toMutate() {
            return getArm().getEpoch().getArms();
        }

        @Override
        protected Arm toMove() {
            return getArm();
        }

        @Override
        protected List<Arm> toMutateRevised() {
            return getRevisedArm().getEpoch().getArms();
        }

        @Override
        protected Arm toMoveRevised() {
            return getRevisedArm();
        }

        public String getRelativeViewName() {
            return "moveArm";
        }

        @Override
        protected PlanTreeNode<?> toMoveParent() {
            return getSafeArmParent();
        }
    }

    private class MoveEpoch extends MoveMode<Epoch> {
        @Override
        protected List<Epoch> toMutate() {
            return getEpoch().getPlannedCalendar().getEpochs();
        }

        @Override
        protected Epoch toMove() {
            return getEpoch();
        }

        @Override
        protected List<Epoch> toMutateRevised() {
            return getRevisedEpoch().getPlannedCalendar().getEpochs();
        }

        @Override
        protected Epoch toMoveRevised() {
            return getRevisedEpoch();
        }

        public String getRelativeViewName() {
            return "moveEpoch";
        }

        @Override
        protected PlanTreeNode<?> toMoveParent() {
            return getSafeEpochParent();
        }
    }
}
