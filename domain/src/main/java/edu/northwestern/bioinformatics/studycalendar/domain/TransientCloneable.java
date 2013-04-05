/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

/**
 * @author Rhett Sutphin
 */
public interface TransientCloneable<T extends TransientCloneable<T>> {
    /**
     * Tainting flag to indicated that this node instance shouldn't be saved.  I.e., it is a
     * transient copy of the persistent node with the same IDs, used only for
     * building concrete revision trees.
     *
     * TODO: enforce this somehow, possibly with a hibernate event listener
     */
    boolean isMemoryOnly();
    void setMemoryOnly(boolean memoryOnly);

    T transientClone();
}
