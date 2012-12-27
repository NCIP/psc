/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.domain.DomainObject;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public abstract class PlanTreeOrderedInnerNode<P extends DomainObject, C extends PlanTreeNode>
	extends PlanTreeInnerNode<P, C, List<C>> {

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
