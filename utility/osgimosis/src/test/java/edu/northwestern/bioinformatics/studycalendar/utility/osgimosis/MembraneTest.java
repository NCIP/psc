package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.DefaultPerson;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.Person;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.PersonProblem;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.PersonService;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.impl.PersonServiceImpl;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.impl.PieMaker;

/**
 * @author Rhett Sutphin
 */
public class MembraneTest extends OsgimosisTestCase {
    private Membrane membrane;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        membrane = Membrane.get(
            Thread.currentThread().getContextClassLoader(),
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

    public void testMembraneAllowsAccessToMethodsOnInaccessibleClasses() throws Exception {
        PersonService near = bridgedPersonService();
        Person result = near.createPrivateInvestigator();
        assertEquals("PI", result.getKind());
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

    private PersonService bridgedPersonService() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Object far = classFromLoader(PersonServiceImpl.class, loaderA).newInstance();
        return (PersonService) membrane.farToNear(far);
    }
}
