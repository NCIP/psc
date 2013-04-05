/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import java.util.Set;

/**
 * @author Rhett Sutphin
 */
public class SetEncapsulator implements Encapsulator {
    private Membrane membrane;
    private ClassLoader nearClassLoader;

    public SetEncapsulator(Membrane membrane, ClassLoader nearClassLoader) {
        this.membrane = membrane;
        this.nearClassLoader = nearClassLoader;
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public Object encapsulate(Object far) {
        return new EncapsulatedSet((Set) far, membrane, nearClassLoader);
    }
}