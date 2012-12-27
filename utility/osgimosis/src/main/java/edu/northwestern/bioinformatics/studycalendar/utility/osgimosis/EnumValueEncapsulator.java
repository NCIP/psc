/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rhett Sutphin
 */
public class EnumValueEncapsulator implements Encapsulator {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private ClassLoader nearClassLoader;

    public EnumValueEncapsulator(ClassLoader nearClassLoader) {
        this.nearClassLoader = nearClassLoader;
    }

    public Object encapsulate(Object far) {
        int index = -1;
        for (int i = 0; i < far.getClass().getEnumConstants().length; i++) {
            Object enumConst = far.getClass().getEnumConstants()[i];
            if (enumConst == far) {
                index = i;
                break;
            }
        }

        if (index < 0) return far;

        try {
            Class<?> nearEnumClass = nearClassLoader.loadClass(far.getClass().getName());
            return nearEnumClass.getEnumConstants()[index];
        } catch (ClassNotFoundException e) {
            log.trace("Enum class {} not found in near CL {}", far.getClass().getName(), nearClassLoader);
            return far;
        }
    }
}
