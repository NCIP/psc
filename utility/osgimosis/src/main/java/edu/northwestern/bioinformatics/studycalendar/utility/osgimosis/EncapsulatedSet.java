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
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class EncapsulatedSet<T> extends EncapsulatedCollection<T> implements Set<T> {
    public EncapsulatedSet(Set farSet, Membrane membrane, ClassLoader nearClassLoader) {
        super(farSet, membrane, nearClassLoader);
    }
}
