package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.DefaultPerson;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.Person;

import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class ProxyEncapsulatorTest extends OsgimosisTestCase {
    private Membrane aMembrane;
    private Object aInstance;
    private Class personB, defaultPersonB;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        aMembrane = new DefaultMembrane(loaderA,
            "edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people");
        aInstance = classFromLoader(DefaultPerson.class, loaderA).newInstance();
        personB = classFromLoader(Person.class, loaderB);
        defaultPersonB = classFromLoader(DefaultPerson.class, loaderB);
    }

    public void testProxyWithoutSuperclassIsJdkProxy() throws Exception {
        Encapsulator params = new ProxyEncapsulator(
            aMembrane, loaderB, Arrays.asList(personB));
        Object actual = params.encapsulate(aInstance);
        assertTrue("Class should contain $Proxy in name: " + actual.getClass().getName(),
            actual.getClass().getName().contains("$Proxy"));
    }

    public void testProxyWithSuperclassIsCglibProxy() throws Exception {
        Encapsulator params = new ProxyEncapsulator(
            aMembrane, loaderB, defaultPersonB, null, Arrays.asList(personB), null);
        Object actual = params.encapsulate(aInstance);
        assertTrue("Class should contain Enhancer in name: " + actual.getClass().getName(),
            actual.getClass().getName().contains("Enhancer"));
        assertTrue("Class should contain superclass in name: " + actual.getClass().getName(),
            actual.getClass().getName().contains("DefaultPerson"));
    }

    public void testComponentTypeWithBaseClassIsBaseClass() throws Exception {
        assertEquals(defaultPersonB,
            new ProxyEncapsulator(aMembrane, loaderB, defaultPersonB, null, Arrays.asList(personB), null).componentType());
    }
    
    public void testComponentTypeWithInterfacesIsFirstInterface() throws Exception {
        assertEquals(personB,
            new ProxyEncapsulator(aMembrane, loaderB, Arrays.asList(personB)).componentType());
    }
    
    public void testProxyConstructorParametersMayBeNull() throws Exception {
        Encapsulator encapsulator = new ProxyEncapsulator(
           aMembrane, loaderB, defaultPersonB, new Object[] { "Joe", null }, Arrays.asList(personB), null);
        Object actual = encapsulator.encapsulate(aInstance);
        assertNotNull(actual);
    }
}
