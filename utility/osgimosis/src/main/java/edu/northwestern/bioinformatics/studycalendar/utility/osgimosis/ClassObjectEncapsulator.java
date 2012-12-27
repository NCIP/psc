/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

/**
 * @author Rhett Sutphin
 */
public class ClassObjectEncapsulator implements Encapsulator {
    private ClassLoader nearClassLoader;

    public ClassObjectEncapsulator(ClassLoader nearClassLoader) {
        this.nearClassLoader = nearClassLoader;
    }

    @SuppressWarnings( { "RawUseOfParameterizedType" })
    public Object encapsulate(Object far) {
        Class farClass = (Class) far;
        try {
            return nearClassLoader.loadClass(farClass.getName());
        } catch (ClassNotFoundException e) {
            throw new MembraneException(e,
                "Could not bridge class %s into %s", farClass.getName(), nearClassLoader);
        }
    }
}
