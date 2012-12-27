/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

/**
 * A trivial {@link Membrane} which does not convert the parameter objects at all.
 * It's intended for test environments where everything is actually on the same classloader.
 *
 * @author Rhett Sutphin
 */
public class TransparentMembrane implements Membrane {
    public Object farToNear(Object farObject) {
        return farObject;
    }

    public Object traverse(Object object, ClassLoader newCounterpartClassLoader) {
        return object;
    }

    public Object traverse(Object object, ClassLoader newCounterpartClassLoader, ClassLoader newCounterpartReverseClassLoader) {
        return object;
    }
}
