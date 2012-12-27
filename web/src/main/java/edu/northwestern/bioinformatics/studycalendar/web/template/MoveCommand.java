/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Reorder;
import org.springframework.ui.ModelMap;

import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class MoveCommand extends EditTemplateCommand {
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
    protected Mode studySegmentMode() {
        return new MoveStudySegment();
    }

    private abstract class MoveMode<T extends PlanTreeNode<?>> extends Mode {
        boolean changed = false;

        @Override
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

        @Override
        @SuppressWarnings({ "unchecked" })
        public Map<String, Object> getModel() {
            ModelMap model = new ModelMap("changed", changed);
            if (changed) {
                int newIndex = toMutateRevised().indexOf(toMoveRevised());
                if (newIndex == toMutateRevised().size() - 1) {
                    model.addAttribute("position", "after")
                        .addAttribute("relativeTo", toMutateRevised().get(newIndex - 1));
                } else {
                    model.addAttribute("position", "before")
                        .addAttribute("relativeTo", toMutateRevised().get(newIndex + 1));
                }
            }
            return model;
        }

        protected abstract T toMove();

        protected abstract List<T> toMutateRevised();
        protected abstract T toMoveRevised();

        protected abstract PlanTreeNode<?> toMoveParent();
    }

    private class MoveStudySegment extends MoveMode<StudySegment> {

        @Override
        protected StudySegment toMove() {
            return getStudySegment();
        }

        @Override
        protected List<StudySegment> toMutateRevised() {
            return getRevisedStudySegment().getEpoch().getStudySegments();
        }

        @Override
        protected StudySegment toMoveRevised() {
            return getRevisedStudySegment();
        }

        @Override
        public String getRelativeViewName() {
            return "moveStudySegment";
        }

        @Override
        protected PlanTreeNode<?> toMoveParent() {
            return getSafeStudySegmentParent();
        }
    }

    private class MoveEpoch extends MoveMode<Epoch> {

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

        @Override
        public String getRelativeViewName() {
            return "moveEpoch";
        }

        @Override
        protected PlanTreeNode<?> toMoveParent() {
            return getSafeEpochParent();
        }
    }
}
