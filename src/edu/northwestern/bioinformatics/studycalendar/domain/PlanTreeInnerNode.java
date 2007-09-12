package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.domain.DomainObject;

import java.util.Collection;

/**
 * @author Rhett Sutphin
 * @param <P> type of the parent
 * @param <C> type of the children of this element
 * @param <G> type of the collection of children (will be either List or SortedSet)
 */
public abstract class PlanTreeInnerNode<P extends DomainObject, C extends PlanTreeNode, G extends Collection<C>>
    extends PlanTreeNode<P>
{
    private G children;

    protected PlanTreeInnerNode() {
        children = createChildrenCollection();
    }

    // Utility method for cleanliness
    @SuppressWarnings({ "unchecked" })
    public static PlanTreeInnerNode<?, PlanTreeNode<?>, ?> cast(PlanTreeNode<?> source) {
        return (PlanTreeInnerNode<?,PlanTreeNode<?>, ?>) source;
    }

    protected abstract G createChildrenCollection();
    public abstract Class<C> childClass();

    public void addChild(C child) {
        children.add(child);
        child.setParent(this);
    }

    public G getChildren() {
        return children;
    }

    public void setChildren(G children) {
        this.children = children;
    }

    @Override
    public void setMemoryOnly(boolean memoryOnly) {
        super.setMemoryOnly(memoryOnly);
        for (C child : getChildren()) {
            child.setMemoryOnly(memoryOnly);
        }
    }

    @Override
    protected PlanTreeInnerNode<P, C, G> clone() {
        PlanTreeInnerNode<P, C, G> clone = (PlanTreeInnerNode<P, C, G>) super.clone();
        // deep clone the children
        clone.setChildren(clone.createChildrenCollection());
        for (C child : getChildren()) {
            clone.addChild((C) child.clone());
        }
        return clone;
    }
}
