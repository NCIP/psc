/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeOrderedInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Reorder;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
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

    public void apply(Changeable source) {
        PlanTreeNode<?> match = removeMatchingChild((PlanTreeNode<?>) source);
        ((PlanTreeOrderedInnerNode<?, PlanTreeNode<?>>) source).getChildren().add(reorder.getNewIndex(), match);
    }

    public void revert(Changeable target) {
        PlanTreeNode<?> match = removeMatchingChild((PlanTreeNode<?>) target);
        ((PlanTreeOrderedInnerNode<?, PlanTreeNode<?>>) target).getChildren().add(reorder.getOldIndex(), match);
    }

    private PlanTreeNode<?> removeMatchingChild(PlanTreeNode<?> source) {
        List<PlanTreeNode<?>> children = ((PlanTreeOrderedInnerNode<?, PlanTreeNode<?>>) source).getChildren();
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
        throw new StudyCalendarSystemException("%s cannot be applied to an existing schedule", reorder);
    }
}
