package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeOrderedInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Reorder;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class ReorderMutator implements Mutator {
    private Reorder reorder;

    public ReorderMutator(Reorder reorder) {
        this.reorder = reorder;
    }

    public void apply(PlanTreeNode<?> source) {
        PlanTreeNode<?> match = removeMatchingChild(source);
        PlanTreeOrderedInnerNode.cast(source).getChildren().add(reorder.getNewIndex(), match);
    }

    public void revert(PlanTreeNode<?> target) {
        PlanTreeNode<?> match = removeMatchingChild(target);
        PlanTreeOrderedInnerNode.cast(target).getChildren().add(reorder.getOldIndex(), match);
    }

    private PlanTreeNode<?> removeMatchingChild(PlanTreeNode<?> source) {
        List<PlanTreeNode<?>> children = PlanTreeOrderedInnerNode.cast(source).getChildren();
        PlanTreeNode<?> match = null;
        for (PlanTreeNode<?> child : children) {
            if (reorder.isSameChild(child)) {
                match = child;
                break;
            }
        }
        if (match == null) {
            throw new StudyCalendarSystemException(
                "The children of %s do not contain a match for the child in %s", source, reorder);
        }
        children.remove(match);
        return match;
    }

    public boolean appliesToExistingSchedules() {
        return false;
    }

    public void apply(ScheduledCalendar calendar) {
        throw new UnsupportedOperationException("apply not implemented");
    }
}
