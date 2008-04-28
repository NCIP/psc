package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.domain.DomainObject;

import java.util.List;
import java.util.LinkedList;

/**
 * @author Rhett Sutphin
 */
public abstract class PlanTreeOrderedInnerNode<P extends DomainObject, C extends PlanTreeNode>
    extends PlanTreeInnerNode<P, C, List<C>> 
{
    @SuppressWarnings({ "MethodOverridesStaticMethodOfSuperclass" })
    public static PlanTreeOrderedInnerNode<? extends DomainObject, PlanTreeNode<?>> cast(PlanTreeNode<?> source) {
        return ((PlanTreeOrderedInnerNode<? extends DomainObject, PlanTreeNode<?>>) source);
    }

    @Override
    protected List<C> createChildrenCollection() {
        return new LinkedList<C>();
    }

    public void addChild(C child, int index) {
        child.setParent(this);
        getChildren().add(index, child);
    }

    public int indexOf(C child) {
        int index = getChildren().indexOf(child);
        if (index >= 0) {
            return index;
        } else {
            throw new IllegalArgumentException(child + " is not a child of " + this);
        }
    }
}
