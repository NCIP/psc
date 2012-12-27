/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;

/**
 * @author Rhett Sutphin
 */
public class ArrayEncapsulator implements Encapsulator {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private ArrayCapableEncapsulator componentEncapsulator;

    public ArrayEncapsulator(ArrayCapableEncapsulator componentEncapsulator) {
        this.componentEncapsulator = componentEncapsulator;
    }

    public Object encapsulate(Object far) {
        if (!far.getClass().isArray()) {
            throw new IllegalArgumentException("This encapsulator is for arrays only");
        }
        int len = Array.getLength(far);
        Object near = Array.newInstance(componentEncapsulator.componentType(), len);
        for (int i = 0; i < len; i++) {
            Array.set(near, i, componentEncapsulator.encapsulate(Array.get(far, i)));
        }
        return near;
    }
}
