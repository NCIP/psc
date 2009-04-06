package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.DefaultPerson;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.FinalPerson;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.NonDefaultPerson;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.Person;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.PersonProblem;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.PersonService;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.impl.PersonServiceImpl;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.impl.PieMaker;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
public class MembraneTest extends OsgimosisTestCase {
    private Membrane membrane;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        membrane = new Membrane(Thread.currentThread().getContextClassLoader(),
            "edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people");
    }

    public void testClassesAreIncompatibleWithoutBridging() throws Exception {
        Class<?> cFromA = classFromLoader(DefaultPerson.class, loaderA);
        Class<?> iFromB = classFromLoader(Person.class, getClass().getClassLoader());

        assertFalse("Class and interface should be incompatible", iFromB.isAssignableFrom(cFromA));
    }

    public void testMebraneFiltersServiceInterface() throws Exception {
        bridgedPersonService();
        // expect no class cast exception
    }

    public void testMembraneFiltersReturnedValues() throws Exception {
        PersonService near = bridgedPersonService();
        Person result = near.createPieMaker(); // no exception
        assertEquals("pie maker", result.getKind());
    }

    public void testMembraneAllowsAccessToInterfaceMethodsOnInaccessibleClasses() throws Exception {
        PersonService near = bridgedPersonService();
        Person result = near.createPrivateInvestigator();
        assertEquals("PI", result.getKind());
    }

    public void testMembraneAllowsAccessToObjectMethodsOnInaccessibleClasses() throws Exception {
        PersonService near = bridgedPersonService();
        Person result = near.createPrivateInvestigator();
        assertEquals("PrivateInvestigator[Emerson]", result.toString());
    }

    public void testMembraneFiltersMethodParameters() throws Exception {
        PersonService near = bridgedPersonService();
        Person titled = near.setTitle("Frau", new DefaultPerson("Olive", "waitress"));
        assertEquals("Frau Olive", titled.getName());
    }

    public void testMethodInvocationOnFarSideOfMembraneWorks() throws Exception {
        DefaultPerson in = new DefaultPerson("Chuck", "pie maker");
        assertEquals("PIE MAKER",  bridgedPersonService().capsKind(in));
    }

    public void testItemPassedBothDirectionsAcrossMembraneComesBackAsOriginalObject() throws Exception {
        DefaultPerson in = new DefaultPerson("Chuck", "pie maker");
        Person back = bridgedPersonService().same(in);
        assertEquals(DefaultPerson.class,  back.getClass());
        assertSame(in, back);
    }

    public void testBridgeObjectExtendsClassIfAvailable() throws Exception {
        Object bridged = membrane.farToNear(classFromLoader(DefaultPerson.class, loaderA).newInstance());
        assertTrue(DefaultPerson.class.isAssignableFrom(bridged.getClass()));
    }

    public void testBridgeObjectDoesNotExtendClassIfNotInBridgedPackage() throws Exception {
        Class<?> farClass = loaderA.loadClass(PieMaker.class.getName());
        Object farChuck = farClass.getConstructor(String.class).newInstance("Chuck");

        Class<?> nearClass = membrane.farToNear(farChuck).getClass();
        assertFalse("Should not match concrete class", PieMaker.class.isAssignableFrom(nearClass));
        assertTrue("Should match inteface", Person.class.isAssignableFrom(nearClass));
    }
    
    public void testBridgedObjectDoesNotExtendClassIfFinal() throws Exception {
        Class<?> farClass = loaderA.loadClass(FinalPerson.class.getName());
        Object farInstance = farClass.newInstance();

        Class<?> nearClass = membrane.farToNear(farInstance).getClass();
        assertFalse("Should not match concrete class", FinalPerson.class.isAssignableFrom(nearClass));
        assertTrue("Should match inteface", Person.class.isAssignableFrom(nearClass));
    }
    
    public void testBridgedObjectIsOfConcreteTypeWhenConstructorParametersAreProvided() throws Exception {
        Class<?> farClass = loaderA.loadClass(NonDefaultPerson.class.getName());
        Object farInstance = farClass.getConstructor(String.class).newInstance("Expected");
        membrane.registerProxyConstructorParameters(NonDefaultPerson.class.getName(), new Object[] { "proxy" });

        Object near = membrane.farToNear(farInstance);
        Class<?> nearClass = near.getClass();
        assertTrue("Should match concrete class", NonDefaultPerson.class.isAssignableFrom(nearClass));
        assertTrue("Should match inteface", Person.class.isAssignableFrom(nearClass));
        assertEquals("Value should be proxied", "Expected", ((Person) near).getName());
    }

    public void testBridgedObjectAccessedFromBridgedObjectIsProxiedInCorrectClassLoader() throws Exception {
        assertEquals(Color.BLACK, bridgedPersonService().hatColor(new FinalPerson()));
    }

    public void testBridgedNullIsNull() throws Exception {
        assertNull(membrane.farToNear(null));
    }

    public void testThrownExceptionsAreWrapped() throws Exception {
        try {
            bridgedPersonService().problem();
            fail("Test setup issue: exception not thrown");
        } catch (PersonProblem problem) {
            assertEquals("Implementation problem", problem.getMessage());
        }
    }

    public void testChainedFarSideInvocationWorks() throws Exception {
        PersonService service = bridgedPersonService();

        Person p2 = new DefaultPerson("A", "B");
        Person p1 = service.createPieMaker();
        assertFalse(service.equals(p1, p2));
    }

    public void testFarCollectionContentsAreEncapsulated() throws Exception {
        Collection<Person> actual = bridgedPersonService().createSeveral();
        assertEquals("Ned", actual.iterator().next().getName());
        assertEquals(3, actual.size());
    }

    public void testFarArrayIsEncapsulated() throws Exception {
        Object farArray = Array.newInstance(classFromLoader(Person.class, loaderA), 2);
        Array.set(farArray, 0, classFromLoader(DefaultPerson.class, loaderA).newInstance());
        Array.set(farArray, 1, classFromLoader(DefaultPerson.class, loaderA).newInstance());

        Object near = membrane.farToNear(farArray);
        assertTrue("Wrapped object should be an array: " + near.getClass().getName(),
            near.getClass().isArray());
        assertEquals("Wrapped object should be from context loader",
            Thread.currentThread().getContextClassLoader(),
            near.getClass().getClassLoader());
        assertTrue("Wrapped object should an array of Person: " + near.getClass().getName(),
            near instanceof Person[]);

        Person[] nearArray = (Person[]) near;
        assertEquals("Wrong length", 2, nearArray.length);
        assertTrue("Contents are wrong type", nearArray[0] instanceof Person);
    }

    private PersonService bridgedPersonService() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Object far = classFromLoader(PersonServiceImpl.class, loaderA).newInstance();
        return (PersonService) membrane.farToNear(far);
    }
}
