/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
import gov.nih.nci.cabig.ctms.domain.MutableDomainObject;

import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
public interface Parent<C extends Child, G extends Collection<C>> extends MutableDomainObject, Changeable {
    Class<C> childClass();

    void addChild(C child);
    /** Remove the child from children.  Should return the child IFF it was actually present and was removed. */
    C removeChild(C child);

    G getChildren();
    void setChildren(G children);
}
