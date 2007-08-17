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
    @Override
    protected List<C> createChildrenCollection() {
        return new LinkedList<C>();
    }

    public void addChild(C child, int index) {
        child.setParent(this);
        getChildren().add(index, child);
    }
}
