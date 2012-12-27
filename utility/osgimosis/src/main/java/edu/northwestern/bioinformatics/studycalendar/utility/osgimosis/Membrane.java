/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

/**
 * A membrane represents the boundary between the sets of classes loaded by
 * two separate classloaders.  It has a "near" side and a "far" side and manages
 * taking instances across the boundary in both directions.
 *
 * @author Rhett Sutphin
 */
public interface Membrane {
    @SuppressWarnings({ "unchecked" })
    Object farToNear(Object farObject);

    @SuppressWarnings({ "unchecked" })
    Object traverse(Object object, ClassLoader newCounterpartClassLoader);

    @SuppressWarnings({ "unchecked" })
    Object traverse(Object object, ClassLoader newCounterpartClassLoader, ClassLoader newCounterpartReverseClassLoader);
}
