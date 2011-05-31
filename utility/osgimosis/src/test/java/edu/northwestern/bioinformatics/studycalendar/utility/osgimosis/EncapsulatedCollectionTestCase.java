package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.DefaultPerson;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;

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
}
