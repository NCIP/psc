/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.DefaultHat;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.DefaultPerson;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Jalpa Patel
 */
public abstract class EncapsulatedCollectionTestCase extends OsgimosisTestCase {
    protected Membrane membrane;

    public void setUp() throws Exception {
        super.setUp();
        membrane = new DefaultMembrane(defaultClassLoader(),
            "edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people");
    }

    protected ClassLoader defaultClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    protected Object farPerson(String name) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor cons = classFromLoader(DefaultPerson.class, loaderA).getConstructor(String.class, String.class);
        return cons.newInstance(name, "traveler");
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    protected Object farHat(Color color) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor cons = classFromLoader(DefaultHat.class, loaderA).getConstructor(Color.class);
        return cons.newInstance(color);
    }
}
