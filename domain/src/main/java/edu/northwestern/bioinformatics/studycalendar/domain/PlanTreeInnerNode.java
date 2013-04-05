/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
import gov.nih.nci.cabig.ctms.domain.DomainObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

/**
 * @author Rhett Sutphin
 * @param <P> type of the parent
 * @param <C> type of the children of this element
 * @param <G> type of the collection of children (will be either List or SortedSet)
 */
public abstract class PlanTreeInnerNode<P extends DomainObject, C extends PlanTreeNode, G extends Collection<C>>
	extends PlanTreeNode<P> implements Parent<C, G>, Changeable
{
	private G children;

	protected PlanTreeInnerNode() {
		children = createChildrenCollection();
	}

	protected abstract G createChildrenCollection();

	@SuppressWarnings({"unchecked"})
	public void addChild(C child) {
		children.add(child);
		child.setParent(this);
	}

	@SuppressWarnings({"unchecked"})
	public C removeChild(C child) {
		if (children.remove(child)) {
			child.setParent(null);
			return child;
		} else {
			return null;
		}
	}

	/**
	 * Returns the child of this node that uniquely matches the given
	 * external key.  The semantics of the external key are dependent on the
	 * node's child type.
	 *
	 * @param key
	 *
	 * @return
	 */
	public abstract C findNaturallyMatchingChild(String key);

	// Utility method for subclasses whose children are Named
	protected Collection<C> findMatchingChildrenByName(String name) {
		List<C> found = new ArrayList<C>(getChildren().size());
		for (C child : getChildren()) {
			if (!(child instanceof Named)) {
				throw new IllegalStateException("This helper method only works if the children are Named");
			}
			if (name.equals(((Named) child).getName())) found.add(child);
		}
		return found;
	}

	// Utility method to assist implementations of #findNaturallyMatchingChild
	protected Collection<C> findMatchingChildrenByGridId(String gridId) {
		List<C> found = new ArrayList<C>(getChildren().size());
		for (C child : getChildren()) {
			if (gridId.equals(child.getGridId())) found.add(child);
		}
		return found;
	}

	@SuppressWarnings({"RawUseOfParameterizedType"})
	public boolean isAncestorOf(PlanTreeNode descendant) {
		Collection<C> all = getChildren();
		if (all instanceof SortedSet) {
			all = new ArrayList<C>(all);
		}
		if (all.contains(descendant)) {
			return true;
		} else if (PlanTreeInnerNode.class.isAssignableFrom(childClass())) {
			for (C child : getChildren()) {
				if (((PlanTreeInnerNode) child).isAncestorOf(descendant)) {
					return true;
				}
			}
			return false;
		}
		return false;
	}

	@Override
	public void clearIds() {
		super.clearIds();
		for (C child : getChildren()) {
			child.clearIds();
		}
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
	@SuppressWarnings({"unchecked"})
    public PlanTreeInnerNode<P, C, G> clone() {
		PlanTreeInnerNode<P, C, G> clone = (PlanTreeInnerNode<P, C, G>) super.clone();
		// deep clone the children
		clone.setChildren(clone.createChildrenCollection());
		for (C child : getChildren()) {
			clone.addChild((C) child.clone());
		}
		return clone;
	}

	@Override
	@SuppressWarnings({"unchecked"})
	protected PlanTreeInnerNode<P, C, G> copy() {
		PlanTreeInnerNode<P, C, G> copiedParent = (PlanTreeInnerNode<P, C, G>) super.copy();
		copiedParent.setChildren(copiedParent.createChildrenCollection());

		for (C child : getChildren()) {
			C copiedChild = (C) child.copy();
			copiedParent.addChild(copiedChild);
		}
		return copiedParent;
	}
}
