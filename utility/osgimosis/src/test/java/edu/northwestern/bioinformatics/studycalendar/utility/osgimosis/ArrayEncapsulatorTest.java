/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.DefaultPerson;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.Person;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class ArrayEncapsulatorTest extends OsgimosisTestCase {
    private Membrane aMembrane;
    private Object aInstance;
    private Class personA, defaultPersonA, personB, defaultPersonB;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        aMembrane = new DefaultMembrane(loaderA, "edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people");
        personA = classFromLoader(Person.class, loaderA);
        defaultPersonA = classFromLoader(DefaultPerson.class, loaderA);
        aInstance = defaultPersonA.newInstance();
        personB = classFromLoader(Person.class, loaderB);
        defaultPersonB = classFromLoader(DefaultPerson.class, loaderB);
    }

    public void testArrayOfInterfacesProxied() throws Exception {
        Object personBs = createPeopleArray(personB);

        Encapsulator enc = new ArrayEncapsulator(
            new ProxyEncapsulator(aMembrane, loaderA, Arrays.asList(personA)));
        Object personAs = enc.encapsulate(personBs);
        assertTrue("Encapsulated result is not array", personAs.getClass().isArray());
        assertEquals("Encapsulated result is not array of interface",
            personA, personAs.getClass().getComponentType());
        assertTrue("Contents not encapsulated",
            Array.get(personAs, 0).getClass().getName().contains("$Proxy"));
    }

    public void testArrayOfClassesProxied() throws Exception {
        Object personBs = createPeopleArray(defaultPersonB);

        Encapsulator enc = new ArrayEncapsulator(
            new ProxyEncapsulator(aMembrane, loaderA, defaultPersonA, null, Arrays.asList(personA), null));
        Object personAs = enc.encapsulate(personBs);
        assertTrue("Encapsulated result is not array", personAs.getClass().isArray());
        assertEquals("Encapsulated result is not array of base class",
            defaultPersonA, personAs.getClass().getComponentType());
        assertTrue("Contents not encapsulated",
            Array.get(personAs, 0).getClass().getName().contains("Enhancer"));
    }

    private Object createPeopleArray(Class componentType) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Object personBs = Array.newInstance(componentType, 2);
        Constructor cons = defaultPersonB.getConstructor(String.class, String.class);
        Array.set(personBs, 0, cons.newInstance("Andy", "writer"));
        Array.set(personBs, 1, cons.newInstance("Byron", "artist"));
        return personBs;
    }
}
